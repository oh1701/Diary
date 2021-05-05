package com.diary.diary

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.bumptech.glide.Glide
import com.diary.recycler.font_list
import java.text.DateFormat
import java.util.*

interface Inter_recycler_remove{ // 폰트 지우는 리사이클러뷰.

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
        return if(companioncheck == "켜짐") //쓰레기통 버튼 눌렸을 시
            2
        else
            1
    }

    fun trash_checkd(text:String){
        companioncheck = text
    }

}

interface layout_remove{ // 메인 리사이클러뷰에서 레이아웃 지우는 용도의 인터페이스

    companion object {
        var check: ArrayList<Int> = arrayListOf()
        var const_layout: ArrayList<ConstraintLayout> = arrayListOf()
        var date:ArrayList<Long> = arrayListOf()
        var darkmodecheck = ""
        private var tagarray:ArrayList<Int> = arrayListOf()
    }

    fun darkmodesetting(string:String){
        darkmodecheck = string
    }
    fun darkmode():String?{
        return if(darkmodecheck == "다크모드")
            "다크모드"
        else
            null
    }
    fun layout_remove_position_check(int:Int){ // if일경우 모든 companion 변수 값 지우기.
        if(int == 1024) {
            for(i in const_layout.indices){
                const_layout[i].setBackgroundResource(R.drawable.layout_background)
            }
            check.removeAll(check)
            date.removeAll(date)
            const_layout.removeAll(const_layout)
            tagarray.removeAll(tagarray)
        }
        else
            check.add(int)
    }

    fun layout_remove():Pair<ArrayList<Int>?, ArrayList<Long>?>{
        return if(check.isNotEmpty())
            Pair(check, date)
        else
            Pair(null, null)
    }

    fun tag_get(position:Int):String?{
        var a:String? = null
        if(tagarray.isNotEmpty()){
            for(i in tagarray.indices){
                if(tagarray[i] == position){
                    a = "클릭"
                    Log.d("체코", i.toString())
                    break
                }
            }
        }
        return a
    }

    fun layout_remove_position_remove(int:Int){
        if(check.isNotEmpty()){
            for(i in check.indices){
                if(check[i] == int){
                    check.removeAt(i)
                    Log.d("체크", i.toString())
                    break
                }
            }
        }
    }

    fun layout_add_or_remove(layout: ConstraintLayout, int:Int, dateLong:Long, tagposition:Int){
        if(int == 0){ // 클릭상태가 아닐경우
            const_layout.add(layout)
            date.add(dateLong)
            tagarray.add(tagposition)
        }
        else {
            if(const_layout.isNotEmpty()){
                for(i in const_layout.indices){
                    if(const_layout[i] == layout){ //레이아웃과 동일할시 해당 배열을 삭제.
                        const_layout.removeAt(i)
                        date.removeAt(i)
                        tagarray.removeAt(i)
                        break
                    }
                }
            }
        }
    }
}

interface text_font { // 폰트 변경용 인터페이스

    fun inter_font_change(EditText: EditText, typeface: Typeface) {
        EditText.typeface = typeface
    }

    fun inter_roomdata_fontToString(font: Typeface, context: Context): String {
        var d = "기본"
        when (font) {
            Typeface.DEFAULT -> d = "기본"
            Typeface.DEFAULT_BOLD -> d = "기본 두꺼움"
            context.resources.getFont(R.font.bazzi) -> d = "배찌"
            context.resources.getFont(R.font.bmeuljiro10yearslater) -> d = "을지로 10년 후"
            context.resources.getFont(R.font.cafe24ohsquareair) -> d = "카페24 아네모네 에어"
            context.resources.getFont(R.font.cafe24oneprettynight) -> d = "카페24 고운밤"
            context.resources.getFont(R.font.cafe24shiningstar) -> d = "카페24 빛나는 별"
            context.resources.getFont(R.font.cafe24ssurroundair) -> d = "카페24 써라운드 에어"
            context.resources.getFont(R.font.chosuncentennial_ttf) -> d = "조선 100년"
            context.resources.getFont(R.font.heiroflightbold) -> d = "빛의 사용자 bold"
            context.resources.getFont(R.font.heiroflightregular) -> d = "빛의 사용자 regular"
            context.resources.getFont(R.font.koreanfrenchtypewriter) -> d = "한불 정부표준 타자기"
            context.resources.getFont(R.font.mapoflower) -> d = "마포 꽃"
            context.resources.getFont(R.font.sdsamliphopangchettfbasic) -> d = "삼립 호빵"
            context.resources.getFont(R.font.yyour) -> d = "너만을 비춤"
        }
        return d
    }

    fun inter_roomdata_stringToFont(font: String, context: Context): Typeface {
        var ee: Typeface = Typeface.DEFAULT
        when (font) {
            "기본" -> ee = Typeface.DEFAULT
            "기본 두꺼움" -> ee = Typeface.DEFAULT_BOLD
            "배찌" -> ee = context.resources.getFont(R.font.bazzi)
            "을지로 10년 후" -> ee = context.resources.getFont(R.font.bmeuljiro10yearslater)
            "카페24 아네모네 에어" -> ee = context.resources.getFont(R.font.cafe24ohsquareair)
            "카페24 고운밤" -> ee = context.resources.getFont(R.font.cafe24oneprettynight)
            "카페24 빛나는 별" -> ee = context.resources.getFont(R.font.cafe24shiningstar)
            "카페24 써라운드 에어" -> ee = context.resources.getFont(R.font.cafe24ssurroundair)
            "조선 100년" -> ee = context.resources.getFont(R.font.chosuncentennial_ttf)
            "빛의 사용자 bold" -> ee = context.resources.getFont(R.font.heiroflightbold)
            "빛의 사용자 regular" -> ee = context.resources.getFont(R.font.heiroflightregular)
            "한불 정부표준 타자기" -> ee = context.resources.getFont(R.font.koreanfrenchtypewriter)
            "마포 꽃" -> ee = context.resources.getFont(R.font.mapoflower)
            "삼립 호빵" -> ee = context.resources.getFont(R.font.sdsamliphopangchettfbasic)
            "너만을 비춤" -> ee = context.resources.getFont(R.font.yyour)
        }
        return ee
    }
}

interface main_recycler_view{
    fun photoimageset(imageView: ImageView, string:String?): ImageView{ //holder.photo 와 이미지 uri를 받아온후, 설정한 이미지를 return합니다.
        imageView.apply{
            var bitmap = string //각 순서에 맞는 imageuri를 넣어준다.
            clipToOutline = true // xml에서 설정한 drawable에 맞게 아웃라인 설정을 위해 true로 한다.
            Glide.with(context).load(Uri.parse(bitmap)).into(this) // 파라미터 값에 이미지 대입해줌.
            visibility = View.VISIBLE // 있을경우에만 visibility. 기본값은 gone입니다.
        }
        return imageView
    }
}