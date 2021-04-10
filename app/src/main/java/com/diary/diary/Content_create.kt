package com.diary.diary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.diary.diary.databinding.ActivityContentCreateBinding
import com.google.android.flexbox.*
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

lateinit var recy:RecyclerView
lateinit var tag_array:ArrayList<tagline>

class Content_create: AppCompatActivity(), Inter_recycler_remove { // intent 통해서 전해진 데이터값으로 태그를 받는다. 태그값에 따라 room에 넣어지는 값도 달리하기.

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
        var tag_changed = 1
        var trash_changed = 1
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

        tag_array = arrayListOf()
        binding.FlexRecycler.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP) //가로정렬, 꽉차면 다음칸으로 넘어가게 만듬.
        binding.FlexRecycler.setHasFixedSize(true)
        binding.FlexRecycler.adapter = Recycler_tag(tag_array)

        recy = binding.FlexRecycler

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
                else{ // 토스트 삭제 후 재생성.
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
                else{ // 토스트 삭제 후 재생성.
                    toast!!.cancel()
                    toast = Toast.makeText(this, "터치 활성화", Toast.LENGTH_SHORT)
                }
            }
            toast!!.show()
        }

        binding.tag.setOnClickListener {
            tag_changed *= -1 // 태그 체인지드가 1일경우 tag넣는 공간 사라지게 만들기.
            recy.adapter?.notifyDataSetChanged() // 여기서 선언 안하면 리사이클러뷰 상태에서 remove 했을 때, 새로운 태그 생성시 자리가 남게됨 (글자 입력하면 사라지지만 가독성을 위해 만들자.)

            if(tag_changed == 1){ // 태그 버튼이 꺼짐
                binding.tagline.visibility = View.GONE
                binding.tag.setBackgroundResource(R.drawable.btn_select)
                binding.trash.setBackgroundResource(R.drawable.btn_select)

                if (binding.etn.text.isNotEmpty()) { //값이 있으면 add, 없으면 add안함.
                    tag_array.add(tagline("# ", binding.etn.text.toString())) // 태그, 작성한 입력값을 받은 텍스트값을 매개변수로 한다.
                    binding.FlexRecycler.adapter?.notifyDataSetChanged() // 추가된 데이터 새로고침하여 변경
                }

                binding.bottomLinear.visibility = View.VISIBLE
                binding.tagline.visibility = View.GONE
                binding.trash.visibility = View.GONE // 태그, 쓰레기통 버튼 꺼짐. 및 나머지 리니어 visible
            }
            else { // 태그 버튼이 켜짐
                binding.tagline.visibility = View.VISIBLE
                binding.trash.visibility = View.VISIBLE
                binding.bottomLinear.visibility = View.GONE // 태그, 쓰레기통 버튼 켜짐. 및 나머지 리니어 GONE

                binding.tag.setBackgroundResource(R.drawable.btn_on)

                binding.etn.setOnEditorActionListener { textView, action, event ->
                    var handled = false
                    if (action == EditorInfo.IME_ACTION_DONE) { //inputtype text의 완료버튼 선택 시 이벤트.
                        val inputmethodservice = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputmethodservice.hideSoftInputFromWindow(binding.etn.windowToken, 0)
                        inputmethodservice

                        if (binding.etn.text.isNotEmpty()) { //값이 있으면 add, 없으면 add안함.
                            tag_array.add(tagline("# ", binding.etn.text.toString())) // 태그, 작성한 입력값을 받은 텍스트값을 매개변수로 한다.
                            binding.FlexRecycler.adapter?.notifyDataSetChanged() // 추가된 데이터 새로고침하여 변경

                            tag_changed = 1
                            binding.tagline.visibility = View.GONE
                            binding.trash.visibility = View.GONE
                            binding.bottomLinear.visibility = View.VISIBLE // 태그, 쓰레기통 버튼 꺼짐. 및 나머지 리니어 VISIBLE

                            binding.tag.setBackgroundResource(R.drawable.btn_select)

                            binding.etn.text = null // null 값으로 설정
                        }
                        else{
                            binding.bottomLinear.visibility = View.VISIBLE
                            binding.tagline.visibility = View.GONE
                            binding.trash.visibility = View.GONE // 태그, 쓰레기통 버튼 꺼짐. 및 나머지 리니어 visible
                        }
                        handled = true


                        trash_changed = 1 // 쓰레기통버튼 off
                        if(trash_changed == 1){
                            trash_checkd("꺼짐")
                            binding.trash.setBackgroundResource(R.drawable.btn_select)
                        }
                        else{
                            trash_checkd("켜짐")
                            binding.trash.setBackgroundResource(R.drawable.btn_on)
                        }
                    }
                    handled
                }
            }

            trash_changed = 1 // 쓰레기통버튼 off
            if(trash_changed == 1){
                trash_checkd("꺼짐")
                binding.trash.setBackgroundResource(R.drawable.btn_select)
            }
            else{
                trash_checkd("켜짐")
                binding.trash.setBackgroundResource(R.drawable.btn_on)
            }
            binding.etn.text = null // null 값으로 설정
        }

        binding.trash.setOnClickListener {
            trash_changed *= -1

            if(trash_changed == 1){
                trash_checkd("꺼짐")
                binding.trash.setBackgroundResource(R.drawable.btn_select)
            }
            else{
                trash_checkd("켜짐")
                binding.trash.setBackgroundResource(R.drawable.btn_on)
            }

        }

        binding.fontChange.setOnClickListener {
            /*tag_array.removeAt(0)
            binding.FlexRecycler.adapter?.notifyItemRemoved(0)
            binding.FlexRecycler.adapter?.notifyItemRangeChanged(0, tag_array.size)// 추가된 데이터 새로고침하여 변경

            binding.tagTe.text = "" // 빈값으로 설정
            binding.etn.text = null // 빈값으로 설정*/

            /*var anima = AnimationUtils.loadAnimation(this, R.anim.remove_animation)

            binding.fontChange.startAnimation(anima)*/

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

