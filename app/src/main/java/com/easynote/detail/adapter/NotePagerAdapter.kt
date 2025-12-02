package com.easynote.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.detail.data.model.NotePage

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
            holder.etContent.isEnabled = false
        } else {
            holder.etContent.isEnabled = true
        }

        holder.etContent.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                page.content = holder.etContent.text.toString()
            }
        }
    }

    override fun getItemCount(): Int = pages.size


    fun setReadOnlyMode(isReadOnly: Boolean) {
        this.isReadOnly = isReadOnly

        notifyDataSetChanged()
    }
}