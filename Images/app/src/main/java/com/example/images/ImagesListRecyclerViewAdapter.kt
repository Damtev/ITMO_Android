package com.example.images

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.image_list_item.view.*

class ImagesListRecyclerViewAdapter(private val values: List<Image>) :
    RecyclerView.Adapter<ImagesListRecyclerViewAdapter.ViewHolder>() {

    var onClickListener: ((Image) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.image_list_item,
            parent,
            false
        )
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            onClickListener?.invoke(values[holder.adapterPosition])
        }
        return holder
    }

    override fun getItemCount(): Int {
        return values.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.apply {
            imagePreview.setImageBitmap(item.previewBitmap)
            description.text = item.description
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagePreview: ImageView = view.image_preview
        val description: TextView = view.image_description
    }
}