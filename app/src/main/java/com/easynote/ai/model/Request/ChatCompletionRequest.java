package com.easynote.ai.model.Request;

import com.easynote.ai.model.Message;
import com.easynote.ai.service.AIConfig;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatCompletionRequest {
    @SerializedName("model")
    private String model;
    private List<Message> messages;
    private boolean stream;

    public ChatCompletionRequest(List<Message> messages){
        this.model= AIConfig.getModelId();
        this.messages=messages;
        this.stream=false;
    }

    public ChatCompletionRequest(String role, String userText) {
        this.model = AIConfig.getModelId();
        // 自动包装为单个Message的列表
        this.messages = java.util.Collections.singletonList(new Message(role,userText));
        this.stream = false;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }


    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
