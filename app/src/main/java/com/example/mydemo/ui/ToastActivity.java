package com.example.mydemo.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.R;
import com.google.android.material.snackbar.Snackbar;

/**
 * create by WUzejian on 2025/11/16
 */
public class ToastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast);

        findViewById(R.id.normal_toast_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ToastActivity.this, "普通Toast显示成功", Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.custom_toast_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomToast();
            }
        });
        findViewById(R.id.snackbar_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSnackbar(v);
            }
        });


    }

    public void showSnackbar(View view){
        Log.d("wzj", "viewG:"+(view instanceof ViewGroup));
        Snackbar.make(view, "文件已删除", Snackbar.LENGTH_LONG)
                // 第一个参数：view ，第二个参数：按钮文本；第二个参数：点击回调
                .setAction("撤销", v -> {
                    // 执行撤销操作（如恢复文件）
                    Toast.makeText(ToastActivity.this, "已恢复文件", Toast.LENGTH_SHORT).show();
                })
                //通过 addCallback() 监听 Snackbar 的生命周期：
                .addCallback(new Snackbar.Callback(){
                    @Override
                    public void onShown(Snackbar sb) {
                        super.onShown(sb);
                        // Snackbar显示时触发（如开始加载动画）
                    }

                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        // Snackbar消失时触发（如停止加载动画）
                        // event 可判断消失原因：如用户滑动关闭、超时、点击按钮等
                    }
                })
                .show();
    }
    /**
     * 完全自定义 Toast
     */
    private void showCustomToast(){
        // 1. 获取 LayoutInflater 服务
        LayoutInflater inflater = getLayoutInflater();

        // 2. 加载自定义的布局文件
        View layout = inflater.inflate(R.layout.custom_toast_layout,null);

        // 3. 找到布局中的 TextView 并设置文本
        TextView text = layout.findViewById(R.id.textView1);
        text.setText("这是一个自定义布局的 Toast");

        // 4. 创建并显示自定义 Toast
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0); // 设置显示位置
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
