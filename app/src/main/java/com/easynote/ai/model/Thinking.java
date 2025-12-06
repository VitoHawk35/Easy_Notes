package com.easynote.ai.model;

import com.google.gson.annotations.SerializedName;

public class Thinking {
    @SerializedName("type")
    private String type;

    public Thinking(){
        this.type="disable";
    }
    public Thinking(String type){
        this.type=type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
