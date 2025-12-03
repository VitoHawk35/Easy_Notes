package com.easynote.ai.core;



import com.easynote.ai.model.Response.ChatCompletionResponse;
import com.easynote.ai.processor.TaskProcessor;

import retrofit2.Callback;

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


    /**
     * 通用处理（支持所有任务类型）
     */
    public void process(String text, TaskType taskType, Callback<ChatCompletionResponse> callback) {
        processor.processTask(text, taskType, callback);
    }

    public void destroy() {
        if (processor != null) {
            processor.destroy();
        }
    }
}
