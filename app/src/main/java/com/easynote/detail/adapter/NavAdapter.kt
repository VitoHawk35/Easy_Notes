package com.easynote.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.detail.data.model.NotePage

class NavAdapter(
    private val pages: List<NotePage>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<NavAdapter.NavViewHolder>() {

    inner class NavViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPageNum: TextView = view.findViewById(R.id.tvNavPageNum)
        val tvContent: TextView = view.findViewById(R.id.tvNavContent)

        init {
            //点击整行触发跳转
            view.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_item_nav_list, parent, false)
        return NavViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        val page = pages[position]

        holder.tvPageNum.text = "P${position + 1}"

//        val rawContent = page.content.trim()
//
//        val flatContent = rawContent.replace("\n", " ")
//
//        holder.tvContent.text = if (flatContent.isBlank()) {
//            "(空白页)"
//        } else if (flatContent.length > 15) {
//            "${flatContent.substring(0, 15)}..."
//        } else {
//            flatContent
//        }

        // 1. 使用正则去除所有的 HTML 标签 (<p>, </div>, <b> 等)
        var plainText = page.content.replace(Regex("<[^>]*>"), "")

        // 2. 处理富文本编辑器常见的转义空格 "&nbsp;"，把它变成普通空格
        plainText = plainText.replace("&nbsp;", " ")

        // 3. 去除换行符和首尾空白
        plainText = plainText.replace("\n", " ").trim()

        // 4. 设置显示文本
        holder.tvContent.text = if (plainText.isBlank()) {
            // 优化体验：如果去掉了标签发现是空的，但原内容里有图片标签，提示“[图片]”
            if (page.content.contains("<img")) {
                "[图片]"
            } else {
                "(空白页)"
            }
        } else if (plainText.length > 15) {
            "${plainText.substring(0, 15)}..."
        } else {
            plainText
        }
    }

    override fun getItemCount(): Int = pages.size
}