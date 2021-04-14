package com.diary.recycler

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.diary.diary.R

class Recycler_font(val font_list:ArrayList<font_list>): RecyclerView.Adapter<Recycler_font.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.font_recycler_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Recycler_font.ViewHolder, position: Int) {
        //여기 font_btn에 포지션에 맞는 font타입으로 텍스트, 폰트 지정해서 설정해주기. 클릭시 해당 폰트 observe 해서 가져가거나 콜백해서 가져가기.
    }

    override fun getItemCount(): Int {
        return font_list.size
    }

    class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val font_btn = itemview.findViewById<Button>()// 여기에 버튼 id 넣기
    }
}
class font_list(val font: Typeface)