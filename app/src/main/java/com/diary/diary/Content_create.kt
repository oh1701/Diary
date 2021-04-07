package com.diary.diary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.diary.diary.databinding.ActivityContentCreateBinding
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Roommodel:ViewModel(){
    private val edittitle = MutableLiveData<String>()
    private val editcontent = MutableLiveData<String>()

    var title = MutableLiveData<String>()
    var textcontent = MutableLiveData<String>()

    fun getEdi() = edittitle
    fun getContent() = editcontent

    fun Checktext() = title // 확인버튼 누르면 변경, 옵저버가 변경을 파악하여 coroutine을 통해 room에게 전송

    fun onclick(){
        title.value = edittitle.value
        textcontent.value = editcontent.value
        Log.d("TAG", "보내진 것은 ${title.value}")
    }
}

class Content_create : AppCompatActivity() { // intent 통해서 전해진 데이터값으로 태그를 받는다. 태그값에 따라 room에 넣어지는 값도 달리하기.

    lateinit var binding:ActivityContentCreateBinding

    companion object{
        lateinit var viewModel:Roommodel
        lateinit var db:RoomdiaryDB
        val dateformat = DateTimeFormatter.ofPattern("yyyyMMdd")
        val now = LocalDateTime.now().format(dateformat).toLong()
        var titletext = ""
        var contenttext = ""
        lateinit var tag_layout:FlexboxLayout
        lateinit var toplayout:LinearLayout
        var tagfirst = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content_create)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(Roommodel::class.java)
        binding.creatediary = viewModel
            db = Room.databaseBuilder(
                    applicationContext, RoomdiaryDB::class.java,
                    "RoomDB"
            )
                    .build()
        tag_layout = binding.tagParent
        toplayout = binding.layout

        
        // 함수 불러올 공간
        observemodel()
        clickListener()
    }

    private fun observemodel(){
        viewModel.getEdi().observe(this, {
            titletext = it
        })

        viewModel.getContent().observe(this, {
            contenttext = it
        })

        viewModel.Checktext().observe(this, {
            Log.d("TAG", "값은 ${viewModel.Checktext().value}")

            if (viewModel.Checktext().value != null) {
                Log.d("TAG", "글자 바뀜")

                CoroutineScope(Dispatchers.IO).launch {
                    db.RoomDao().insertDao(Diaryroom(0, now, titletext, contenttext))
                    Log.d("TAG날짜", "${db.RoomDao().getAll()}")  //비동기처리로 Room에 데이터 처리. 만약 now가 똑같을 시 id가 작은것이 아래 리사이클러뷰 출력하게 만들기.
                }

                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun clickListener(){
        var notouch_change = 1
        var toast: Toast? = null
        
        binding.notouch.setOnClickListener { //터치 활성화 이벤트
            notouch_change *= -1
            if(notouch_change == -1) {
                binding.contentTitle.isEnabled = false
                binding.contentText.isEnabled = false

                if(toast == null) {
                    toast = Toast.makeText(this, "터치 비활성화", Toast.LENGTH_SHORT)
                }
                else{
                    toast!!.cancel()
                    toast = Toast.makeText(this, "터치 비활성화", Toast.LENGTH_SHORT)
                }
            }
            else{
                binding.contentTitle.isEnabled = true
                binding.contentText.isEnabled = true

                if(toast == null) {
                    toast = Toast.makeText(this, "터치 활성화", Toast.LENGTH_SHORT)
                }
                else{
                    toast!!.cancel()
                    toast = Toast.makeText(this, "터치 활성화", Toast.LENGTH_SHORT)
                }
            }
            toast!!.show()
        }

        binding.tag.setOnClickListener {

            var tagcontent = findViewById<TextView>(R.id.tag_content)
            var tagedit = findViewById<EditText>(R.id.tag_edit)

            var inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.tag_layout_copy, binding.tagParent, true) //아무래도 리사이클러뷰와 Flexlayout 같이 사용해야할듯.


            if(tagedit != null) {
                if (tagedit.text.isNotEmpty()) {// 빈값이 아니면 텍스트에 저장시킴
                    Toast.makeText(this, "된다", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", tagedit?.text?.length.toString())
                } else { //빈값이면 view 삭제.
                    Toast.makeText(this, "아니된다", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", tagedit.text.length.toString())
                }
            }
        }

        binding.backBtn.setOnClickListener { //X버튼 누를시
            if(viewModel.getEdi().value != null || viewModel.getContent().value != null){
                Toast.makeText(this, "내용이 있음", Toast.LENGTH_SHORT).show()
                AlertDialog.Builder(this)
                        .setTitle("내용이 존재합니다. 저장하시겠습니까?")
                        .setPositiveButton("저장") { dialog, which ->
                            CoroutineScope(Dispatchers.IO).launch {
                                db.RoomDao().insertDao(Diaryroom(0, now, titletext, contenttext))
                            }

                            startActivity(Intent(this, MainActivity::class.java))
                            Log.d("TAG", "확인")
                        }
                        .setNegativeButton("취소"){dialog, which ->
                            finish()
                        }
                        .show()
                //저장할건지 물어보고 저장한다고 하면 내용 작성 안하면 삭제.
            }
            else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}