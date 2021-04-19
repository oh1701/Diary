package com.diary.recycler

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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

        fun loadBitmapFromMediaStoreBy(photoUri: Uri?): Bitmap? { //이미지 uri 비트맵형식으로 변경하기
            var image: Bitmap? = null
            Log.d("확인이이여여", "확인00")
            try {
                Log.d("확인이이여여", "확인11")
                image = if (Build.VERSION.SDK_INT > 27) { // Api 버전별 이미지 처리
                    val source: ImageDecoder.Source =
                            ImageDecoder.createSource(context.contentResolver, photoUri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    Log.d("확인이이여여", "확인22")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri!!)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return image
        }

        holder.photo.setImageBitmap(loadBitmapFromMediaStoreBy(Uri.parse(diary_list[position].imageuri[0])))
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

class list(val id:Int, val date:Long, val title:String, val content:String, val imageuri:List<String?>)