package com.example.mydemo.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.mydemo.R;
/**
 * create by WUzejian on 2025/11/17
 * 自定义样式：通过布局文件和 dialog_bg.xml 实现圆角、白色背景，避免系统默认的生硬样式。
 * 动画效果：添加从底部弹出 / 消失的动画，提升交互体验。
 * 事件回调：通过接口回调处理取消 / 确认按钮的点击事件，实现业务逻辑与 Dialog 的解耦。
 * 灵活配置：支持动态设置标题、内容、按钮文本，适应不同场景（如提示、确认、输入等）。
 * 屏幕适配：Dialog 宽度设为屏幕的 80%，在不同尺寸设备上显示更协调。
 */
public class CustomDialog extends Dialog {

    // 控件
    private TextView tvTitle;
    private TextView tvContent;
    private Button btnCancel;
    private Button btnConfirm;

    // 文本内容
    private String title;
    private String content;
    private String cancelText;
    private String confirmText;

    // 点击事件回调接口
    private OnCancelClickListener cancelListener;
    private OnConfirmClickListener confirmListener;


    // 构造方法：必须传入Context
    public CustomDialog(Context context) {
        // 引用自定义主题（去除标题栏、设置动画）
        super(context, R.style.CustomDialogStyle);
    }

    // 设置标题
    public void setTitle(String title) {
        this.title = title;
    }

    // 设置内容
    public void setContent(String content) {
        this.content = content;
    }

    // 设置取消按钮文本
    public void setCancelText(String text) {
        this.cancelText = text;
    }

    // 设置确认按钮文本
    public void setConfirmText(String text) {
        this.confirmText = text;
    }

    // 取消按钮点击回调
    public interface OnCancelClickListener {
        void onCancel();
    }

    // 确认按钮点击回调
    public interface OnConfirmClickListener {
        void onConfirm();
    }

    // 设置取消监听
    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.cancelListener = listener;
    }

    // 设置确认监听
    public void setOnConfirmClickListener(OnConfirmClickListener listener) {
        this.confirmListener = listener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去除默认标题栏（必须在setContentView前调用）
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 加载布局
        setContentView(R.layout.dialog_custom);
        // 初始化控件
        initView();
        // 设置点击事件
        initEvent();
        // 设置Dialog宽度（占屏幕80%）
        setDialogWidth();
    }

    // 初始化控件
    private void initView() {
        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);

        // 设置文本（如果有自定义文本则替换默认值）
        if (title != null) {
            tvTitle.setText(title);
        }
        if (content != null) {
            tvContent.setText(content);
        }
        if (cancelText != null) {
            btnCancel.setText(cancelText);
        }
        if (confirmText != null) {
            btnConfirm.setText(confirmText);
        }
    }

    // 初始化点击事件
    private void initEvent() {
        // 取消按钮
        btnCancel.setOnClickListener(v -> {
            if (cancelListener != null) {
                cancelListener.onCancel();
            }
            dismiss(); // 关闭Dialog
        });

        // 确认按钮
        btnConfirm.setOnClickListener(v -> {
            if (confirmListener != null) {
                confirmListener.onConfirm();
            }
            dismiss(); // 关闭Dialog
        });
    }

    // 设置Dialog宽度为屏幕宽度的80%
    private void setDialogWidth() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
            window.setAttributes(params);
        }
    }
}
