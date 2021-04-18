package com.diary.diary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.*
import com.diary.diary.databinding.ActivityMainBinding
import com.diary.recycler.Recycler_main
import com.diary.recycler.list
import com.diary.recycler.tagline
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.File.separator
import java.util.jar.Manifest
// 리무브 버튼은 0번째 리스트부터 하나씩 넣어가기(바이트형 이미지와 스트링형 edit도)

@Entity
data class Diaryroom(//id, 날짜, 제목, 내용, 태그, layout 두개.
        @PrimaryKey(autoGenerate = true) val id:Int,
        @ColumnInfo(name = "date") val date:Long,
        @ColumnInfo(name = "title") val title:String,
        @ColumnInfo(name = "content") val content:String,

        /*@ColumnInfo(name = "linear_list") val linear_list:List<Byte>, //바이트에서 형 변환하자

        @ColumnInfo(name = "frame_list") val frame_list:List<String?> //스트링형으로 변환.*/
)

@Dao
interface DiaryDao{
    @Insert
    fun insertDao(vararg diaryroom: Diaryroom)

    @Query("DELETE FROM Diaryroom")
    fun DeleteDao()

    @Query("SELECT * FROM Diaryroom")
    fun getAll():List<Diaryroom>
}

object Converters{
    class linear {
        @TypeConverter
        fun linearToJson(linear: List<Byte?>) = Gson().toJson(linear)

        @TypeConverter
        fun jsonlinearTolist(linear: String) = Gson().fromJson(linear, Array<Byte>::class.java).toList()
    }

    class frame {
        @TypeConverter
        fun frameToJson(frame: List<String>) = Gson().toJson(frame)

        @TypeConverter
        fun jsonrframeTolist(frame: String) = Gson().fromJson(frame, Array<String>::class.java).toList()
    }
}

@Database(entities = [Diaryroom::class], version = 1)
@TypeConverters(Converters.linear::class, Converters.frame::class)
abstract class RoomdiaryDB:RoomDatabase(){
    abstract fun RoomDao():DiaryDao
}

class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding
    var diarylist:ArrayList<list> = arrayListOf() //이거 이미지도 추가하기.
    lateinit var db:RoomdiaryDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("확인", "get 된다.")
        val context = this
        var linear: ArrayList<LinearLayout?>

        db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java
                , "RoomDB"
        )
                .build()

                //db.RoomDao().DeleteDao()
        CoroutineScope(Dispatchers.IO).launch { // Room DB는 mainthread에서 못가져온다.
            val a = db.RoomDao().getAll().size - 1

            Log.d("사이즈", a.toString())

            if(a == 0){
                val room: Diaryroom = db.RoomDao().getAll()[0]
                val id: Int = room.id
                val date: Long = room.date
                val title: String = room.title
                val content: String = room.content

                diarylist.add(list(id, date, title, content, null, null))
            }
            else if(a >= 1){
                for (i in a downTo 0) { // 역순으로 설정하여 최신 데이터가 상단으로 올라오게 만든다.
                    val room: Diaryroom = db.RoomDao().getAll()[i]
                    val id: Int = room.id
                    val date: Long = room.date
                    val title: String = room.title
                    val content: String = room.content

                    diarylist.add(list(id, date, title, content, null, null))
                }
            }
            Log.d("TAG", "안임")
            Log.d("TAG룸", "${db.RoomDao().getAll()}")
        }

        Log.d("확인", "확인")

        binding.mainRecylerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.mainRecylerview.setHasFixedSize(true)
        binding.mainRecylerview.adapter = Recycler_main(diarylist)

        Log.d("TAG", "밖임")

        binding.allDiary.setOnClickListener {
            var intent = Intent(context, Content_create::class.java)
            startActivity(intent)
        }

        /*if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        }
        else{
            makeRequest()
        }*/
    }


    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode){ // requestpermissions을 통해 arrayof는 grantresults로, requestcode는 그대로 가져와진다.
            0 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) { // grantResult가 비어있을시 혹은 0번째 확인(읽고 쓰기)가 거절인지, 1번째 확인(카메라) 가 거절인지 확인.
                    Toast.makeText(this, "권한 거부됨.", Toast.LENGTH_SHORT).show()
                }
                else {
                }
            }
        }
        return
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA), 0)
    }*/
}