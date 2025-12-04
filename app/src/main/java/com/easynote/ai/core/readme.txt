使用教程：
 1. 获取AIProvider实例（单例）
AIProvider aiProvider = AIProvider.getInstance();

 2. 调用简化版API处理任务（以翻译为例）
aiProvider.processTranslate(
    "上下文：这是一篇科技类文章",  // 上下文
    "人工智能正在改变世界",     // 待翻译内容
    new AIResultCallback() {
        @Override
        public void onSuccess(String aiReply) {
            // 直接拿到翻译结果，无需解析！
            Log.d("AI翻译结果", aiReply); // 输出：Artificial intelligence is changing the world
        }

        @Override
        public void onFailure(AIException e) {
            // 统一处理异常（友好提示）
            Toast.makeText(context, "翻译失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
);

// 其他任务（如总结）同理：
aiProvider.process(
    "长文本内容...",
    TaskType.SUMMARY,
    new AIResultCallback() {
        @Override
        public void onSuccess(String aiReply) {
            Log.d("AI总结结果", aiReply); // 直接拿到总结后的字符串
        }

        @Override
        public void onFailure(AIException e) {
            // 异常处理
        }
    }
);