package com.diary.diary

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.*
import com.diary.diary.databinding.ActivityMainBinding
import com.diary.recycler.Recycler_main
import com.diary.recycler.list
import com.diary.recycler.tagline
import kotlinx.coroutines.*
import java.util.jar.Manifest

@Entity
data class Diaryroom(//id, 날짜, 제목, 내용, 태그, layout 두개.
        @PrimaryKey(autoGenerate = true) val id:Int,
        @ColumnInfo(name = "date") val date:Long,
        @ColumnInfo(name = "title") val title:String,
        @ColumnInfo(name = "content") val content:String,
        @ColumnInfo(name = "tag_array") val tag_array:ArrayList<tagline>,
        @ColumnInfo(name = "create_linear") val create_linear:ArrayList<LinearLayout?>,
        @ColumnInfo(name = "create_frame") val create_frame:ArrayList<FrameLayout?>
)

@Dao
interface DiaryDao{
    @Query("SELECT * FROM Diaryroom")
    fun getAll():List<Diaryroom>

    @Query("SELECT id FROM Diaryroom")
    fun getid():Int

    @Query("SELECT date FROM Diaryroom")
    fun getdate():Long

    @Query("SELECT title FROM Diaryroom")
    fun gettitle():String

    @Query("SELECT content FROM Diaryroom")
    fun getcontent():String

    @Insert
    fun insertDao(vararg diaryroom: Diaryroom)

    @Query("DELETE FROM Diaryroom")
    fun DeleteDao()
}

@Database(entities = [Diaryroom::class], version = 1)
abstract class RoomdiaryDB:RoomDatabase(){
    abstract fun RoomDao():DiaryDao
}


class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val context = this
        var diarylist:ArrayList<list> = arrayListOf() //이거 이미지도 추가하기.

        var db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java
                , "RoomDB"
        )
                .build()

                //db.RoomDao().DeleteDao()
        CoroutineScope(Dispatchers.IO).launch { // Room DB는 mainthread에서 못가져온다.
            val a = db.RoomDao().getAll().size - 1

            Log.d("사이즈", a.toString())

            if(a == 0){
                var room: Diaryroom = db.RoomDao().getAll()[0]
                var id: Int = room.id
                var date: Long = room.date
                var title: String = room.title
                var content: String = room.content

                diarylist.add(list(id, date, title, content))
            }
            else if(a >= 1){
                for (i in a downTo 0) { // 역순으로 설정하여 최신 데이터가 상단으로 올라오게 만든다.
                    var room: Diaryroom = db.RoomDao().getAll()[i]
                    var id: Int = room.id
                    var date: Long = room.date
                    var title: String = room.title
                    var content: String = room.content

                    diarylist.add(list(id, date, title, content))
                }
            }
            Log.d("TAG", "안임")
            Log.d("TAG룸", "${db.RoomDao().getAll()}")
        }

        Log.d("TAG", "밖임")
        binding.mainRecylerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.mainRecylerview.setHasFixedSize(true)
        binding.mainRecylerview.adapter = Recycler_main(diarylist)

        binding.allDiary.setOnClickListener {
            var intent = Intent(context, Content_create::class.java)
            startActivity(intent)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

        }
        else{
            makeRequest()
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

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
    }
}