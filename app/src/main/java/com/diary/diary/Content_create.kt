package com.diary.diary

import android.content.Intent
import android.icu.text.CaseMap
import android.icu.text.DateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.diary.diary.databinding.ActivityContentCreateBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Roommodel:ViewModel(){
    private val edittitle = MutableLiveData<String>()
    private val contenttitle = MutableLiveData<String>()

    var text = MutableLiveData<String>()

    fun getEdi() = edittitle
    fun getContent() = contenttitle

    fun Checktext() = text

    fun onclick(){
        text.value = contenttitle.value.toString()
        Log.d("TAG", "보내진 것은 ${text.value}")
    }
}

class Content_create : AppCompatActivity() {

    lateinit var binding:ActivityContentCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content_create)
        binding.lifecycleOwner = this

        var titletext = ""
        var contenttext = ""

        val dateformat = DateTimeFormatter.ofPattern("yyyyMMdd")
        val now = LocalDateTime.now().format(dateformat).toLong()

        Log.d("TAG", "$now")

        val viewModel = ViewModelProvider(this).get(Roommodel::class.java)
        binding.creatediary = viewModel

        val db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java,
                "RoomDB"
        )
                .build()


        viewModel.getEdi().observe(this, {
            titletext = it
            Log.d("TAG제목", titletext)
        })

        viewModel.getContent().observe(this, {
            contenttext = it
            Log.d("TAG내용", contenttext)
        })

        viewModel.Checktext().observe(this, {
            Log.d("TAG", "글자 바뀜")

            CoroutineScope(Dispatchers.IO).launch {
                db.RoomDao().insertDao(Diaryroom(0, now, titletext, contenttext))
                Log.d("TAG날짜", "${db.RoomDao().getAll()}")  //비동기처리로 Room에 데이터 처리. 만약 now가 똑같을 시 id가 작은것이 아래 리사이클러뷰 출력하게 만들기.
            }

            Log.d("TAG", "제목은 $titletext")
            Log.d("TAG", "내용은 $contenttext")

            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
    }
}