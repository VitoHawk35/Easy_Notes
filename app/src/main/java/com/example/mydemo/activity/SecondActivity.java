package com.example.mydemo.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.R;

/**
 * create by WUzejian on 2025/11/16
 */
public class SecondActivity extends AppCompatActivity {
    private static final String TAG = "SecondActivity";
    private EditText editText;
    private Button btnReturn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Log.d(TAG, "onCreate: SecondActivity is becoming visible.");
        editText = findViewById(R.id.edit_text_id);
        Button returnButton = findViewById(R.id.return_btn);
        Intent intent = getIntent();
//
        if (intent != null){
            String greeting = intent.getStringExtra("GREETING");
            Toast.makeText(this, "接收到数据：" + greeting, Toast.LENGTH_SHORT).show();
        }

//
        returnButton.setOnClickListener(v -> {
            String input = editText.getText().toString();

            // a. 创建一个空的 Intent 用于存放返回数据
            Intent returnIntent = new Intent();

            // b. 将数据放入 Intent
            returnIntent.putExtra("RETURN_DATA", input);

            // c. 设置结果码（RESULT_OK 表示成功）和返回的 Intent
            setResult(Activity.RESULT_OK, returnIntent);

            // d. 关闭当前 Activity
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: SecondActivity is becoming visible.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: SecondActivity is now in the foreground.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: SecondActivity is no longer in the foreground.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: SecondActivity is no longer visible.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: SecondActivity is being destroyed.");
    }
}
