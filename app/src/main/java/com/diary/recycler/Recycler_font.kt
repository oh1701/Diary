package com.diary.recycler

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.diary.diary.R
import com.diary.diary.rere

class Recycler_font(val font_list:ArrayList<font_list>, editText: EditText): RecyclerView.Adapter<Recycler_font.ViewHolder>(), rere {
    var preview = editText
    private var btn_array: ArrayList<Button> = arrayListOf()
    private var dd_array: ArrayList<Button> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.font_recycler_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Recycler_font.ViewHolder, position: Int) {
        holder.font_btn.text = font_list[position].name
        holder.font_btn.typeface = font_list[position].font

        holder.font_btn.setOnClickListener {
            dd(preview, holder.font_btn.typeface)
        }
    }

    override fun getItemCount(): Int {
        return font_list.size
    }

    class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val font_btn = itemview.findViewById<Button>(R.id.font_recycler_btn)// 여기에 버튼 id 넣기
    }
}
class font_list(val font: Typeface, var name: String)