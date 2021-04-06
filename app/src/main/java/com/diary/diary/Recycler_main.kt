package com.diary.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Recycler_main(val diary_list:ArrayList<list>) :  RecyclerView.Adapter<Recycler_main.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Recycler_main.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_recycler_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Recycler_main.ViewHolder, position: Int) {
        holder.title.text = diary_list[position].title
        holder.content.text = diary_list[position].content
    }

    override fun getItemCount(): Int {
        return diary_list.size
    }

    class ViewHolder(itemview: View) :RecyclerView.ViewHolder(itemview){
        val title = itemview.findViewById<TextView>(R.id.recycler_title)
        val content = itemview.findViewById<TextView>(R.id.recycler_text)
    }
}

class list(val id:Int, val date:Long, val title:String, val content:String)