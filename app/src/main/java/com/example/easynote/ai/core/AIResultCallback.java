package com.example.easynote.ai.core;

import com.example.easynote.ai.exception.AIException;
/**
 * 简化版回调接口：只关注「成功结果」和「失败原因」，屏蔽原始响应细节
 */
public interface AIResultCallback {
    /**
     * 成功回调：直接返回AI处理后的文本（无需解析）
     * @param aiReply AI回复内容（非null，空场景返回空字符串）
     */
    void onSuccess(String aiReply);

    /**
     * 失败回调：返回统一的异常信息
     * @param e 异常（包含友好提示，如“响应为空”“网络异常”等）
     */
    void onFailure(AIException e);
}
