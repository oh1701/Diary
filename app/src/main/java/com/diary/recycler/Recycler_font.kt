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
import com.diary.diary.text_font

class Recycler_font(val font_list:ArrayList<font_list>, editText: EditText): RecyclerView.Adapter<Recycler_font.ViewHolder>(), text_font {
    var preview = editText
    var btn: Array<Button?> = arrayOfNulls(15)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.font_recycler_layout, parent, false)
        return ViewHolder(view)
        }


    override fun onBindViewHolder(holder: Recycler_font.ViewHolder, position: Int) {
        holder.font_btn.text = font_list[position].name
        holder.font_btn.typeface = font_list[position].font

        if(btn[position] == null)
            btn[position] = holder.font_btn

        holder.font_btn.setOnClickListener {
            for(i in btn.indices) {
                if(i == position) {
                    btn[i]?.setBackgroundResource(R.drawable.font_on) // 선택한 것은 font_on으로 바꾸기.
                    inter_font_change(preview, holder.font_btn.typeface) //선택한 typeface로 에딧텍스트 바꾸기.
                }
                else{
                    btn[i]?.setBackgroundColor(Color.parseColor("#ECE1E1")) // 나머지는 일반 배경색으로 지정.
                }
            }
        }
        //holder.font_btn.setBackgroundResource(R.drawable.font_on) //포지션 1 클릭시, 포지션 1에만 설정하고 나머지 포지션에 setbackground 하고싶음.
    }

    override fun getItemCount(): Int {
        return font_list.size
    }

    class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val font_btn = itemview.findViewById<Button>(R.id.font_recycler_btn)// inflate한 뷰에 있는 버튼
    }
}
class font_list(val font: Typeface, var name: String)