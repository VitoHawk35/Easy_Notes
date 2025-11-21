package com.example.mydemo;

import  android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.activity.Demo2Activity;
import com.example.mydemo.activity.DemoActivity;
import com.example.mydemo.activity.MainActivityLifecycle;
import com.example.mydemo.data.database.SQLiteActivity;
import com.example.mydemo.data.sp.SharePreferenceActivity;
import com.example.mydemo.ui.ToastActivity;
import com.example.mydemo.ui.dialog.DialogDemoActivity;
import com.example.mydemo.ui.drawable.DrawableActivity;
import com.example.mydemo.ui.layout.LayoutActivity;
import com.example.mydemo.ui.recyclerview.RecyclerViewActivity;

/**
 * create by WUzejian on 2025/11/16
 */
public class AppMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.demo_activity_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, MainActivityLifecycle.class);
                AppMainActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.activity_toast_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, ToastActivity.class);
                AppMainActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.activity_recyclerview_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, RecyclerViewActivity.class);
                AppMainActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.activity_dialog_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, DialogDemoActivity.class);
                AppMainActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.activity_layout_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, LayoutActivity.class);
                AppMainActivity.this.startActivity(intent);
            }
        });
        findViewById(R.id.activity_sharepreference_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, SharePreferenceActivity.class);
                AppMainActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.activity_sqlite_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, SQLiteActivity.class);
                AppMainActivity.this.startActivity(intent);
            }
        });
        findViewById(R.id.activity_drawable_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, DrawableActivity.class);
                AppMainActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.activity_demo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, DemoActivity.class);
                AppMainActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.activity_demo2_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppMainActivity.this, Demo2Activity.class);
                AppMainActivity.this.startActivity(intent);
            }
        });
    }
}
