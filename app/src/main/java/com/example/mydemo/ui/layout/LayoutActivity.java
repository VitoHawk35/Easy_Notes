package com.example.mydemo.ui.layout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.R;
import com.example.mydemo.ui.layout.action.GuideLineActivity;

/**
 * create by WUzejian on 2025/11/18
 */
public class LayoutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        Button btn = findViewById(R.id.frameLayout_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LayoutActivity.this, FrameLayoutActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.frameLayout_btn).setOnClickListener(v -> {
            Intent intent = new Intent(LayoutActivity.this, FrameLayoutActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.linearLayout_btn).setOnClickListener(v -> {
            Intent intent = new Intent(LayoutActivity.this, LinearLayoutActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.relativeLayout_btn).setOnClickListener(v -> {
            Intent intent = new Intent(LayoutActivity.this, RelativeLayoutActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.constraintLayout_btn).setOnClickListener(v -> {
            Intent intent = new Intent(LayoutActivity.this, ConstraintLayoutActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.gridLayout_btn).setOnClickListener(v -> {
            Intent intent = new Intent(LayoutActivity.this, GridLayoutActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.guideline_btn).setOnClickListener(v -> {
            Intent intent = new Intent(LayoutActivity.this, GuideLineActivity.class);
            startActivity(intent);
        });
    }
}
