package com.easynote.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easynote.R
import com.easynote.detail.data.model.NotePage
import android.text.Html
import android.os.Build
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

        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(page.content, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(page.content)
        }

        var plainText = spanned.toString()
        if (plainText.contains("\uFFFC")) {
            plainText = plainText.replace("\uFFFC", "[图片]")
        }

        plainText = plainText.replace("\n", " ").trim()


        holder.tvContent.text = if (plainText.isBlank()) {
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