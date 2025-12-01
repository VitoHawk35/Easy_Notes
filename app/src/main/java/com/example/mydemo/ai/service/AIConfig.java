package com.example.mydemo.ai.service;

import android.content.Context;
import android.content.res.Resources;

import com.easynote.R;


public class AIConfig {
    private static String BASE_URL;
    private static String MODEL_ID;
    private static String ARK_API_KEY;
    public static boolean ENABLE_RETRY = true;
    public static int MAX_RETRY_COUNT = 3;
    public static int RETRY_DELAY= 1000;
    public static int SUMMARY_MAX_LENGTH = 100;

    public static void init(Context context){
        try{
            Resources resources=context.getResources();

            BASE_URL=resources.getString(R.string.valcano_base_url);
            MODEL_ID=resources.getString(R.string.valcano_model_id);
            ARK_API_KEY=resources.getString(R.string.valcano_ark_api_key);
            if(BASE_URL.isEmpty()||MODEL_ID.isEmpty()||ARK_API_KEY.isEmpty())
            {
                throw new RuntimeException("Volcano配置为空！请检查res/values/string.xml");
            }
        }catch(Exception e){
            throw new RuntimeException("Volcano初始化失败："+e.getMessage());
        }
    }

    public static String getBaseUrl(){
        checkInit();
        return BASE_URL;
    }

    public static String getModelId(){
        checkInit();
        return MODEL_ID;
    }

    public static String getArkApiKey(){
        checkInit();
        return ARK_API_KEY;
    }

    //检查初始化
    private static void checkInit(){
        if(BASE_URL==null||ARK_API_KEY==null||MODEL_ID==null)
        {
            throw new RuntimeException("Volcano未初始化！请先调用init（Context)方法");
        }
    }
}
