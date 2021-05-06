package com.diary.diary

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diary.diary.databinding.ActivityMainBinding
import com.diary.recycler.Recycler_main
import com.diary.recycler.list
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


// 스플래쉬 화면 -> (비밀번호 걸려있을시) 비밀번호 -> Intent main 이동 .


@Entity
data class Diaryroom(//id, 날짜, 제목, 내용, 태그, 이미지uri, 에딧text
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "content") val content: String,
        @ColumnInfo(name = "uri_string_array") val uri_string_array: List<String?>, //스트링형으로 변환.*/
        @ColumnInfo(name = "edit_string_array") val edit_string_array: List<String?>,
        @ColumnInfo(name = "edit_size") val edit_size:Float,
        @ColumnInfo(name = "edit_font") val edit_font:String,
        @ColumnInfo(name = "edit_color") val edit_color:String,
        @ColumnInfo(name = "edit_linespacing") val linespacing:Float,
        @ColumnInfo(name = "edit_letterspacing") val letterspacing:Float,
        @ColumnInfo(name = "Shortcuts") val Shortcuts:List<String?>?,
        @ColumnInfo(name = "dateLong") val dateLong:Long,
        @ColumnInfo(name = "date_daytofweek") val date_daytofweek:String,
        @ColumnInfo(name = "daytoweek") val daytoweek:String,
        @ColumnInfo(name = "taglist") val taglist:List<String>,
        @ColumnInfo(name = "allcontent") val allcontent:String
)

class Imagelist {
    @TypeConverter
    fun uristringToJson(value: List<String?>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToUriString(value: String) = Gson().fromJson(value, Array<String?>::class.java).toList()

}

@Dao
interface DiaryDao{
    @Insert //모든 것들 추가.
    suspend fun insertDao(vararg diaryroom: Diaryroom)

    @Update
    suspend fun updateDao(vararg diaryroom: Diaryroom)

    @Query("DELETE FROM Diaryroom WHERE dateLong = :dateLong") //삭제
    suspend fun DeletedateDao(dateLong:Long)

    @Query("SELECT * FROM Diaryroom")
    suspend fun getAll():List<Diaryroom>

    @Query("SELECT * FROM Diaryroom WHERE id = :id") //동적으로 할당시 함수 파라미터에 변수 넣고, WHERE 에 :변수 로 넣기.
    suspend fun getlayoutid(id:Int):Diaryroom

    @Query("SELECT * FROM Diaryroom WHERE dateLong = :dateLong")
    suspend fun getAllfordateLong(dateLong: Long):Diaryroom
}


@Database(entities = [Diaryroom::class], version = 1)
@TypeConverters(Imagelist::class)
abstract class RoomdiaryDB:RoomDatabase(){
    abstract fun RoomDao():DiaryDao
}

class Recylcerviewmodel:ViewModel(){
    var longclick_observe = MutableLiveData<String>()
    var titleClick = MutableLiveData<String>()
    var settingClick = MutableLiveData<String>()
    var alldiaryClick = MutableLiveData<String>()

    fun longClick() = longclick_observe
    fun titleClick() {
        titleClick.value = ""
    }

    fun settingClick(){
        settingClick.value = ""
    }

    fun alldiaryClick(){
        alldiaryClick.value = ""
    }
}

class MainActivity : AppCompatActivity(), layout_remove {
    lateinit var binding: ActivityMainBinding
    lateinit var db: RoomdiaryDB

    var datearray:ArrayList<Long> = arrayListOf() // 날짜 저장용
    var diarylist: ArrayList<list> = arrayListOf() //이미지도 추가하기.


    var remove_layout_checkInt:ArrayList<Int> = arrayListOf()
    var date_layout_checkLong:ArrayList<Long> = arrayListOf()
    var taglist_array:ArrayList<String> = arrayListOf()

    companion object{
        var diary_btn_change = 0

        lateinit var viewModel:Recylcerviewmodel
        lateinit var room:List<Diaryroom>

        val cal = Calendar.getInstance() // 날짜

        var calendar_year = cal.get(Calendar.YEAR)
        var calendar_month = cal.get(Calendar.MONTH) + 1
        var monthstring = calendar_month.toString()

        var year_save = 0
        var month_save = 0
        var day_save = 0
        var search_calendar_min:Long = 1
        var search_calendar_max:Long = 1

        var roomcheck = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("사이클", "크리에잇")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(Recylcerviewmodel::class.java)
        binding.recylcerviewmodel = viewModel

        var two_year = calendar_year.toString().substring(2)
        binding.mainRecylerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.maintitle.setText("${two_year}년 ${calendar_month}월의 추억")

        db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java, "RoomDB"
        )
                .build()

        if(calendar_month < 10)
            monthstring = "0$calendar_month"


        binding.searchDiary.setOnClickListener {
                var intent = Intent(this, search_diary::class.java)
                startActivity(intent)
                diary_btn_change = 0
                removeAll()
                binding.mainRecylerview.adapter?.notifyDataSetChanged()
        }

        viewobserve() // 여기에 놓지 않고, onresume에 하면 observe들이 중복호출된다.
    }

    override fun onResume() {
        super.onResume()

        coroutine()
        alldiary()

        if(Setting.a == 222){ // 뒤로가기로 설정창에서 다크모드 같은것 설정 후 나왔을 경우 대비.
            binding.maintitle.setTextColor(Color.parseColor("#FB9909"))
            binding.maintitleLayout.setBackgroundColor(Color.parseColor("#201F1F"))
            binding.mainTopLayout.setBackgroundColor(Color.parseColor("#201F1F"))
            darkmodesetting("다크모드")
            binding.mainRecylerview.adapter?.notifyDataSetChanged()
        }
    }

    private fun viewobserve(){
        viewModel.longclick_observe.observe(this, {
            trash_btn()
        })

        viewModel.titleClick.observe(this, {
            val today = GregorianCalendar()
            val year: Int = if (year_save == 0) {
                today.get(Calendar.YEAR)
            } else {
                year_save
            }
            val month: Int = if(month_save == 0){
                today.get(Calendar.MONTH)
            }
            else{
                month_save
            }
            val date: Int = if(day_save == 0) {
                today.get(Calendar.DATE)
            }
            else{
                day_save
            }

            val dlg = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

                    year_save = year
                    month_save = month
                    day_save = dayOfMonth

                    calendar_year = year
                    calendar_month = month + 1

                    Log.d("서치선택", calendar_year.toString())
                    Log.d("서치선택", calendar_month.toString())

                    var two_year = year.toString().substring(2)
                    binding.maintitle.setText("${two_year}년 ${month + 1}월의 추억")

                    diarylist.removeAll(diarylist)
                    datearray.removeAll(datearray)
                    taglist_array.removeAll(taglist_array)
                    binding.mainRecylerview.adapter?.notifyItemRangeRemoved(0, diarylist.size)
                    binding.mainRecylerview.adapter?.notifyDataSetChanged()
                    coroutine()
                }
            }, year, month, date)
            dlg.show()
        })

        viewModel.settingClick.observe(this, {
            startActivity(Intent(this, Setting::class.java))
            diary_btn_change = 0
            removeAll()
            binding.mainRecylerview.adapter?.notifyDataSetChanged()
        })

        viewModel.alldiaryClick.observe(this, {
            if (diary_btn_change == 0) {
                var intent = Intent(this, Content_create::class.java)
                startActivity(intent)
                removeAll()
            } else {// 여기에 다이얼로그로 삭제 하냐는 질문 만들고 if else로 나눠서 layout_remove ~~ 연결시키기.
                if (layout_remove().first != null) { //내용물이 가장 큰것이 마지막 배열로 가게 정렬
                    remove_layout_checkInt = layout_remove().first!!  //인터페이스에서 return ArrayList<Int> 입니다.
                    for (i in 0 until remove_layout_checkInt.size) { // 포지션값의 크기에 따라 작은것 -> 큰것순으로 정렬하는 코드
                        var imsi = 0
                        for (j in i until remove_layout_checkInt.size) {
                            if (remove_layout_checkInt[i] > remove_layout_checkInt[j]) {
                                imsi = remove_layout_checkInt[j]
                                remove_layout_checkInt[j] = remove_layout_checkInt[i]
                                remove_layout_checkInt[i] = imsi
                                Log.d("사이즈정렬중 i", remove_layout_checkInt[i].toString())
                                Log.d("사이즈정렬중 j", remove_layout_checkInt[j].toString())
                            }
                        }
                        Log.d("사이즈정렬후", remove_layout_checkInt.toString())
                    }

                    if (layout_remove().second!!.isNotEmpty()) {
                        date_layout_checkLong = layout_remove().second!!
                        Log.d("데이터갯수11", date_layout_checkLong.toString())
                    }

                    for (i in remove_layout_checkInt.size - 1 downTo 0) { // downto로 안하면 작은것부터 삭제 후 큰것삭제하는데, 작은것이 삭제되었을경우 사이즈가 줄어들어 큰 숫자가 안들어가지는 상황이 있어. 오류가 발생함.
                        diarylist.removeAt(remove_layout_checkInt[i]) // remove_layout_checkInt는 아이템 롱클릭시 받아와지는 포지션값.
                        binding.mainRecylerview.adapter?.notifyItemRemoved(remove_layout_checkInt[i])
                        binding.mainRecylerview.adapter?.notifyItemRangeChanged(remove_layout_checkInt[i], diarylist.size)
                        Log.d("사이즈삭제파일은", remove_layout_checkInt[i].toString())
                    }

                    binding.mainRecylerview.adapter?.notifyDataSetChanged()

                    CoroutineScope(Dispatchers.IO).launch {
                        for (i in date_layout_checkLong.indices) {
                            Log.d("데이터갯수22", date_layout_checkLong.toString())
                            Log.d("없애는데이터", date_layout_checkLong[i].toString())
                            db.RoomDao().DeletedateDao(date_layout_checkLong[i])
                        }
                        layout_remove_position_check(1024)
                        Log.d("실행됨", "실행")
                        diary_btn_change = 0
                    }

                    binding.allDiary.setImageResource(R.drawable.ic_baseline_create_24)
                }
            }
        })
    }
    
    private fun coroutine(){
        if(calendar_month < 10)
            monthstring = "0$calendar_month"

        search_calendar_min = "${calendar_year}${monthstring}00000000".toLong()
        search_calendar_max = "${calendar_year}${monthstring}99999999".toLong()

        Log.d("날짜", search_calendar_max.toString())
        Log.d("날짜", search_calendar_min.toString())

        CoroutineScope(Dispatchers.IO).launch { // Room DB는 mainthread에서 못가져온다.
            CoroutineScope(Dispatchers.IO).launch {
                var d = 0
                if (db.RoomDao().getAll().isNotEmpty()) {
                    roomcheck = true
                    room = db.RoomDao().getAll()

                    for (i in room.indices) {
                        datearray.add(room[i].dateLong)
                        if (room[i].taglist.isNotEmpty()) {
                            for (j in room[i].taglist.indices) {
                                taglist_array.add(room[i].taglist[j])
                            }
                        }
                    }

                    for (i in datearray.indices) {
                        for (j in i until datearray.size) {
                            if (datearray[i] > datearray[j]) {
                                var date = datearray[i]
                                datearray[i] = datearray[j]
                                datearray[j] = date
                            }
                        }
                    }

                    for (i in datearray.size-1 downTo 0) {
                        if (datearray[i] in search_calendar_min..search_calendar_max) {
                            continue
                        } else {
                            datearray.removeAt(i)
                        }
                    }

                    if (datearray.isNotEmpty()) {
                        for (i in datearray.size - 1 downTo 0) { // 설정한 날짜에 맞춰 최신것이 가장 위로 올라오게 만들기.
                            var date_room = db.RoomDao().getAllfordateLong(datearray[i])
                            val id: Int = date_room.id
                            val title: String = date_room.title
                            var content: String = date_room.content
                            val font: String = date_room.edit_font
                            val uri = date_room.uri_string_array
                            val editstr = date_room.edit_string_array
                            val date_daytoweek = date_room.date_daytofweek
                            val daytoweek = date_room.daytoweek

                            if (editstr.isNotEmpty()) {
                                for (e in editstr.indices) {
                                    content += editstr[e] //Edit_array에 내용이 존재할경우 리사이클러뷰에는 내용이 모두 추가가 되어 보여짐.
                                }
                            }
                            d++
                            diarylist.add(list(id, title, content, uri, font, date_daytoweek, daytoweek, datearray[i]))
                            Log.d("확인", "사진 갯수, $uri")
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.noSearch.setText("$monthstring 월의 일기가 존재하지 않습니다.")
                            binding.noSearch.visibility = View.GONE
                        }
                    }
                    else{
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.noSearch.setText("$monthstring 월의 일기가 존재하지 않습니다.")
                            binding.noSearch.visibility = View.VISIBLE
                        }
                    }
                }

                Log.d("확인", "모두 불러와짐, $d")
            }.join()


            CoroutineScope(Dispatchers.Main).launch { //Ui 관련이니 Dispachers.main 사용.
                binding.mainRecylerview.setHasFixedSize(true)
                binding.mainRecylerview.adapter = Recycler_main(diarylist, binding.shadowText, "main")
                binding.mainRecylerview.adapter?.notifyDataSetChanged()
                Log.d("사이즈", diarylist.size.toString())
            }
        }
    }
/*
* intent로 액티비티 만든곳에 구글 드라이브 복원 및 백업 구현하고 데이터 초기화 누를시 모든 리사이클러뷰, room 데이터 삭제시키기.
* /이미지들 스크린샷찍어서 설명하기.
                    var intent = Intent(this, password::class.java)
                    intent.putExtra("비밀번호 설정", "비밀번호 설정")
                    startActivity(intent)
                    *
                    var intent = Intent(this, tag_setting::class.java)
                    intent.putExtra("태그 설정", taglist_array)
                    startActivity(intent)
* */
    private fun alldiary(){
        binding.allDiary.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN) // 버튼에 손을 올렸을 경우
                binding.allDiary.setBackgroundResource(R.drawable.create_diary_color)
            else if(event.action == MotionEvent.ACTION_UP){ // 버튼에서 손을 뗏을 경우
                binding.allDiary.setBackgroundResource(R.drawable.btn_on)
            }
            return@setOnTouchListener false
        }
    }

    override fun onBackPressed() {
        if(diary_btn_change == 1){ // 레이아웃 삭제 이벤트중일시.
            diary_btn_change = 0
            layout_remove_position_check(1024)
            binding.allDiary.setImageResource(R.drawable.ic_baseline_create_24)
        }
        else if(intent.hasExtra("이동")) // 나중에 종료 팝업 만들고, 네비게이션시 네비게이션 닫게 만들기.
            Log.d("이동함", "이동함")
        else{
            super.onBackPressed()
        }
    }

    private fun trash_btn(){
        diary_btn_change = 1
        binding.allDiary.setImageResource(R.drawable.ic_baseline_restore_from_trash_24)
        //setting 부분 arrow Left로 변경후 클릭시 모든 상태 원상태로 만들기. diarybtn 0, layout 모두 초기화 등.
    }
}