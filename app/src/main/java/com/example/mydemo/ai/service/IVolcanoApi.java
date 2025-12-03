package com.example.mydemo.ai.service;

import com.example.mydemo.ai.model.Request.ChatCompletionRequest;
import com.example.mydemo.ai.model.Response.ChatCompletionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface IVolcanoApi {
    @POST("chat/completions")
    Call<ChatCompletionResponse> createChatCompletion(
            @Header("Authorization") String auth,
            @Header("Content-Type") String contentType,
            @Body ChatCompletionRequest request
    );
}
