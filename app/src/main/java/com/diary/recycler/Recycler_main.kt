package com.diary.recycler

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.diary.diary.*

//뷰모델 변수를 인자값으로 전달하고 온롱클릭시 변화시켜서 observe로 알려주기.

class Recycler_main(val diary_list: ArrayList<list>, val shadowText: EditText, val check:String) :  RecyclerView.Adapter<Recycler_main.ViewHolder>(), text_font, main_recycler_view, layout_remove {

    lateinit var context: Context
    var a = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Recycler_main.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_recycler_main, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: Recycler_main.ViewHolder, position: Int) {
        holder.photo.apply{
            holder.photo.setImageBitmap(null)
            visibility = View.GONE
        } //초기화 안해주면 이상하게 나옴. 이유 모르겠음.
        holder.photo2.apply{
            holder.photo2.setImageBitmap(null)
            visibility = View.GONE
        }
        holder.photo3.apply{
            holder.photo3.setImageBitmap(null)
            visibility = View.GONE
        }
        holder.photo4.apply{
            holder.photo4.setImageBitmap(null)
            visibility = View.GONE
        }

        holder.date.setText(null)
        holder.title.setText(null)
        holder.content.setText(null)

        holder.title.apply {
            text = diary_list[position].title
            typeface = inter_roomdata_stringToFont(diary_list[position].font, context)
            if(darkmode() == "다크모드")
                setTextColor(Color.WHITE)
            else
                setTextColor(Color.BLACK)

        }

        holder.content.apply {
            text = diary_list[position].content
            typeface = inter_roomdata_stringToFont(diary_list[position].font, context)
            if(darkmode() == "다크모드")
                setTextColor(Color.WHITE)
            else
                setTextColor(Color.parseColor("#5A5858"))

            if(diary_list[position].content.isEmpty()) {
                visibility = View.GONE
                Log.d("불러진 곳", "$position")
            }
            else{
                visibility = View.VISIBLE
            }
        }

        if(diary_list[position].imageuri.isNotEmpty()) { //OnBindViewHolder에서 이미지 uri 존재할시 포지션 photo에 uri를 넣어주는 코드
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

        holder.layout.apply {
            if(darkmode() == "다크모드" && tag == null)
                holder.layout.setBackgroundResource(R.drawable.darkmode_layout)
            else
                holder.layout.setBackgroundResource(R.drawable.layout_background)


            if(check == "main") { //사용하는 것이 main부분이면
                setOnClickListener {
                    if(tag_get(position) == "클릭") //태그를 통해 삭제 선택을 하였는지 확인한다.
                        tag = "클릭"
                    else if(tag_get(position) == null)
                        tag = "1414"

                    if (a == 0 || layout_remove().first == null && MainActivity.diary_btn_change == 0) {
                        Log.d("제목은 :", holder.title.text.toString())
                        var intent = Intent(context, Content_create::class.java)
                        intent.putExtra("이동", diary_list[position].id)
                        context.startActivity(intent)
                    } else {
                        if(darkmode() == "다크모드") { //다크모드일시
                            if (tag == "클릭") { //클릭 상태
                                Log.d("다크클릭 태그", tag.toString())
                                holder.layout.setBackgroundResource(R.drawable.darkmode_layout)
                                layout_remove_position_remove(position)
                                layout_add_or_remove(holder.layout, 1, diary_list[position].dateLong, position)
                            } else {
                                Log.d("다크미클릭 태그", tag.toString())
                                holder.layout.setBackgroundResource(R.drawable.layout_background)
                                a = 1
                                layout_remove_position_check(position)
                                layout_add_or_remove(holder.layout, 0, diary_list[position].dateLong, position)
                                Log.d("클릭", position.toString())
                            }
                        }
                        else if(darkmode() == null){ //기본모드
                            if (tag == "클릭") { //클릭
                                Log.d("기본클릭 태그", tag.toString())
                                holder.layout.setBackgroundResource(R.drawable.layout_background)
                                layout_remove_position_remove(position)
                                layout_add_or_remove(holder.layout, 1, diary_list[position].dateLong, position)
                            } else {
                                Log.d("기본미클릭 태그", tag.toString())
                                holder.layout.setBackgroundResource(R.drawable.longclick_layout)
                                a = 1
                                layout_remove_position_check(position)
                                layout_add_or_remove(holder.layout, 0, diary_list[position].dateLong, position)
                                Log.d("클릭", position.toString())
                            }
                        }
                    }
                }

                setOnLongClickListener { // 온롱클릭 하고 터치시 레이아웃 선택되는 이벤트.
                    shadowText.setText("")

                    if(tag_get(position) == "클릭")
                        tag = "클릭"
                    else if(tag_get(position) == null)
                        tag = "1414"

                    if(darkmode() == "다크모드"){ //다크모드
                        if (tag == "클릭") { // 클릭 상태면
                            holder.layout.setBackgroundResource(R.drawable.darkmode_layout)
                            layout_remove_position_remove(position)
                            layout_add_or_remove(holder.layout, 1, diary_list[position].dateLong, position)
                        } else { //클릭 상태가 아니면
                            holder.layout.setBackgroundResource(R.drawable.layout_background)
                            a = 1
                            layout_remove_position_check(position)
                            layout_add_or_remove(holder.layout, 0, diary_list[position].dateLong, position)
                            Log.d("클릭", position.toString())
                        }
                    }
                    else if(darkmode() == null){ //기본 모드
                        if (tag == "클릭") { // 클릭 상태면
                            holder.layout.setBackgroundResource(R.drawable.layout_background)
                            layout_remove_position_remove(position)
                            layout_add_or_remove(holder.layout, 1, diary_list[position].dateLong, position)
                        } else { //클릭 상태가 아니면
                            holder.layout.setBackgroundResource(R.drawable.longclick_layout)
                            a = 1
                            layout_remove_position_check(position)
                            layout_add_or_remove(holder.layout, 0, diary_list[position].dateLong, position)
                            Log.d("클릭", position.toString())
                        }
                    }
                    return@setOnLongClickListener true
                }
            }
            else{ // 사용하는 부분이 search 부분이면.
                setOnClickListener {
                    var intent = Intent(context, Content_create::class.java)
                    intent.putExtra("이동", diary_list[position].id)
                    context.startActivity(intent)
                }
            }
        }

        holder.date.apply {
           text = diary_list[position].date + diary_list[position].daytoweek
            if(darkmode() == "다크모드")
                setTextColor(Color.WHITE)
            else
                setTextColor(Color.BLACK)
        }
        when(diary_list[position].daytoweek){
            "월" -> holder.date_color.setImageResource(R.drawable.circle_sunday)
            "일" -> holder.date_color.setImageResource(R.drawable.circle_monday)
            "토" -> holder.date_color.setImageResource(R.drawable.circle_tuesday)
            "금" -> holder.date_color.setImageResource(R.drawable.circle_wednesday)
            "목" -> holder.date_color.setImageResource(R.drawable.circle_thursday)
            "수" -> holder.date_color.setImageResource(R.drawable.circle_friday)
            "화" -> holder.date_color.setImageResource(R.drawable.circle_saturday)
        }
    }

    override fun getItemCount(): Int {
        return diary_list.size
    }

    class ViewHolder(itemview: View) :RecyclerView.ViewHolder(itemview){
        val title = itemview.findViewById<TextView>(R.id.recycler_title)
        val content = itemview.findViewById<TextView>(R.id.recycler_text)
        val date = itemview.findViewById<TextView>(R.id.recycler_date)
        var photo = itemview.findViewById<ImageView>(R.id.imagephoto)
        var photo2 = itemview.findViewById<ImageView>(R.id.imagephoto2)
        var photo3 = itemview.findViewById<ImageView>(R.id.imagephoto3)
        var photo4 = itemview.findViewById<ImageView>(R.id.imagephoto4)
        val layout = itemview.findViewById<ConstraintLayout>(R.id.layout_main)
        val date_color = itemview.findViewById<ImageView>(R.id.date_color)
    }
}



class list(val id:Int, val title:String, val content:String, val imageuri:List<String?>, val font: String, val date:String, val daytoweek:String, val dateLong:Long)