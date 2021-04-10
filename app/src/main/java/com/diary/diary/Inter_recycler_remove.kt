package com.diary.diary

import android.view.View
import android.widget.EditText
import android.widget.TextView

interface Inter_recycler_remove{
    
    companion object{
        var companioncheck:String = "꺼짐"
        var data:String = ""
    }

    fun visible(text: TextView, edit:EditText){
        text.visibility = View.GONE
        edit.visibility = View.VISIBLE
    }

    fun gone(text: TextView, edit:EditText) {
        text.visibility = View.VISIBLE
        edit.visibility = View.GONE
    }
    fun recycler_remove(position:Int){
        tag_array.removeAt(position)
        recy.adapter?.notifyItemRemoved(position)
        recy.adapter?.notifyItemRangeChanged(position, tag_array.size)
    }

    fun trash_get() : Int{
        return if(companioncheck == "켜짐")
            2
        else
            1
    }

    fun trash_checkd(text:String){
        companioncheck = text
    }
}