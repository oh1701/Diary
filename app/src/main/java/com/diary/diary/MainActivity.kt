package com.diary.diary

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diary.diary.databinding.ActivityMainBinding
import com.diary.recycler.Recycler_main
import com.diary.recycler.list
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.kakao.adfit.ads.AdListener
import com.kakao.adfit.ads.ba.BannerAdView
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


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

//숏컷 폰트 어레이 1번 라인스페이싱
//2번 레터 스페이싱
//3번 텍스트사이즈
//4번 폰트
//5번 칼라 모두 스트링형.
@Entity
data class Shortcutroom(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "shortcut") val shortcut: String,
        @ColumnInfo(name = "shortcutfont") val shortcutfont: List<String>?,
        @ColumnInfo(name = "shortcutmystring") val shortcutmystring: String?
)

class Imagelist {
    @TypeConverter
    fun uristringToJson(value: List<String?>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToUriString(value: String) = Gson().fromJson(value, Array<String?>::class.java).toList()
}

@Dao
interface DiaryDao{
    @Insert
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

    //여기부터는 단축키
    @Insert
    suspend fun insertshortcut(vararg shortcutroom: Shortcutroom)

    @Update
    suspend fun updateshortcut(vararg shortcutroom: Shortcutroom)

    @Query("SELECT * FROM Shortcutroom")
    suspend fun getshortcutAll():List<Shortcutroom>

    @Query("SELECT * FROM Shortcutroom WHERE shortcut = :shortcut")
    suspend fun getshortcut(shortcut: String):Shortcutroom

    @Query("DELETE FROM Shortcutroom WHERE shortcut = :shortcut") //삭제
    suspend fun Deleteshortcut(shortcut:String)
}


@Database(entities = [Diaryroom::class, Shortcutroom::class], version = 1)
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
    lateinit var metrics: DisplayMetrics

    var datearray:ArrayList<Long> = arrayListOf() // 날짜 저장용
    var diarylist: ArrayList<list> = arrayListOf() //이미지도 추가하기.

    var remove_layout_checkInt:ArrayList<Int> = arrayListOf()
    var date_layout_checkLong:ArrayList<Long> = arrayListOf()
    var taglist_array:ArrayList<String> = arrayListOf()

    companion object{
        var diary_btn_change = 0

        lateinit var viewModel:Recylcerviewmodel
        lateinit var room:List<Diaryroom>
        lateinit var shortcutroom:List<Shortcutroom>

        var shortcutname:String? = null
        var shortcutfontList: List<String>? = null
        var shortcutmystring: String? = null

        val cal = Calendar.getInstance() // 날짜

        var calendar_year = cal.get(Calendar.YEAR)
        var calendar_month = cal.get(Calendar.MONTH) + 1
        var monthstring = calendar_month.toString()

        var year_save = 0
        var month_save = 0
        var day_save = 0
        var day_change = 0
        var search_calendar_min:Long = 1
        var search_calendar_max:Long = 1

        var roomcheck = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(Recylcerviewmodel::class.java)
        binding.recylcerviewmodel = viewModel

        metrics = resources.displayMetrics

        MobileAds.initialize(this) {}
        var mAdView = binding.adview1
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        var two_year = calendar_year.toString().substring(2)
        binding.maintitle.setText("${two_year}년 ${calendar_month}월의 추억")

        db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java, "RoomDB"
        )
                .build()

        binding.mainRecylerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

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
        coroutine() //기본 room 데이터들 가지고 옴. (단축키 뺀 데이터들) onresume에 넣으면 중복추가.
        alldiary()
    }

    override fun onResume() {
        super.onResume()
        binding.allDiary.setImageResource(R.drawable.ic_baseline_create_24)
        getshortcut() //코루틴 통해서 단축키 가져옴.

        binding.mainRecylerview.setHasFixedSize(true)
        binding.mainRecylerview.adapter = Recycler_main(diarylist, binding.shadowText, "main")
        binding.mainRecylerview.adapter?.notifyDataSetChanged()

        var sharedPreferences = getSharedPreferences("LOCK_PASSWORD", 0)

        if(sharedPreferences.getString("DARK_MODE", "").toString() == "ON"){ // 뒤로가기로 설정창에서 다크모드 같은것 설정 후 나왔을 경우 대비.
            binding.maintitle.setTextColor(Color.parseColor("#FB9909"))
            binding.maintitleLayout.setBackgroundColor(Color.parseColor("#272626"))
            binding.mainTopLayout.setBackgroundColor(Color.parseColor("#272626"))
            binding.noSearch.setTextColor(Color.WHITE)
            darkmodesetting("다크모드")
            binding.mainRecylerview.adapter?.notifyDataSetChanged()
        }
        else if(sharedPreferences.getString("DARK_MODE", "").toString() == "OFF"){
            binding.maintitle.setTextColor(Color.WHITE)
            binding.maintitleLayout.setBackgroundColor(Color.parseColor("#F5201F1F"))
            binding.mainTopLayout.setBackgroundColor(Color.parseColor("#E8E8E8"))
            binding.noSearch.setTextColor(Color.BLACK)
            darkmodesetting("기본")
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
            val month: Int = if(month_save == 0 && day_change == 0){
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
                    day_change = 1

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
                val permission_view: View = LayoutInflater.from(this).inflate(R.layout.activity_permission_intent, null)// 커스텀 다이얼로그 생성하기. 권한은 저장공간, 카메라

                val dialog = Dialog(this)
                dialog.setContentView(permission_view)

                val permission_positive_btn =
                        permission_view.findViewById<Button>(R.id.warning_positive)
                val permission_negative_btn =
                        permission_view.findViewById<Button>(R.id.warning_negative)
                val text = permission_view.findViewById<TextView>(R.id.warnig_text)

                text.text = "정말 삭제하시겠습니까?\n삭제시 복구는 불가능합니다."
                permission_positive_btn.text = "삭제"
                permission_negative_btn.text = "취소"

                permission_positive_btn.setOnClickListener {
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
                            binding.mainRecylerview.adapter?.notifyDataSetChanged()
                            Log.d("사이즈삭제파일은", remove_layout_checkInt[i].toString())
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            for (i in date_layout_checkLong.indices) {
                                Log.d("데이터갯수22", date_layout_checkLong.toString())
                                Log.d("없애는데이터", date_layout_checkLong[i].toString())
                                db.RoomDao().DeletedateDao(date_layout_checkLong[i])
                            }
                        }

                        layout_remove_position_check(1024)
                        diary_btn_change = 0
                        binding.mainRecylerview.adapter?.notifyDataSetChanged()
                        binding.allDiary.setImageResource(R.drawable.ic_baseline_create_24)
                        dialog.dismiss()
                    }
                }
                permission_negative_btn.setOnClickListener {
                    layout_remove_position_check(1024)
                    Log.d("실행됨", "실행")
                    diary_btn_change = 0
                    binding.allDiary.setImageResource(R.drawable.ic_baseline_create_24)
                    binding.mainRecylerview.adapter?.notifyDataSetChanged()
                    dialog.dismiss()
                }

                var lp = WindowManager.LayoutParams()
                lp.copyFrom(dialog.window!!.attributes)
                lp.width = metrics.widthPixels * 7 / 10 //레이아웃 params 에 width, height 넣어주기.
                lp.height = metrics.heightPixels * 4 / 10
                dialog.show()
                dialog.window!!.attributes = lp
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
                            Log.d("확인", "화아아악인")
                        }
                    }
                    else{
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.noSearch.setText("$monthstring 월의 일기가 존재하지 않습니다.")
                            binding.noSearch.visibility = View.VISIBLE
                            Log.d("확인", "화아아악인")
                        }
                    }
                }
                else {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.noSearch.setText("$monthstring 월의 일기가 존재하지 않습니다.")
                        binding.noSearch.visibility = View.VISIBLE
                        Log.d("확인", "화아아악인")
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

    private fun getshortcut(){
        CoroutineScope(Dispatchers.IO).launch {
            if(db.RoomDao().getshortcutAll().isNotEmpty()){
                shortcutroom = db.RoomDao().getshortcutAll()

                for(i in shortcutroom.indices){
                    shortcutname = shortcutroom[i].shortcut
                    shortcutfontList = shortcutroom[i].shortcutfont
                    shortcutmystring = shortcutroom[i].shortcutmystring

                    Log.d("shortcutname", "$shortcutname")
                    Log.d("shortcutfontList", "$shortcutfontList")
                    Log.d("shortcutmystring", "$shortcutmystring")
                    Log.d("숏컷모두", shortcutroom[i].toString())
                }
                Setting.short_size = shortcutroom.size
            }
            else{ // 초기화 된 상태 (처음 실행했을 시에)
                db.RoomDao().insertshortcut(Shortcutroom(0, "현재 날짜", arrayListOf(), null))
                db.RoomDao().insertshortcut(Shortcutroom(0, "현재 시간", arrayListOf(), null))
            }
        }
    }

    override fun onBackPressed() {
        if(diary_btn_change == 1){ // 레이아웃 삭제 이벤트중일시.
            diary_btn_change = 0
            layout_remove_position_check(1024)
            binding.allDiary.setImageResource(R.drawable.ic_baseline_create_24)
        }
        else{
            val view = LayoutInflater.from(this).inflate(R.layout.close_app, null)
            val dialog = Dialog(this)
            dialog.setContentView(view)

            val adfit = view.findViewById<BannerAdView>(R.id.kakao_View)
            val noadfit_image = view.findViewById<ImageView>(R.id.kakao_View_nodownload_image)
            val positive = view.findViewById<Button>(R.id.positive)
            val negative = view.findViewById<Button>(R.id.negative)

            positive.setOnClickListener {
                ActivityCompat.finishAffinity(this)
                exitProcess(0)
            }
            negative.setOnClickListener {
                dialog.dismiss()
            }

            adfit.setClientId("DAN-S0VcxwmVbtlWnXb9")  // 할당 받은 광고단위 ID 설정
            adfit.setAdListener(object : AdListener {  // optional :: 광고 수신 리스너 설정

                override fun onAdLoaded() {
                    // 배너 광고 노출 완료 시 호출
                }

                override fun onAdFailed(errorCode: Int) {
                    adfit.visibility = View.GONE
                    noadfit_image.visibility = View.VISIBLE
                    // 배너 광고 노출 실패 시 호출
                }

                override fun onAdClicked() {
                    // 배너 광고 클릭 시 호출
                }

            })

// activity 또는 fragment의 lifecycle에 따라 호출
            lifecycle.addObserver(object : LifecycleObserver {

                @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                fun onResume() {
                    adfit.resume()
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                fun onPause() {
                    adfit.pause()
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    adfit.destroy()
                }
            })
            adfit.loadAd()

            var lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.width = metrics.widthPixels * 8 / 10 //레이아웃 params 에 width, height 넣어주기.
            lp.height = metrics.heightPixels * 6 / 10
            dialog.window!!.attributes = lp

            dialog.show()
        }
    }

    private fun trash_btn(){
        diary_btn_change = 1
        binding.allDiary.setImageResource(R.drawable.ic_baseline_restore_from_trash_24)
        //setting 부분 arrow Left로 변경후 클릭시 모든 상태 원상태로 만들기. diarybtn 0, layout 모두 초기화 등.
    }
}