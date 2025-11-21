package com.example.mydemo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.R;

/**
 * create by WUzejian on 2025/10/10
 */
public class MainActivityLifecycle extends AppCompatActivity {

    private static final String TAG = "MainActivityLifecycle6666";
    private TextView resultTextView;
    private Button myButton;

    // 1. 注册 ActivityResultLauncher
    private final ActivityResultLauncher<Intent> secondActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), // 使用预设的 Contract
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onActivityResult(ActivityResult result) {
                    // 4. 在回调中处理返回结果
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String resultData = data.getStringExtra("RETURN_DATA");
                            resultTextView.setText("收到返回数据：" + resultData);
                        }
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: MainActivityLifecycle is being created.");
        myButton = findViewById(R.id.myButton);
        resultTextView = findViewById(R.id.result_id);
        myButton.setOnClickListener(v -> {
            // 创建一个显式 Intent
            // 参数一：上下文（通常是当前 MainActivityLifecycle.this）
            // 参数二：目标 MainActivityLifecycle 的 Class 对象
            Intent intent = new Intent(MainActivityLifecycle.this, SecondActivity.class);
            intent.putExtra("GREETING", "Hello from MainActivity!");//传递数据
//            startActivity(intent);
            // 启动目标 MainActivityLifecycle
            secondActivityLauncher.launch(intent);
        });

        findViewById(R.id.intent_demo).setOnClickListener(v -> {
            // 隐式Intent演示
            Intent intent = new Intent(this, ImplicitIntentActivity.class);
            startActivity(intent);
        });
    }

    //onCreate: MainActivityLifecycle is being created.
    //2025-11-20 19:25:45.588 27426-27426 MainActivi...ecycle6666 com.example.mydemo                   D  onStart: MainActivityLifecycle is becoming visible.
    //2025-11-20 19:25:45.590 27426-27426 MainActivi...ecycle6666 com.example.mydemo                   D  onResume: MainActivityLifecycle is in the foreground and interactive.
    //2025-11-20 19:26:19.282 27426-27426 MainActivi...ecycle6666 com.example.mydemo                   D  onPause: MainActivityLifecycle is losing focus.
    //2025-11-20 19:26:19.355 27426-27426 MainActivi...ecycle6666 com.example.mydemo                   D  onStop: MainActivityLifecycle is no longer visible.
    //2025-11-20 19:26:19.357 27426-27426 MainActivi...ecycle6666 com.example.mydemo                   D  onDestroy: MainActivityLifecycle is being destroyed.
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: MainActivityLifecycle is becoming visible.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: MainActivityLifecycle is in the foreground and interactive.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: MainActivityLifecycle is losing focus.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: MainActivityLifecycle is no longer visible.");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: MainActivityLifecycle is being restarted.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: MainActivityLifecycle is being destroyed.");
    }
}