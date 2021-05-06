package com.diary.diary

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diary.diary.databinding.ActivitySettingBinding

class SettingViewmodel: ViewModel(){
    var darkmode = MutableLiveData<String>()
    var diarylock = MutableLiveData<String>()

    var datadrive = MutableLiveData<String>()
    var noteback = MutableLiveData<String>()
    var shortcuts = MutableLiveData<String>()
    var usedtag = MutableLiveData<String>()
    var diary_instruction = MutableLiveData<String>()

    fun darkClick(){
        darkmode.value = "CHECK"
    }

    fun diarylockclick(){
        diarylock.value = "CHECK"
    }
}
class Setting : AppCompatActivity() {
    lateinit var binding:ActivitySettingBinding
    lateinit var settingviewmodel: SettingViewmodel
    lateinit var sharedPreferences:SharedPreferences

    companion object{
        var darkmodechagend = "OFF"
        var diarylock = "OFF"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("LOCK_PASSWORD", 0)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        settingviewmodel = ViewModelProvider(this).get(SettingViewmodel::class.java)
        binding.lifecycleOwner = this
        binding.setting = settingviewmodel

        binding.diaryCount.setText("현재까지 작성한 일기는 ${MainActivity.room.size}개 입니다.")

        if(sharedPreferences.getString("LOCK_CHECK", "").toString().isNotEmpty())
            diarylock = sharedPreferences.getString("LOCK_CHECK", "").toString()

        when(darkmodechagend){
            "ON" -> binding.darkSwitch.isChecked = true
            "OFF" -> binding.darkSwitch.isChecked = false
        }

        when(diarylock){
            "ON" -> binding.lockSwitch.isChecked = true
            "OFF" -> binding.lockSwitch.isChecked = false
        }
        observe()
    }

    fun observe(){
        val editor = sharedPreferences.edit()

        settingviewmodel.darkmode.observe(this, {
            when(darkmodechagend){
                "ON" -> {
                    darkmodechagend = "OFF"
                    binding.darkSwitch.isChecked = false
                }
                "OFF" -> {
                    darkmodechagend = "ON"
                    binding.darkSwitch.isChecked = true
                }
            }
        })

        settingviewmodel.diarylock.observe(this, {
            when(diarylock){
                "ON" -> {
                    editor.putString("LOCK_CHECK", "OFF")
                    diarylock = "OFF"
                    editor.apply()
                    binding.lockSwitch.isChecked = false
                    Log.d("후후", sharedPreferences.getString("LOCK_CHECK", "").toString())
                }
                "OFF" ->{
                    if(sharedPreferences.getString("PASSWORD", "").toString().isEmpty()) { //비밀번호 설정이 안되어있을 경우
                        var intent = Intent(this, password::class.java)
                        intent.putExtra("비밀번호 설정", "비밀번호 설정")
                        startActivity(intent)
                    }
                    else{ //비밀번호 설정되어있으면.
                        editor.putString("LOCK_CHECK", "ON")
                        diarylock = "ON"
                        editor.apply()
                        binding.lockSwitch.isChecked = true
                        Log.d("후후", sharedPreferences.getString("LOCK_CHECK", "").toString())
                    }
                    //비밀번호 설정 되어있는지 확인, 안되어있으면 설정하고 ON으로 넘어가기.
                }
            }
        })
    }
}
/*
* intent로 액티비티 만든곳에 구글 드라이브 복원 및 백업 구현하고 데이터 초기화 누를시 모든 리사이클러뷰, room 데이터 삭제시키기.
* /이미지들 스크린샷찍어서 설명하기.
                    var intent = Intent(this, tag_setting::class.java)
                    intent.putExtra("태그 설정", taglist_array)
                    startActivity(intent)
* */