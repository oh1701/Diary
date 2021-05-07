package com.diary.diary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        var sharedPreferences = getSharedPreferences("LOCK_PASSWORD", 0)
        var context = this
        CoroutineScope(Dispatchers.Main).launch {
            delay(1200L)
            if(sharedPreferences.getString("LOCK_CHECK", "").toString() != "ON") { // 락이 ON상태일경우
                startActivity(Intent(context, MainActivity::class.java))
            }
            else {
                var intent = Intent(context, password::class.java)
                intent.putExtra("이동", "이동")
                startActivity(intent)
            }
        }
    }
}