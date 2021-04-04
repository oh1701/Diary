package com.diary.diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.*
import com.diary.diary.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity
data class Diaryroom(
        @PrimaryKey(autoGenerate = true) val id:Int,
        @ColumnInfo(name = "date") val date:Long,
        @ColumnInfo(name = "title") val title:String,
        @ColumnInfo(name = "content") val content:String
)

@Dao
interface DiaryDao{
    @Query("SELECT * FROM Diaryroom")
    fun getAll():List<Diaryroom>

    @Query("SELECT date FROM Diaryroom")
    fun getdate():Long

    @Query("SELECT title FROM Diaryroom")
    fun gettitle():String

    @Insert
    fun insertDao(vararg diaryroom: Diaryroom)

    @Delete
    fun DeleteDao(vararg diaryroom: Diaryroom)
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

        var lis:ArrayList<list> = arrayListOf(list("2021년 04월 04일","오늘은 아무것도 안했다.\n 심심하다"))

        binding.mainRecylerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.mainRecylerview.setHasFixedSize(true)
        binding.mainRecylerview.adapter = Recycler_main(lis)

        var db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java
        , "RoomDB_Main"
        )
                .build()

        CoroutineScope(Dispatchers.IO).launch{
            db.RoomDao().insertDao(Diaryroom(0, 20210404, "제목입니다.","내용입니다"))
            Log.d("TAG", "${db.RoomDao().getAll()}")
        }
    }
}