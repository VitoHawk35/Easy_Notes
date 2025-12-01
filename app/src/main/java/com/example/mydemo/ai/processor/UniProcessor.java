package com.example.mydemo.ai.processor;



import com.example.mydemo.ai.core.IVolcanoApi;
import com.example.mydemo.ai.core.TaskType;
import com.example.mydemo.ai.model.Message;
import com.example.mydemo.ai.model.Request.ChatCompletionRequest;
import com.example.mydemo.ai.model.Response.ChatCompletionResponse;
import com.example.mydemo.ai.service.AIConfig;
import com.example.mydemo.ai.service.VolcanoArkClient;
import com.example.mydemo.ai.utils.PromptBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class UniProcessor {
    private IVolcanoApi apiClient;
    public UniProcessor(){
        apiClient= VolcanoArkClient.getInstance().getAiAPI();
    }
    private String processPrompt(TaskType taskType){
        try{

            String systemPrompt = PromptBuilder.getSystemPrompt(taskType);
            return systemPrompt;

            }
        catch (Exception e) {
            throw new RuntimeException("火山引擎服务调用失败："+e.getMessage());
        }
    }


    public void processText(String userText,TaskType taskType, Callback<ChatCompletionResponse> callback) {
        //构造提示词
        String systemPrompt= processPrompt(taskType);

        //构造消息列表
        List<Message> messages =new ArrayList<>();
        messages.add(new Message("system",systemPrompt));
        messages.add(new Message("user",userText));

        //构造请求
        ChatCompletionRequest request = new ChatCompletionRequest(messages);
        String auth = "Bearer " + AIConfig.getArkApiKey();
        //调用API
        Call<ChatCompletionResponse> call = apiClient.createChatCompletion(auth,"application/json",request);

        //异步执行
        call.enqueue(callback);

    }

    /*
    private String buildPrompt(String text,TaskType taskType,String options){
        switch (taskType){
            case SUMMARY:
                return PromptBuilder.buildSummaryPrompt(text,100);
            case TRANSLATE:
                String targetLang = options!=null?options:"英文";
                return PromptBuilder.buildTranslatePrompt(text,targetLang);
            case CORRECT:
                return PromptBuilder.buildCorrectPrompt(text);
            case POLISH:
                return PromptBuilder.buildPolishPrompt(text);
            default:
                return text;
        }
    }
    */
}
