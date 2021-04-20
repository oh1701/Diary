package com.diary.recycler

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diary.diary.R
import java.io.IOException

class Recycler_main(val diary_list:ArrayList<list>) :  RecyclerView.Adapter<Recycler_main.ViewHolder>(){

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Recycler_main.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_recycler_main, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Recycler_main.ViewHolder, position: Int) {
        holder.title.text = diary_list[position].title
        holder.content.text = diary_list[position].content
        var bitmap = diary_list[position].imageuri[0]
        //var uri = diary_list[position].ddd[0] uri형식으로 하면 이미지로 데이터 변환 시 시간이 걸려서 렉이 생김.
        holder.photo.setImageBitmap(bitmap)

        Log.d("냐냐냐냐", diary_list[position].imageuri[0].toString())
        //이미지 최대 2-3개까지만 하기.
    }

    override fun getItemCount(): Int {
        return diary_list.size
    }

    class ViewHolder(itemview: View) :RecyclerView.ViewHolder(itemview){
        val title = itemview.findViewById<TextView>(R.id.recycler_title)
        val content = itemview.findViewById<TextView>(R.id.recycler_text)
        val photo = itemview.findViewById<ImageView>(R.id.imagephoto)
    }
}



class list(val id:Int, val date:Long, val title:String, val content:String, val imageuri:List<Bitmap?>, val ddd:List<String?>)