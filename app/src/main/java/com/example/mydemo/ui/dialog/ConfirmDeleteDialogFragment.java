package com.example.mydemo.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * create by WUzejian on 2025/11/17
 * DialogFragment 实现确认删除弹窗demo
 */
public class ConfirmDeleteDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 使用 Builder 类来方便地构建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("确认删除")
                .setMessage("你确定要删除这条记录吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 通知 Activity 执行删除操作
                        // ((MyActivity) getActivity()).doPositiveClick();
                        Toast.makeText(getActivity(), "删除操作已确认", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 用户取消了对话框
                        ConfirmDeleteDialogFragment.this.getDialog().cancel();
                        Toast.makeText(getActivity(), "删除操作已取消", Toast.LENGTH_SHORT).show();
                    }
                });
        // 创建 AlertDialog 对象并返回
        return builder.create();
    }
}
