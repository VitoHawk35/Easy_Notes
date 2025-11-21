package com.example.mydemo.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.R;

/**
 * create by WUzejian on 2025/11/17
 */
public class DialogDemoActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        findViewById(R.id.normal_dialog_btn).setOnClickListener(this);
        findViewById(R.id.fragment_dialog_btn).setOnClickListener(this);
        findViewById(R.id.custom_dialog_btn).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.normal_dialog_btn) {
            new AlertDialog.Builder(this)
                    .setTitle("确认删除") // 设置标题
                    .setMessage("你确定要删除这条记录吗？此操作不可撤销。") // 设置内容
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 用户点击了“确定”按钮，执行删除逻辑
                            Toast.makeText(DialogDemoActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(DialogDemoActivity.this, "已取消删除", Toast.LENGTH_SHORT).show();
                        }
                    }) // 设置“取消”按钮，null 表示点击后直接关闭对话框
                    .setIcon(R.drawable.ic_warning) // 设置标题旁边的小图标
                    .show();
        } else if (v.getId() == R.id.fragment_dialog_btn) {
            new ConfirmDeleteDialogFragment().show(getSupportFragmentManager(), "confirm_delete");
        } else if (v.getId() == R.id.custom_dialog_btn) {
            showCustomDialog();
        }
    }


    // 显示自定义Dialog
    private void showCustomDialog() {
        // 创建Dialog实例
        CustomDialog dialog = new CustomDialog(this);
        // 设置标题和内容
        dialog.setTitle("删除确认");
        dialog.setContent("确定要删除这条数据吗？删除后不可恢复");
        // 可选：修改按钮文本
        dialog.setCancelText("取消");
        dialog.setConfirmText("确认");

        // 设置取消按钮监听
        dialog.setOnCancelClickListener(() -> {
            Toast.makeText(DialogDemoActivity.this, "已取消", Toast.LENGTH_SHORT).show();
        });

        // 设置确认按钮监听
        dialog.setOnConfirmClickListener(() -> {
            Toast.makeText(DialogDemoActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
            // 执行实际删除操作...
        });

        // 显示Dialog
        dialog.show();
    }
}
