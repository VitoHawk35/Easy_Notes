package com.example.mydemo.ai.utils;

import com.example.mydemo.ai.exception.AIException;
import com.example.mydemo.ai.model.Response.ChatCompletionResponse;

public class AIResponseHelper {

    /**
     * 从响应体中获取AI回复（自动处理空值，避免空指针）
     * @param response 原始响应体（ChatCompletionResponse）
     * @return AI回复文本（非null，空场景返回空字符串）
     * @throws AIException 关键节点为空时抛出友好异常
     */
    public static String getReply(ChatCompletionResponse response) throws AIException {
        // 1. 空值校验：逐层判断，避免NPE
        if (response == null) {
            throw new AIException("AI响应体为空");
        }
        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new AIException("AI响应无有效结果（choices为空）");
        }
        ChatCompletionResponse.Choice firstChoice = response.getChoices().get(0);
        if (firstChoice == null || firstChoice.getMessage() == null) {
            throw new AIException("AI响应结果格式异常（无有效消息）");
        }
        // 2. 安全获取回复内容（空内容返回空字符串，避免"null"字样）
        return firstChoice.getMessage().getContent() == null ? "" : firstChoice.getMessage().getContent();
    }

    /**
     * 重载方法：直接从Response对象中获取回复（兼容原始回调）
     * @param response Retrofit的Response对象
     * @return AI回复文本
     * @throws AIException 响应失败或格式异常时抛出
     */
    public static String getReply(retrofit2.Response<ChatCompletionResponse> response) throws AIException {
        if (!response.isSuccessful()) {
            throw new AIException(String.format("AI请求失败：状态码=%d，消息=%s", response.code(), response.message()));
        }
        return getReply(response.body());
    }
}
