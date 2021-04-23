package com.diary.recycler

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diary.diary.*
import com.diary.diary.Content_create.Companion.metrics
import java.io.IOException

class Recycler_main(val diary_list:ArrayList<list>) :  RecyclerView.Adapter<Recycler_main.ViewHolder>(), text_font, main_recycler_view {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Recycler_main.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_recycler_main, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Recycler_main.ViewHolder, position: Int) {

        holder.title.apply {
            text = diary_list[position].title
            typeface = inter_roomdata_stringToFont(diary_list[position].font, context)
        }

        holder.content.apply {
            text = diary_list[position].content
            typeface = inter_roomdata_stringToFont(diary_list[position].font, context)
            if(diary_list[position].content.isEmpty()) {
                visibility = View.GONE
                Log.d("불러진 곳", "$position")
            }
            else{
                visibility = View.VISIBLE
            }
        }

        if(diary_list[position].imageuri.isNotEmpty()) { //이미지 uri 존재할시.
            for(i in diary_list[position].imageuri.indices){ // when과 for 사용해서 반복작업.
                when(i) {
                    0 -> {
                        holder.photo = photoimageset(holder.photo, diary_list[position].imageuri[i])
                    }
                    1 -> {
                        holder.photo2 =  photoimageset(holder.photo2, diary_list[position].imageuri[i])
                    }
                    2 -> {
                        holder.photo3 = photoimageset(holder.photo3, diary_list[position].imageuri[i])
                    }
                    3 -> {
                        holder.photo4 =  photoimageset(holder.photo4, diary_list[position].imageuri[i])
                    }
                    else ->  break // 설정한 이미지 갯수인 4개보다 많을시, 추가 등록하지 않고 반복 멈추기.

                }
            }
        }

        holder.layout.setOnClickListener {
            Log.d("제목은 :", holder.title.text.toString())
            var intent = Intent(context, Content_create::class.java)
            intent.putExtra("이동", diary_list[position].id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return diary_list.size
    }

    class ViewHolder(itemview: View) :RecyclerView.ViewHolder(itemview){
        val title = itemview.findViewById<TextView>(R.id.recycler_title)
        val content = itemview.findViewById<TextView>(R.id.recycler_text)
        var photo = itemview.findViewById<ImageView>(R.id.imagephoto)
        var photo2 = itemview.findViewById<ImageView>(R.id.imagephoto2)
        var photo3 = itemview.findViewById<ImageView>(R.id.imagephoto3)
        var photo4 = itemview.findViewById<ImageView>(R.id.imagephoto4)
        val layout = itemview.findViewById<ConstraintLayout>(R.id.layout_main)
        val date_color = itemview.findViewById<ImageView>(R.id.date_color)
    }
}



class list(val id:Int, val title:String, val content:String, val imageuri:List<String?>, val font: String)