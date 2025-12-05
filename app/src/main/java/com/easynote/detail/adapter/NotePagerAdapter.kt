package com.easynote.detail.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.detail.data.model.NotePage


import com.easynote.richtext.view.RichTextView
import com.easynote.ai.core.TaskType

class NotePagerAdapter(
    private val pages: MutableList<NotePage>,
    private val addImage: (callback: (Uri) -> Unit) -> Unit,
    private val save: (Int, String)->Unit,
    private val onAiRequest: (String, TaskType, String?, (String) -> Unit) -> Unit,
    private val onUpdateAbstract: (String) -> Unit
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
        holder.richEditor.html = page.content

        holder.richEditor.setReadOnly(isReadOnly)

        holder.richEditor.setOnRichTextListener(object : RichTextView.OnRichTextListener {

            override fun onSave(html: String) {
                val currentPos = holder.bindingAdapterPosition

                if (currentPos != RecyclerView.NO_POSITION) {
                    save(currentPos, html)
                }

            }

            override fun onInsertImageRequest() {
                addImage { uri ->

                    holder.richEditor.insertImage(uri)
                }
            }

            override fun onContentChanged(html: String) {
                page.content = html
            }

            override fun onAIRequest(text: String, taskType: TaskType, context: String?, onResult: (String) -> Unit) {
                onAiRequest(text, taskType, context, onResult)
            }

            override fun onUpdateAbstract(abstract: String) {
                this@NotePagerAdapter.onUpdateAbstract(abstract)
            }
        })
    }

    override fun getItemCount(): Int = pages.size

    fun setReadOnlyMode(isReadOnly: Boolean) {
        this.isReadOnly = isReadOnly
        notifyDataSetChanged()
    }
}