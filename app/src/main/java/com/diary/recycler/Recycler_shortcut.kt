package com.diary.recycler

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diary.diary.R
import com.diary.diary.Recycler_shortcut_inter
import com.diary.diary.Setting

class Recycler_shortcut(val font_shortcut:ArrayList<font_shrotcut>, val observetext: EditText): RecyclerView.Adapter<Recycler_shortcut.ViewHolder>(), Recycler_shortcut_inter {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Recycler_shortcut.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shortcut_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return font_shortcut.size
    }

    override fun onBindViewHolder(holder: Recycler_shortcut.ViewHolder, position: Int) {
        holder.text.apply {
            text = "@${font_shortcut[position].title}@"
            when (Setting.darkmodechagend) {
                "ON" -> setTextColor(Color.WHITE)
                "OFF" -> setTextColor(Color.BLACK)
            }
        }

        holder.image.apply {
            when (Setting.darkmodechagend) {
                "ON" -> setImageResource(R.drawable.darkmode_radiobtn)
                "OFF" -> setImageResource(R.drawable.ic_baseline_radio_button_unchecked_24)
            }
        }

        holder.layout.apply{
            setOnClickListener {
                Log.d("어레이", font_shortcut[position].arrayString.toString())
                shortcut_layoutclick_inter(font_shortcut[position].title, font_shortcut[position].mystring, font_shortcut[position].arrayString, font_shortcut[position].id, position)
                if (font_shortcut[position].arrayString?.isNotEmpty() == true){ //리스트 전달값이 존재하면 (폰트 리사이클러뷰 누른거면)
                    observetext.setText("폰트")
                }
                else if(font_shortcut[position].mystring != null) {
                    observetext.setText("문자")
                }
            }
        }

        Log.d("어레이", font_shortcut[position].arrayString.toString())
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val text = itemView.findViewById<TextView>(R.id.recycler_textfont_text)
        val layout = itemView.findViewById<LinearLayout>(R.id.recycler_textfont_layout)
        val image = itemView.findViewById<ImageView>(R.id.recycler_textfont_image1)
    }
}

class font_shrotcut(val id:Int, val title:String, val arrayString:List<String>?, val mystring:String?)