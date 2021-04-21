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
import com.diary.diary.Content_create
import com.diary.diary.R
import com.diary.diary.text_font
import java.io.IOException

class Recycler_main(val diary_list:ArrayList<list>) :  RecyclerView.Adapter<Recycler_main.ViewHolder>(), text_font {

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

        }

        holder.photo.apply {
            if(diary_list[position].imageuri.isNotEmpty()) {
                var bitmap = diary_list[position].imageuri[0] //0번째 사진 붙여넣기. 리사이클러뷰에서 하는게 렉 가장 안걸린다.
                setImageURI(Uri.parse(bitmap))
            }

            setOnClickListener {

            }
        }

        holder.layout.setOnClickListener {
            Log.d("제목은 :", holder.title.text.toString())
            var intent = Intent(context, Content_create::class.java)
            intent.putExtra("ID", diary_list[position].id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return diary_list.size
    }

    class ViewHolder(itemview: View) :RecyclerView.ViewHolder(itemview){
        val title = itemview.findViewById<TextView>(R.id.recycler_title)
        val content = itemview.findViewById<TextView>(R.id.recycler_text)
        val photo = itemview.findViewById<ImageView>(R.id.imagephoto)
        val layout = itemview.findViewById<ConstraintLayout>(R.id.layout_main)
    }
}



class list(val id:Int, val date:Long, val title:String, val content:String, val imageuri:List<String?>, val font: String)