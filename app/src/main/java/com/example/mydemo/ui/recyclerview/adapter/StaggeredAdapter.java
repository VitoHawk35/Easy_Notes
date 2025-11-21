package com.example.mydemo.ui.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydemo.R;
import com.example.mydemo.ui.recyclerview.viewholder.ImageViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * create by WUzejian on 2025/11/17
 */
public class StaggeredAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private LayoutInflater mInflater;
    private int[] imgIds;
    private List<Integer> heights;

    public StaggeredAdapter(Context context, int[] imgIds) {
        this.imgIds = imgIds;
        mInflater = LayoutInflater.from(context);
        this.heights = new ArrayList<>();
        for (int i = 0; i < imgIds.length; i++) {
            this.heights.add((int) (300 + Math.random() * 300));// ignore_security_alert [ByDesign7.4]WeakPRNG
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        lp.height = heights.get(position);
        holder.itemView.setLayoutParams(lp);
        holder.imageView.setBackgroundResource(imgIds[position % imgIds.length]);
    }

    @Override
    public int getItemCount() {
        return imgIds.length;
    }
}
