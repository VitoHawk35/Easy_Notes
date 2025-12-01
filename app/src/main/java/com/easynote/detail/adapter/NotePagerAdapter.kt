package com.easynote.detail.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.detail.data.model.NotePage // 引用刚才建的数据模型


import com.easynote.richtext.view.RichTextView
import com.example.mydemo.ai.core.TaskType

class NotePagerAdapter(
    private val pages: MutableList<NotePage>,
    private val addImage: (callback: (Uri) -> Unit) -> Unit,
    private val save: (Int, String)->Unit,
    private val onAiRequest: (String, TaskType, (String) -> Unit) -> Unit // 新参数
) : RecyclerView.Adapter<NotePagerAdapter.PageViewHolder>() {

    private var isReadOnly: Boolean = true

    inner class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val richEditor: RichTextView = view.findViewById(R.id.detail_paper_content)
//        val tvPageNum: TextView = view.findViewById(R.id.tvPageNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_item_note_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]
        // 获取内容
        holder.richEditor.html = page.content

        // 2. 设置只读状态
        holder.richEditor.setReadOnly(isReadOnly)

        // 3. 设置统一监听器 (连接 View 和 Activity)
        holder.richEditor.setOnRichTextListener(object : RichTextView.OnRichTextListener {

            override fun onSave(html: String) {
                //修复：获取当前实时的位置，而不是使用过时的 position 参数
                val currentPos = holder.bindingAdapterPosition

                // 必须检查是否有效（防止极少数情况下 View 已经被移除但还在执行动画时点击）
                if (currentPos != RecyclerView.NO_POSITION) {
                    // 将保存事件转发给 Activity，并附带当前页码位置
                    save(currentPos, html)
                }

            }

            override fun onInsertImageRequest() {
                addImage { uri ->

                    holder.richEditor.insertImage(uri)
                }
            }

            override fun onContentChanged(html: String) {
                // 实时更新数据模型，防止划走后数据丢失
                page.content = html
            }

            override fun onAIRequest(text: String, taskType: TaskType, onResult: (String) -> Unit) {
                // 转发给 Activity
                onAiRequest(text, taskType, onResult)
            }
        })
    }

    override fun getItemCount(): Int = pages.size

    fun setReadOnlyMode(isReadOnly: Boolean) {
        this.isReadOnly = isReadOnly
        notifyDataSetChanged()
    }
}