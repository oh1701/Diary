package com.diary.recycler

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.diary.diary.Inter_recycler_remove
import com.diary.diary.R

class Recycler_tag(val tagline:ArrayList<tagline>): RecyclerView.Adapter<Recycler_tag.ViewHolder>(), Inter_recycler_remove {
    lateinit var context:Context
    var change = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Recycler_tag.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_tag_layout, parent, false)
        context = parent.context

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Recycler_tag.ViewHolder, position: Int) {
        if(trash_get() == 2) {
            holder.cancel.visibility = View.VISIBLE
        }
        else{
            holder.cancel.visibility = View.GONE
        }

        holder.tag_tag.text = tagline[position].tag_tag
        holder.tag_content.text = tagline[position].tag_content
        holder.cancel.setOnClickListener { //visibility를 mvvm패턴으로 확인한다.
            if(trash_get() == 2) {
                recycler_remove(position)
            }
        }

        Log.d("화악인", position.toString())

        //태그 버튼 누르면 나타나는 휴지통 버튼. 태그 상태에서 휴지통 버튼 눌렀을 때, 삭제하고 싶은 태그버튼 누르면 삭제.
        /*holder.btn.setOnClickListener {
            recycler_remove(position) // 버튼 누를시 포지션 값에 맞춰서 삭제.
        }*/

    }

    override fun getItemCount(): Int {
        return tagline.size
    }

    class ViewHolder(itemview: View):RecyclerView.ViewHolder(itemview){
        var tag_tag = itemview.findViewById<TextView>(R.id.tag_tag)
        var tag_content = itemview.findViewById<TextView>(R.id.tag_content)
        var layout = itemview.findViewById<FrameLayout>(R.id.copy_layout)
        var cancel = itemview.findViewById<ImageButton>(R.id.remove_btn)
        //var btn = itemview.findViewById<Button>(R.id.btn)
    }
}

class tagline(val tag_tag:String, val tag_content:String)
/*
*


        holder.tag_edit.setOnEditorActionListener { textView, action, event ->
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) { //inputtype text의 완료버튼 선택 시 이벤트.
                val inputmethodservice = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputmethodservice.hideSoftInputFromWindow( holder.tag_edit.windowToken, 0)
                inputmethodservice
                if (holder.tag_edit.text.isNotEmpty()) { //값이 있으면 add, 없으면 add안함.
                    tag_array.add(tagline("# ",  holder.tag_edit.text.toString()))
                    tag_parent.adapter?.notifyDataSetChanged()

                }
                handled = true
            }
            handled
        }
            *
            *
        var tag_tag = itemview.findViewById<TextView>(R.id.tag_tag)
        var tag_content = itemview.findViewById<TextView>(R.id.tag_content)
        var tag_edit = itemview.findViewById<EditText>(R.id.tag_edit)

        var layout = itemview.findViewById<LinearLayout>(R.id.copy_layout)
        //var btn = itemview.findViewById<Button>(R.id.btn)
* */