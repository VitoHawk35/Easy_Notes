package com.easynote.ai.core;

import com.easynote.ai.exception.AIException;
import com.easynote.ai.model.Response.ChatCompletionResponse;
import com.easynote.ai.processor.TaskProcessor;
import com.easynote.ai.processor.utils.AIResponseHelper;

import retrofit2.Callback;
import retrofit2.Response;

public class AIProvider {
    private static AIProvider instance;
    private final TaskProcessor processor;

    private AIProvider(){
        this.processor=new TaskProcessor();
    }

    public static synchronized AIProvider getInstance() {
        if (instance == null) {
            instance = new AIProvider();
        }
        return instance;
    }


    // ========== 新增：简化版API（推荐开发者使用） ==========
    /**
     * 简化版：处理AI任务（无需关注响应解析）
     * @param text 输入文本
     * @param taskType 任务类型（TRANSLATE/POLISH/SUMMARY/CORRECT）
     * @param callback 简化回调（直接拿到结果或异常）
     */
    public void process(String text, TaskType taskType, AIResultCallback callback) {
        // 调用原有process方法，内部自动解析响应
        process(text, taskType, new Callback<ChatCompletionResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ChatCompletionResponse> call, Response<ChatCompletionResponse> response) {
                try {
                    // 用工具类解析响应，直接返回字符串
                    String aiReply = AIResponseHelper.getReply(response);
                    callback.onSuccess(aiReply);
                } catch (AIException e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ChatCompletionResponse> call, Throwable t) {
                callback.onFailure(new AIException("AI请求失败：" + t.getMessage(), t));
            }
        });
    }

    /**
     * 简化版：翻译任务（专门重载，进一步降低翻译场景的使用成本）
     * @param content 上下文内容
     * @param text 待翻译内容
     * @param callback 简化回调
     */
    public void processTranslate(String content, String text, AIResultCallback callback) {
        String safeContent = content == null ? "" : content;
        String safeText = text == null ? "" : text;
        String userPrompt = "请结合以下上下文翻译指定内容（仅输出翻译结果，无需额外说明）：\n"
                + "上下文：" + safeContent + "\n"
                + "待翻译内容：" + safeText;
        // 复用简化版process方法
        process(userPrompt, TaskType.TRANSLATE, callback);
    }

    // ========== 原有方法保留（兼容老用户） ==========
    public void process(String text, TaskType taskType, Callback<ChatCompletionResponse> callback) {
        processor.processTask(text, taskType, callback);
    }
    public void destroy() {
        if (processor != null) {
            processor.destroy();
        }
    }
}
