package com.diary.diary

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.*
import com.diary.diary.databinding.ActivityMainBinding
import com.diary.recycler.Recycler_main
import com.diary.recycler.list
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.IOException


// 리무브 버튼은 0번째 리스트부터 하나씩 넣어가기(바이트형 이미지와 스트링형 edit도)
@Entity
data class Diaryroom(//id, 날짜, 제목, 내용, 태그, layout 두개.
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "date") val date: Long,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "content") val content: String,
        @ColumnInfo(name = "uri_string_array") val uri_string_array: List<String?> //스트링형으로 변환.*/
)

class Imagelist {
    @TypeConverter
    fun uristringToJson(value: List<String?>) = Gson().toJson(value)

    @TypeConverter
    fun jsonToUriString(value: String) = Gson().fromJson(value, Array<String?>::class.java).toList()

}

@Dao
interface DiaryDao{
    @Insert
    fun insertDao(vararg diaryroom: Diaryroom)

    @Query("DELETE FROM Diaryroom")
    fun DeleteDao()

    @Query("SELECT * FROM Diaryroom")
    fun getAll():List<Diaryroom>
}


@Database(entities = [Diaryroom::class], version = 1)
@TypeConverters(Imagelist::class)
abstract class RoomdiaryDB:RoomDatabase(){
    abstract fun RoomDao():DiaryDao
}

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var diarylist: ArrayList<list> = arrayListOf() //이거 이미지도 추가하기.
    lateinit var db: RoomdiaryDB

    companion object {
        val PICTURE_REQUEST = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("TAG", "get 된다.")

        db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java, "RoomDB"
        )
                .build()

        //db.RoomDao().DeleteDao()
        CoroutineScope(Dispatchers.IO).launch { // Room DB는 mainthread에서 못가져온다.
            val a = db.RoomDao().getAll().size - 1

            if (db.RoomDao().getAll().isNotEmpty()) {
                Log.d("TAG디비", db.RoomDao().getAll().size.toString())
                for (i in db.RoomDao().getAll().size - 1 downTo 0) {
                    val room: Diaryroom = db.RoomDao().getAll()[i]
                    val id: Int = room.id
                    val date: Long = room.date
                    val title: String = room.title
                    val content: String = room.content
                    val uri = room.uri_string_array

                    diarylist.add(list(id, date, title, content, uri))
                }
            }
        }

        Log.d("TAG", "확인")

        binding.mainRecylerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.mainRecylerview.setHasFixedSize(true)
        binding.mainRecylerview.adapter = Recycler_main(diarylist)



        Log.d("TAG", "밖임")

        binding.allDiary.setOnClickListener {
            var intent = Intent(this, Content_create::class.java)
            startActivity(intent)
        }
    }
}