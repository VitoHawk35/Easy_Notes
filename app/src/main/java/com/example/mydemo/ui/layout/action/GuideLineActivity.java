package com.example.mydemo.ui.layout.action;

/**
 * create by WUzejian on 2025/11/18
 */

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.example.mydemo.R;

public class GuideLineActivity extends AppCompatActivity {

    private Guideline guidelineV;
    private Guideline guidelineH;
    private TextView txtVPercent;
    private SeekBar seekVertical;
    private Button btnToggleH;
    private ConstraintLayout root;

    private static final float H_LOW = 0.40f;
    private static final float H_HIGH = 0.60f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guideline_layout);

        root = findViewById(R.id.root);
        guidelineV = findViewById(R.id.guideline_v);
        guidelineH = findViewById(R.id.guideline_h);
        txtVPercent = findViewById(R.id.txt_v_percent);
        seekVertical = findViewById(R.id.seek_vertical);
        btnToggleH = findViewById(R.id.btn_toggle_h);

        // 初始化 SeekBar 和文本显示，依据当前垂直 Guideline 百分比
        ConstraintLayout.LayoutParams lpV = (ConstraintLayout.LayoutParams) guidelineV.getLayoutParams();
        float vPercent = lpV.guidePercent;
        if (Float.isNaN(vPercent) || vPercent <= 0f) {
            vPercent = 0.30f; // 兜底
        }
        updateVerticalPercentText(vPercent);
        // SeekBar 映射 [10..90]% 到 progress [0..80]
        int progress = Math.round(vPercent * 100f) - 10;
        if (progress < 0) progress = 0;
        if (progress > 80) progress = 80;
        seekVertical.setProgress(progress);

        seekVertical.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int p, boolean fromUser) {
                float newPercent = (p + 10) / 100f; // 0.10 .. 0.90
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineV.getLayoutParams();
                params.guidePercent = newPercent;
                guidelineV.setLayoutParams(params);
                updateVerticalPercentText(newPercent);
                root.requestLayout();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnToggleH.setOnClickListener(v -> {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineH.getLayoutParams();
            float current = params.guidePercent;
            if (Float.isNaN(current) || current <= 0f) {
                current = H_LOW;
            }
            params.guidePercent = (current < ((H_LOW + H_HIGH) / 2f)) ? H_HIGH : H_LOW;
            guidelineH.setLayoutParams(params);
            root.requestLayout();
        });
    }

    private void updateVerticalPercentText(float percent) {
        int p = Math.round(percent * 100f);
        txtVPercent.setText("垂直指引线: " + p + "%");
    }
}
