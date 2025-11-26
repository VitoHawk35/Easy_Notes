package com.easynote.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.detail.data.model.NotePage // 引用刚才建的数据模型

class NotePagerAdapter(
    private val pages: MutableList<NotePage>
) : RecyclerView.Adapter<NotePagerAdapter.PageViewHolder>() {

    private var isReadOnly: Boolean = true

    inner class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val etContent: EditText = view.findViewById(R.id.detail_paper_content)
//        val tvPageNum: TextView = view.findViewById(R.id.tvPageNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_item_note_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]

//        holder.tvPageNum.text = "- ${page.pageNumber} -"

        holder.etContent.setOnFocusChangeListener(null)
        holder.etContent.setText(page.content)

        if (isReadOnly) {
            holder.etContent.isEnabled = false  // 不可用（变灰，无法点击）
            // 或者用下面这两句，保持颜色但不能点：
            // holder.etContent.isFocusable = false
            // holder.etContent.isFocusableInTouchMode = false
        } else {
            holder.etContent.isEnabled = true   // 启用
            // holder.etContent.isFocusable = true
            // holder.etContent.isFocusableInTouchMode = true
        }

        holder.etContent.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                page.content = holder.etContent.text.toString()
            }
        }
    }

    override fun getItemCount(): Int = pages.size

    // 3. 【新增】提供一个方法给 Activity 调用，用来更新状态
    fun setReadOnlyMode(isReadOnly: Boolean) {
        this.isReadOnly = isReadOnly
        // 这一句非常重要！它会强制刷新所有页面，重新执行 onBindViewHolder
        notifyDataSetChanged()
    }
}