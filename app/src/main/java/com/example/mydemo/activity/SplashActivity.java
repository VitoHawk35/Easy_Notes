package com.example.mydemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.AppMainActivity;
import com.example.mydemo.R;

/**
 * create by WUzejian on 2025/11/20
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // 模拟启动延迟
        new Handler().postDelayed(() -> {
            // 启动主界面
            Intent intent = new Intent(SplashActivity.this, AppMainActivity.class);
            startActivity(intent);
            // 关闭启动页
            finish();
        }, 2000); // 2秒延迟
    }


}
