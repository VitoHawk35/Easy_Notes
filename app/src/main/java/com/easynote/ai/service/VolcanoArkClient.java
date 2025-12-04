package com.easynote.ai.service;



import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class VolcanoArkClient {
    // 1. 静态字段：保存全局唯一实例
    private static volatile VolcanoArkClient instance;
    // 2. OkHttpClient 实例（全局复用）
    private final OkHttpClient okHttpClient;
    private final Retrofit retrofit;
    private final IVolcanoApi aiAPI;
    private String baseUrl;
    private String modelId;
    private String arkApiKey;

    private VolcanoArkClient(){
        this.baseUrl= AIConfig.getBaseUrl();
        this.modelId= AIConfig.getModelId();
        this.arkApiKey= AIConfig.getArkApiKey();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        Interceptor authInterceptor =chain -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .header("Content-Type","application/json")
                    .header("Authorization","Bearer "+arkApiKey)
                    .build();
            return chain.proceed(newRequest);
        };

        this.okHttpClient= new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60,TimeUnit.SECONDS)
                .writeTimeout(60,TimeUnit.SECONDS)
                .build();

        this.retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        aiAPI=retrofit.create(IVolcanoApi.class);
    }

    public static VolcanoArkClient getInstance() {
        if (instance == null) {
            synchronized (VolcanoArkClient.class) {
                if (instance == null) {
                    instance = new VolcanoArkClient();
                }
            }
        }
        return instance;
    }

    public IVolcanoApi getAiAPI(){
        return aiAPI;
    }

}
