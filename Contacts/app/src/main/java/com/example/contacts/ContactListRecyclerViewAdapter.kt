package com.example.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.contact.view.*

class ContactListRecyclerViewAdapter(private val values: List<Contact>) :
    RecyclerView.Adapter<ContactListRecyclerViewAdapter.ViewHolder>() {

    var onClickListener: ((Contact) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.contact,
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
            name.text = item.name
            phone.text = item.phoneNumber
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.name
        val phone: TextView = view.phone
    }
}