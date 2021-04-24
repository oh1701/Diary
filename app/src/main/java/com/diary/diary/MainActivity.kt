package com.diary.diary

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

    @Query("DELETE FROM Diaryroom") //삭제
    suspend fun DeleteDao()

    @Query("SELECT * FROM Diaryroom")
    suspend fun getAll():List<Diaryroom>

    @Query("SELECT * FROM Diaryroom WHERE id = :id") //동적으로 할당시 함수 파라미터에 변수 넣고, WHERE 에 :변수 로 넣기.
    suspend fun getlayoutid(id:Int):Diaryroom
}


@Database(entities = [Diaryroom::class], version = 1)
@TypeConverters(Imagelist::class)
abstract class RoomdiaryDB:RoomDatabase(){
    abstract fun RoomDao():DiaryDao
}

class recyclerviewmodel:ViewModel(){
    var longclick_observe = MutableLiveData<String>()

    fun longClick() = longclick_observe
}

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var diarylist: ArrayList<list> = arrayListOf() //이거 이미지도 추가하기.
    lateinit var db: RoomdiaryDB
    lateinit var room:List<Diaryroom>

    var datearray:ArrayList<Long> = arrayListOf()

    companion object{
        lateinit var viewModel:recyclerviewmodel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(recyclerviewmodel::class.java)
        binding.recyclerviewmodel = viewModel

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

                    Log.d("i는는", datearray.toString())

                    for (j in datearray.size - 1 downTo 0) { // 설정한 날짜에 맞춰 최신것이 가장 위로 올라오게 만들기.
                        for(i in room.indices){
                            if(datearray[j] == room[i].dateLong){
                                val id: Int = room[i].id
                                val title: String = room[i].title
                                var content: String = room[i].content
                                val font:String = room[i].edit_font
                                val uri = room[i].uri_string_array
                                val editstr = room[i].edit_string_array
                                val date_daytoweek = room[i].date_daytofweek
                                val daytoweek = room[i].daytoweek

                                if(editstr.isNotEmpty()){
                                    for(e in editstr.indices) {
                                        content += editstr[e] //Edit_array에 내용이 존재할경우 리사이클러뷰에는 내용이 모두 추가가 되어 보여짐.
                                    }
                                }
                                d++
                                diarylist.add(list(id, title, content, uri, font, date_daytoweek, daytoweek))

                                Log.d("확인", "사진 갯수, $uri")
                                Log.d("i는", "${datearray[j]}, $j 는 ${room[i]}, $i 다")
                                break
                            }
                        }
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
            var intent = Intent(this, Content_create::class.java)
            startActivity(intent)
        }

        binding.setting.setOnClickListener {
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
        else {
            super.onBackPressed()
        }
    }

    private fun viewobserve(){
        viewModel.longclick_observe.observe(this, {
            Toast.makeText(this, "눌려짐", Toast.LENGTH_SHORT).show()
        })
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