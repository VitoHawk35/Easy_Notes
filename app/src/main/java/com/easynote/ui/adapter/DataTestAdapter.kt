package com.easynote.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.easynote.data.relation.NoteWithTags
import com.example.mydemo.R

class DataTestAdapter(diffCallback: DiffUtil.ItemCallback<NoteWithTags>) :
    PagingDataAdapter<NoteWithTags, DataTestAdapter.MyViewHolder>(diffCallback) {
    object DiffCallback : DiffUtil.ItemCallback<NoteWithTags>() {
        override fun areItemsTheSame(oldItem: NoteWithTags, newItem: NoteWithTags) =
            oldItem.noteEntity?.id == newItem.noteEntity?.id

        override fun areContentsTheSame(oldItem: NoteWithTags, newItem: NoteWithTags) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv_title = itemView.findViewById<TextView>(R.id.tv_title)
        private val tv_content = itemView.findViewById<TextView>(R.id.tv_content)
        private val tv_time = itemView.findViewById<TextView>(R.id.tv_time)
        fun bind(item: NoteWithTags?) {
            tv_title.text = item?.noteEntity?.title ?: ""
            tv_content.text = item?.noteEntity?.content ?: ""
            tv_time.text = item?.noteEntity?.createTime?.toString() ?: ""
        }
    }

}