package com.diary.diary

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import com.diary.diary.databinding.ActivityMainBinding
import com.diary.recycler.Recycler_main
import com.diary.recycler.list
import com.google.gson.Gson
import kotlinx.coroutines.*


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
        @ColumnInfo(name = "daytoweek") val daytoweek:String

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
    suspend fun DeleteDao(dateLong:Long)

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

    fun longClick() = longclick_observe
}

class MainActivity : AppCompatActivity(), layout_remove {
    lateinit var binding: ActivityMainBinding
    var diarylist: ArrayList<list> = arrayListOf() //이거 이미지도 추가하기.
    lateinit var db: RoomdiaryDB
    lateinit var room:List<Diaryroom>

    var datearray:ArrayList<Long> = arrayListOf()
    var change = 0

    var remove_layout_checkInt:ArrayList<Int> = arrayListOf()

    companion object{
        lateinit var viewModel:Recylcerviewmodel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(Recylcerviewmodel::class.java)
        binding.recylcerviewmodel = viewModel

        binding.mainRecylerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java, "RoomDB"
        )
                .build()

        CoroutineScope(Dispatchers.IO).launch { // Room DB는 mainthread에서 못가져온다.
            CoroutineScope(Dispatchers.IO).launch {
                var d = 0
                if (db.RoomDao().getAll().isNotEmpty()) {
                    room = db.RoomDao().getAll()

                    for (i in room.indices) {
                        datearray.add(room[i].dateLong)
                    }

                        for (i in datearray.indices) {
                            for(j in i until datearray.size){
                                if(datearray[i] > datearray[j]){
                                    var date = datearray[i]
                                    datearray[i] = datearray[j]
                                    datearray[j] = date
                                }
                            }
                        }

                    for (i in datearray.size - 1 downTo 0) { // 설정한 날짜에 맞춰 최신것이 가장 위로 올라오게 만들기.
                                var date_room = db.RoomDao().getAllfordateLong(datearray[i])
                                val id: Int = date_room.id
                                val title: String = date_room.title
                                var content: String = date_room.content
                                val font:String = date_room.edit_font
                                val uri = date_room.uri_string_array
                                val editstr = date_room.edit_string_array
                                val date_daytoweek = date_room.date_daytofweek
                                val daytoweek = date_room.daytoweek

                                if(editstr.isNotEmpty()){
                                    for(e in editstr.indices) {
                                        content += editstr[e] //Edit_array에 내용이 존재할경우 리사이클러뷰에는 내용이 모두 추가가 되어 보여짐.
                                    }
                                }
                                d++
                                diarylist.add(list(id, title, content, uri, font, date_daytoweek, daytoweek, datearray[i]))

                                Log.d("확인", "사이즈, $datearray")
                                Log.d("확인", "사진 갯수, $uri")
                                Log.d("i는", "${datearray[i]}, $i 는 ${room[i]}, $i 다")
                    }
                }

                Log.d("확인", "모두 불러와짐, $d")
            }.join()


            CoroutineScope(Dispatchers.Main).launch { //Ui 관련이니 Dispachers.main 사용.
                binding.mainRecylerview.setHasFixedSize(true)
                binding.mainRecylerview.adapter = Recycler_main(diarylist, binding.shadowText)
            }
        }

        binding.allDiary.setOnClickListener {
            if(change == 0) {
                var intent = Intent(this, Content_create::class.java)
                startActivity(intent)
            }
            else{
                if(layout_remove().first != null){ //내용물이 가장 큰것이 마지막 배열로 가게 정렬.
                    remove_layout_checkInt = layout_remove().first!!
                    for(i in 0 until remove_layout_checkInt.size){
                        var imsi = 0
                        for(j in remove_layout_checkInt.indices){
                            if (remove_layout_checkInt[i] > remove_layout_checkInt[j]) {
                                imsi = remove_layout_checkInt[j]
                                remove_layout_checkInt[i] = remove_layout_checkInt[j]
                                remove_layout_checkInt[j] = imsi
                            }
                        }
                    }

                    for(i in remove_layout_checkInt.size - 1 downTo 0){ // downto로 안하면 작은것부터 삭제 후 큰것삭제하는데, 작은것이 삭제되었을경우 사이즈가 줄어들어 큰 숫자가 안들어갈수 있어. 오류가 발생함.
                        Log.d("클릭일부", remove_layout_checkInt[i].toString())
                        Log.d("클릭일부", diarylist.toString())
                        diarylist.removeAt(remove_layout_checkInt[i])
                    }

                    binding.mainRecylerview.adapter?.notifyDataSetChanged()
                    Log.d("클릭전체", remove_layout_checkInt.toString())
                }

                layout_remove_position_check(1024)
                CoroutineScope(Dispatchers.IO).launch {

                }
            }
        }

        binding.setting.setOnClickListener {
            binding.allDiary.setBackgroundResource(R.drawable.exit_btn)
            binding.drawerSetting.openDrawer(GravityCompat.START)
        }

        /*binding.mainSettingNavi.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navi_shortcuts ->{}
                R.id.navi_password -> {}
                R.id.navi_tema_change -> {}
                R.id.navi_dark_mode -> {}
                R.id.navi_tag -> {}
                R.id.navi_lock -> {}
                R.id.google_drive ->{}
                R.id.explanation -> {}
                R.id.font_name -> {}
            }
        }*/
        viewobserve()
    }

    override fun onBackPressed() {

        if(intent.hasExtra("이동")) // 나중에 종료 팝업 만들고, 네비게이션시 네비게이션 닫게 만들기.
            Log.d("이동함", "이동함")
        else if(change == 1){
            change = 0
        }
        else{
            super.onBackPressed()
        }
    }

    private fun viewobserve(){
        viewModel.longclick_observe.observe(this, {
            trash_btn()
        })
    }

    private fun trash_btn(){
        change = 1
        binding.allDiary.setBackgroundResource(R.drawable.ic_baseline_restore_from_trash_24)
    }
}

/*
*
            CoroutineScope(Dispatchers.IO).launch {
                var d = 0
                if (db.RoomDao().getAll().isNotEmpty()) {
                    room = db.RoomDao().getAll()

                    for (i in room.size - 1 downTo 0) {
                        val id: Int = room[i].id
                        val title: String = room[i].title
                        var content: String = room[i].content
                        val font:String = room[i].edit_font
                        val uri = room[i].uri_string_array
                        val editstr = room[i].edit_string_array

                        if(editstr.isNotEmpty()){
                            for(i in editstr.indices) {
                                content += editstr[i] //Edit_array에 내용이 존재할경우 리사이클러뷰에는 내용이 모두 추가가 되어 보여짐.
                            }
                        }
                        d++
                        diarylist.add(list(id, title, content, uri, font))
                        Log.d("확인", "사진 갯수, $uri")
                    }
                }

                Log.d("확인", "모두 불러와짐, $d")
            }.join()
* */