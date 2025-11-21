package com.example.mydemo.ui.recyclerview.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydemo.R;
import com.example.mydemo.ui.recyclerview.bean.UserBean;
import com.example.mydemo.ui.recyclerview.viewholder.UserViewHolder;

import java.util.List;

/**
 * create by WUzejian on 2025/11/17
 */
public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {
    private List<UserBean> mUserList;

    // 构造方法：接收数据集
    public UserAdapter(List<UserBean> userList) {
        this.mUserList = userList;
    }

    /**
     * 第一步：Item创建ViewHolder（加载item布局）
     *
     * @return
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_user, parent, false);
        //创建返回的ViewHolder
        return new UserViewHolder(itemRootView);
    }

    /**
     * // 第二步：绑定数据到ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        //绑定数据
        //当前位置的数据
        UserBean userBean = mUserList.get(position);
        // 调用ViewHolder的bindData方法绑定数据
        holder.bindData(userBean);
    }

    @Override
    public int getItemCount() {
        return mUserList != null ? mUserList.size() : 0;
    }

    // 数据更新（后续刷新列表用）
    public void updateData(List<UserBean> newUserList) {
        this.mUserList = newUserList;
        // 通知RecyclerView数据已变更
        notifyDataSetChanged();
    }
}
