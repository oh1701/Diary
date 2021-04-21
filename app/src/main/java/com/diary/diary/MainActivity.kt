package com.diary.diary

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.*
import com.bumptech.glide.Glide
import com.diary.diary.databinding.ActivityMainBinding
import com.diary.recycler.Recycler_main
import com.diary.recycler.list
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.IOException


// 리무브 버튼은 0번째 리스트부터 하나씩 넣어가기(바이트형 이미지와 스트링형 edit도)
// uri null 허용시키기.

@Entity
data class Diaryroom(//id, 날짜, 제목, 내용, 태그, 이미지uri, 에딧text
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "date") val date: Long,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "content") val content: String,
        @ColumnInfo(name = "uri_string_array") val uri_string_array: List<String?>, //스트링형으로 변환.*/
        @ColumnInfo(name = "edit_string_array") val edit_string_array: List<String?>,
        @ColumnInfo(name = "edit_font") val edit_font:String,
        @ColumnInfo(name = "edit_color") val edit_color:String,
        @ColumnInfo(name = "edit_linespacing") val linespacing:Float,
        @ColumnInfo(name = "edit_letterspacing") val letterspacing:Float
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

    @Query("DELETE FROM Diaryroom")
    suspend fun DeleteDao()

    @Query("SELECT * FROM Diaryroom")
    suspend fun getAll():List<Diaryroom>
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
    lateinit var room:List<Diaryroom>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("TAG", "get 된다.")

        binding.mainRecylerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java, "RoomDB"
        )
                .build()

        fun loadBitmapFromMediaStoreBy(photoUri: Uri?): Bitmap? { //이미지 uri 비트맵형식으로 변경하기
            var image: Bitmap? = null
            try {
                image = if (Build.VERSION.SDK_INT > 27) { // Api 버전별 이미지 처리
                    val source: ImageDecoder.Source =
                            ImageDecoder.createSource(this.contentResolver, photoUri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri!!)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return image
        }

        //db.RoomDao().DeleteDao()

        CoroutineScope(Dispatchers.IO).launch { // Room DB는 mainthread에서 못가져온다.
            CoroutineScope(Dispatchers.IO).launch {
                var d = 0
                if (db.RoomDao().getAll().isNotEmpty()) {
                    room = db.RoomDao().getAll()

                    for (i in room.size - 1 downTo 0) {
                        val id: Int = room[i].id
                        val date: Long = room[i].date
                        val title: String = room[i].title
                        val content: String = room[i].content
                        val font:String = room[i].edit_font
                        val uri = room[i].uri_string_array
                        val editstr = room[i].edit_string_array

                        if(editstr.isNotEmpty()){
                            Log.d("확인", editstr[0].toString())
                        } //47개 불러오는데 4초.
                        d++
                        diarylist.add(list(id, date, title, content, uri, font))
                    }
                }

                Log.d("확인", "모두 불러와짐, $d")
            }.join()


            CoroutineScope(Dispatchers.Main).launch { //Ui 관련이니 Dispachers.main 사용.
                binding.mainRecylerview.setHasFixedSize(true)
                binding.mainRecylerview.adapter = Recycler_main(diarylist)
            }
        }

        binding.allDiary.setOnClickListener {
            var intent = Intent(this, Content_create::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //네비게이션바 열린것이 아니면 종료하도록 만들기.
    }
}