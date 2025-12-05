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

        val rawContent = page.content.trim()

        val flatContent = rawContent.replace("\n", " ")

        holder.tvContent.text = if (flatContent.isBlank()) {
            "(空白页)"
        } else if (flatContent.length > 15) {
            "${flatContent.substring(0, 15)}..."
        } else {
            flatContent
        }
    }

    override fun getItemCount(): Int = pages.size
}