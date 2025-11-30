package com.example.mydemo.ai.model;

import com.google.gson.annotations.SerializedName;



public class Message {
    @SerializedName("role")
    private String role; //角色： user/assistant/system
    @SerializedName("content")
    private String content; //消息内容

    public Message(String role,String text){
        this.role=role;
        this.content= text;
    }

    public String getRole() {
        return role;
    }


    public String getContent() {
        return content;
    }
}
