package com.diary.recycler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.diary.diary.*

//뷰모델 변수를 인자값으로 전달하고 온롱클릭시 변화시켜서 observe로 알려주기.

class Recycler_main(val diary_list: ArrayList<list>, val shadowText: EditText, val check:String) :  RecyclerView.Adapter<Recycler_main.ViewHolder>(), text_font, main_recycler_view, layout_remove {

    lateinit var context: Context
    var a = 0
    var position_array:ArrayList<Int> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Recycler_main.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_recycler_main, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int { //이거로 이미지 재활용문제가 해결되었음.
        return position
    }

    override fun onBindViewHolder(holder: Recycler_main.ViewHolder, position: Int) {
        holder.photo.setImageBitmap(null)
        holder.photo2.setImageBitmap(null)
        holder.photo3.setImageBitmap(null)
        holder.photo4.setImageBitmap(null)
        holder.date.setText(null)
        holder.title.setText(null)
        holder.content.setText(null)

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
//
        holder.layout.apply {

            if(check == "main") { //사용하는 것이 main부분이면
                tag = "" // notify 할 시를 생각해서 기본값으로 ""를 준다.
                setOnClickListener {
                    if (a == 0 || layout_remove().first == null) {
                        Log.d("제목은 :", holder.title.text.toString())
                        var intent = Intent(context, Content_create::class.java)
                        intent.putExtra("이동", diary_list[position].id)
                        context.startActivity(intent)
                    } else {
                        if (tag == "클릭") { //클릭상태면
                            holder.layout.setBackgroundResource(R.drawable.layout_background)
                            tag = "" //태그 대신할거 생성하기.
                            layout_remove_position_remove(position)
                            layout_add_or_remove(holder.layout, 1, diary_list[position].dateLong)
                            Log.d("여기임", "여기임11")
                        } else {
                            holder.layout.setBackgroundResource(R.drawable.longclick_layout)
                            tag = "클릭"
                            a = 1
                            layout_remove_position_check(position)
                            layout_add_or_remove(holder.layout, 0, diary_list[position].dateLong)
                            Log.d("클릭", position.toString())
                            Log.d("여기임", "1414여기임")
                        }
                    }
                }

                setOnLongClickListener { // 온롱클릭 하고 터치시 레이아웃 선택되는 이벤트. 이후 삭제버튼 누르면 삭제 다이얼로그 뜨고 확인 누를시 삭제. mvvm활용해서 간편하게.
                    shadowText.setText("")

                    if (tag == "클릭") { // 클릭 상태면
                        holder.layout.setBackgroundResource(R.drawable.layout_background)
                        tag = ""
                        layout_remove_position_remove(position)
                        layout_add_or_remove(holder.layout, 1, diary_list[position].dateLong)
                        Log.d("여기임", "여기임22")
                    } else { //클릭 상태가 아니면
                        holder.layout.setBackgroundResource(R.drawable.longclick_layout)
                        tag = "클릭"
                        a = 1
                        layout_remove_position_check(position)
                        layout_add_or_remove(holder.layout, 0, diary_list[position].dateLong)
                        Log.d("클릭", position.toString())
                        Log.d("여기임", "1515여기임")
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

        holder.date.text = diary_list[position].date +  diary_list[position].daytoweek
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