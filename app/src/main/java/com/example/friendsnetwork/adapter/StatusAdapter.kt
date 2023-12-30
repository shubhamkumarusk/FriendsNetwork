package com.example.friendsnetwork.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.friendsnetwork.R

class StatusAdapter(private val list:List<String>):RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {
    class StatusViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val username = itemView.findViewById<TextView>(R.id.user_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.status_list,parent,false)
        return StatusViewHolder(view)

    }


    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val curr = list[position]
        holder.username.text = curr

    }
}