<<<<<<<< HEAD:app/src/main/java/com/easynote/ai/core/IVolcanoApi.java
package com.easynote.ai.core;
========
package com.easynote.ai.service;
>>>>>>>> origin/merge-test:app/src/main/java/com/easynote/ai/service/IVolcanoApi.java

import com.easynote.ai.model.Request.ChatCompletionRequest;
import com.easynote.ai.model.Response.ChatCompletionResponse;

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
