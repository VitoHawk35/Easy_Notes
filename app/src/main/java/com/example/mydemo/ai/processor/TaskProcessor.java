package com.example.mydemo.ai.processor;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;


import com.example.mydemo.ai.core.TaskType;
import com.example.mydemo.ai.exception.AIException;
import com.example.mydemo.ai.model.Response.ChatCompletionResponse;
import com.example.mydemo.ai.service.AIConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 任务处理器：负责管理AI请求的重试逻辑、线程切换、参数校验
 * 核心修复：API成功后立即终止未执行的重试任务，避免重复请求
 */
public class TaskProcessor {
    private final UniProcessor uniProcessor;
    private ExecutorService retryExecutor; // 用于重试延迟的后台线程池（需支持动态重建）
    private final Handler mainHandler;     // 用于将回调切换到主线程（UI线程安全）

    // 构造器：初始化依赖组件
    public TaskProcessor() {
        this.uniProcessor = new UniProcessor();
        this.retryExecutor = Executors.newSingleThreadExecutor(); // 单线程池（避免重试并发冲突）
        this.mainHandler = new Handler(Looper.getMainLooper());   // 绑定主线程Looper
    }

    /**
     * 对外暴露的核心异步接口：处理AI任务（支持摘要/翻译/润色/纠错）
     * @param text 输入文本（需处理的内容，非空）
     * @param taskType 任务类型（非空，枚举值）
     * @param callback 结果回调（非空，返回AI响应或错误）
     */
    public void processTask(String text, TaskType taskType, Callback<ChatCompletionResponse> callback) {
        // 1. 严格参数校验：避免空指针和无效请求
        if (text == null || text.isEmpty()) {
            postFailureToMainThread(callback, new AIException("输入文本不能为空"));
            return;
        }
        if (taskType == null) {
            postFailureToMainThread(callback, new AIException("任务类型不能为空（需指定SUMMARY/TRANSLATE等）"));
            return;
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback 不能为 null（需接收AI结果）");
        }

        // 2. 初始化重试状态：根据配置决定最大重试次数（ENABLE_RETRY=true时重试3次，否则0次）
        int maxRetry = AIConfig.ENABLE_RETRY ? AIConfig.MAX_RETRY_COUNT : 0;
        RetryState retryState = new RetryState(text, taskType, callback, maxRetry);

        // 3. 启动首次任务请求
        executeTask(retryState);
    }

    /**
     * 执行单次AI任务：调用UniProcessor发起Retrofit请求，处理响应后决定重试或返回结果
     */
    private void executeTask(RetryState retryState) {
        // 调用UniProcessor（封装火山方舟API的实际请求逻辑）
        uniProcessor.processText(
                retryState.text,
                retryState.taskType,
                new Callback<ChatCompletionResponse>() {
                    @Override
                    public void onResponse(Call<ChatCompletionResponse> call, Response<ChatCompletionResponse> response) {
                        // 响应成功的判定：HTTP 200+ 且 响应体非空（业务层成功）
                        if (response.isSuccessful() && response.body() != null) {
                            // 关键修复1：API成功后立即终止所有未执行的重试任务（避免重复请求）
                            cancelRetryTasks();
                            // 主线程回调成功结果（UI线程安全）
                            postResponseToMainThread(retryState.callback, call, response);
                        } else {
                            // 业务失败（如响应体为空、400/401等错误）：处理重试
                            @SuppressLint("DefaultLocale") String errorMsg = String.format(
                                    "AI服务业务失败：状态码=%d，消息=%s",
                                    response.code(),
                                    response.message()
                            );
                            handleRetryOrFail(retryState, new AIException(errorMsg));
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatCompletionResponse> call, Throwable t) {
                        // 网络失败（如超时、DNS解析失败）：处理重试
                        handleRetryOrFail(retryState, new AIException("AI服务请求失败（网络异常）", t));
                    }
                }
        );
    }

    /**
     * 处理重试或最终失败：有剩余次数则延迟重试，否则回调失败
     */
    private void handleRetryOrFail(RetryState retryState, AIException exception) {
        // 还有重试次数：延迟后执行下一次请求
        if (retryState.currentRetry < retryState.maxRetry) {
            retryState.currentRetry++; // 重试次数递增（从0开始）
            // 后台线程执行延迟（避免阻塞主线程）
            retryExecutor.execute(() -> {
                try {
                    // 重试延迟时间（从AIConfig读取，默认1000ms）
                    long delayMs = AIConfig.RETRY_DELAY;
                    TimeUnit.MILLISECONDS.sleep(delayMs);
                    // 延迟结束后，执行下一次任务
                    executeTask(retryState);
                } catch (InterruptedException e) {
                    // 延迟被中断（如页面销毁）：回调中断错误
                    Thread.currentThread().interrupt();
                    postFailureToMainThread(
                            retryState.callback,
                            new AIException("重试延迟被中断（可能页面已销毁）", e)
                    );
                }
            });
        } else {
            // 重试耗尽：回调最终失败（拼接重试次数信息）
            @SuppressLint("DefaultLocale") String finalFailMsg = retryState.maxRetry > 0
                    ? String.format("已重试%d次仍失败：%s", retryState.maxRetry, exception.getMessage())
                    : exception.getMessage();
            postFailureToMainThread(retryState.callback, new AIException(finalFailMsg, exception.getCause()));
        }
    }

    /**
     * 关键修复2：取消所有未执行的重试任务（API成功后调用）
     * 逻辑：终止线程池任务 → 重新创建线程池（避免后续请求无可用线程）
     */
    private void cancelRetryTasks() {
        if (retryExecutor != null && !retryExecutor.isShutdown()) {
            retryExecutor.shutdownNow(); // 强制终止所有未执行的延迟任务
            retryExecutor = Executors.newSingleThreadExecutor(); // 重建线程池（供后续请求使用）
        }
    }

    /**
     * 主线程投递成功结果：确保UI操作在主线程执行（避免View操作异常）
     */
    private void postResponseToMainThread(Callback<ChatCompletionResponse> callback, Call<ChatCompletionResponse> call, Response<ChatCompletionResponse> response) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 已在主线程：直接回调
            callback.onResponse(call, response);
        } else {
            // 不在主线程：通过Handler切换
            mainHandler.post(() -> callback.onResponse(call, response));
        }
    }

    /**
     * 主线程投递失败结果：同上，确保UI线程安全
     */
    private void postFailureToMainThread(Callback<ChatCompletionResponse> callback, Throwable t) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.onFailure(null, t);
        } else {
            mainHandler.post(() -> callback.onFailure(null, t));
        }
    }

    /**
     * 资源释放：页面销毁时调用，避免内存泄漏（线程池/Handler）
     */
    public void destroy() {
        // 终止线程池（不再接受新任务，强制关闭）
        if (retryExecutor != null && !retryExecutor.isShutdown()) {
            retryExecutor.shutdownNow();
            retryExecutor = null;
        }
        // 清除Handler未执行的回调（避免内存泄漏）
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 重试状态封装类：持有单次任务的所有上下文（输入文本、任务类型、回调、重试次数）
     */
    private static class RetryState {
        final String text;                // 输入文本（固定，不随重试变化）
        final TaskType taskType;          // 任务类型（固定）
        final Callback<ChatCompletionResponse> callback; // 最终结果回调（固定）
        final int maxRetry;               // 最大重试次数（固定）
        int currentRetry;                 // 当前重试次数（从0开始递增）

        RetryState(String text, TaskType taskType, Callback<ChatCompletionResponse> callback, int maxRetry) {
            this.text = text;
            this.taskType = taskType;
            this.callback = callback;
            this.maxRetry = maxRetry;
            this.currentRetry = 0; // 初始重试次数为0（首次请求）
        }
    }
}