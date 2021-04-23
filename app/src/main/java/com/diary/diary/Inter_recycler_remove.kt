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
import com.bumptech.glide.Glide
import com.diary.recycler.font_list
import java.text.DateFormat
import java.util.*

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
        return if(companioncheck == "켜짐") //쓰레기통 버튼 눌렸을 시
            2
        else
            1
    }

    fun trash_checkd(text:String){
        companioncheck = text
    }

}

interface text_font {

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
    fun photoimageset(imageView: ImageView, string:String?): ImageView{
        imageView.apply{
            var bitmap = string //각 순서에 맞는 imageuri를 넣어준다.
            clipToOutline = true // xml에서 설정한 drawable에 맞게 아웃라인 설정을 위해 true로 한다.
            Glide.with(context).load(Uri.parse(bitmap)).into(this) // 파라미터 값에 이미지 대입해줌.
            visibility = View.VISIBLE // 있을경우에만 visibility
        }
        return imageView
    }

    fun datecolorset(date:String){

        Log.d("확인", date.toString())
        var calencar = Calendar.getInstance()
    }
}