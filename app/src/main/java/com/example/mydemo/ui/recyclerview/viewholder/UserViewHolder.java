package com.example.mydemo.ui.recyclerview.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydemo.R;
import com.example.mydemo.ui.recyclerview.bean.UserBean;

/**
 * create by WUzejian on 2025/11/17
 */
public class UserViewHolder extends RecyclerView.ViewHolder {
    ImageView ivAvatar;
    TextView tvName;
    TextView tvDesc;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        // 绑定视图ID
        ivAvatar = itemView.findViewById(R.id.iv_avatar);
        tvName = itemView.findViewById(R.id.tv_name);
        tvDesc = itemView.findViewById(R.id.tv_desc);
    }

    // 绑定数据到视图
    public void bindData(UserBean userBean) {
        // 设置头像（实际开发中推荐用Glide/Picasso加载网络图片）
        ivAvatar.setImageResource(userBean.getAvatarResId());
        // 设置用户名
        tvName.setText(userBean.getName());
        // 设置简介
        tvDesc.setText(userBean.getDesc());
    }
}
