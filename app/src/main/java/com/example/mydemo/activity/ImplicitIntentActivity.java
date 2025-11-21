package com.example.mydemo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.R;

/**
 * create by WUzejian on 2025/11/19
 */
public class ImplicitIntentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_implicit_intent);

        findViewById(R.id.open_page).setOnClickListener(v -> {
            // 隐式Intent通过浏览器打开一个网页
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.baidu.com"));
            startActivity(intent);
        });

        findViewById(R.id.call_phone).setOnClickListener(v -> {
            // 隐式Intent拨打电话
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:10086"));
            startActivity(intent);
        });
    }
}
