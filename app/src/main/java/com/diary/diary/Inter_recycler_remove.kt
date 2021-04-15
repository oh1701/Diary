package com.diary.diary

import android.content.Context
import android.graphics.Typeface
import android.media.Image
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView

interface Inter_recycler_remove{

    companion object{
        var companioncheck:String = "꺼짐"
    }

    fun recycler_remove(position:Int){
        tag_array.removeAt(position)
        recy.adapter?.notifyDataSetChanged() // 이걸 가장 먼저해야 remove 했을때 다음 레이아웃이 크기만큼 줄어든다
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

interface rere{

    fun dd(EditText:EditText, typeface: Typeface){
        EditText.typeface = typeface
    }
}