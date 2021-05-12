package com.diary.diary

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.diary.diary.databinding.ActivitySettingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewmodel: ViewModel(){
    var darkmode = MutableLiveData<String>()
    var diarylock = MutableLiveData<String>()
    var datadrive = MutableLiveData<String>()

    var passwordchange = MutableLiveData<String>()
    var passwordhint =MutableLiveData<String>()
    var shortcuts = MutableLiveData<String>()
    var usedtag = MutableLiveData<String>()
    var diary_instruction = MutableLiveData<String>()
    var backbtn = MutableLiveData<String>()

    fun darkClick(){
        darkmode.value = "CHECK"
    }

    fun diarylockclick(){
        diarylock.value = "CHECK"
    }

    fun datadriveClick(){
        datadrive.value = "CLICK"
    }

    fun backClick(){
        backbtn.value = "CLICK"
    }

    fun passchangeClick(){
        passwordchange.value = "CLICK"
    }

    fun passwordhintClick(){
        passwordhint.value = "CLICK"
    }

    fun shortcutsClick(){
        shortcuts.value = "CLICK"
    }

    fun usedtagClick(){
        usedtag.value = "CLICK"
    }

    fun diary_instructionClick(){
        diary_instruction.value = "CLICK"
    }
}
class Setting : AppCompatActivity() {
    lateinit var binding:ActivitySettingBinding
    lateinit var settingviewmodel: SettingViewmodel
    lateinit var sharedPreferences:SharedPreferences
    var toast: Toast? = null

    companion object{
        var darkmodechagend = "OFF"
        var diarylock = "OFF"
        var short_size = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("LOCK_PASSWORD", 0)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        settingviewmodel = ViewModelProvider(this).get(SettingViewmodel::class.java)
        binding.lifecycleOwner = this
        binding.setting = settingviewmodel

        if(MainActivity.roomcheck)
            binding.diaryCount.setText("현재까지 작성한 일기는 ${MainActivity.room.size}개 입니다.")

        observe()
    }

    override fun onResume() {
        super.onResume()

        binding.shortcutSize.text = "현재 ${short_size}개의 단축키가 존재합니다."

        if(sharedPreferences.getString("LOCK_CHECK", "").toString().isNotEmpty())
            diarylock = sharedPreferences.getString("LOCK_CHECK", "").toString()

        if(sharedPreferences.getString("DARK_MODE", "").toString().isNotEmpty())
            darkmodechagend = sharedPreferences.getString("DARK_MODE", "").toString()

        when(darkmodechagend){
            "ON" -> {
                darkON()
            }
            "OFF" -> {
                darkOFF()
            }
        }

        when(diarylock){
            "ON" -> binding.lockSwitch.isChecked = true
            "OFF" -> binding.lockSwitch.isChecked = false
        }
    }

    override fun onBackPressed() {
        intentMain()
    }

    fun observe() {
        val editor = sharedPreferences.edit()

        settingviewmodel.darkmode.observe(this, { //다크모드
            when (darkmodechagend) {
                "ON" -> {
                    editor.putString("DARK_MODE", "OFF")
                    editor.apply()
                    darkOFF()
                    Log.d("실행11", "실행11")
                }
                "OFF" -> {
                    editor.putString("DARK_MODE", "ON")
                    editor.apply()
                    darkON()
                    Log.d("실행22", "실행22")
                }
            }
        })

        settingviewmodel.diarylock.observe(this, { //일기 잠금
            when (diarylock) {
                "ON" -> {
                    diarylock = "OFF"
                    editor.putString("LOCK_CHECK", "OFF")
                    editor.apply()
                    binding.lockSwitch.isChecked = false
                    Log.d("후후", sharedPreferences.getString("LOCK_CHECK", "").toString())
                }
                "OFF" -> {
                    if (sharedPreferences.getString("PASSWORD", "").toString().isEmpty()) { //비밀번호 설정이 안되어있을 경우
                        var intent = Intent(this, password::class.java)
                        intent.putExtra("비밀번호 설정", "비밀번호 설정")
                        startActivity(intent)
                        binding.lockSwitch.isChecked = false
                        Log.d("실행므1", "실행11")
                    }
                    else { //비밀번호 설정되어있으면.
                        diarylock = "ON"
                        editor.putString("LOCK_CHECK", "ON")
                        editor.apply()
                        binding.lockSwitch.isChecked = true
                        Log.d("실행므2", "실행22")
                    }
                    //비밀번호 설정 되어있는지 확인, 안되어있으면 설정하고 ON으로 넘어가기.
                }
            }
        })

        settingviewmodel.datadrive.observe(this, {
            toast = when (toast) {
                null -> Toast.makeText(this, "준비중인 기능입니다.", Toast.LENGTH_SHORT)
                else -> {
                    toast!!.cancel()
                    Toast.makeText(this, "준비중인 기능입니다.", Toast.LENGTH_SHORT)
                }
            }
            toast!!.show()
        })

        settingviewmodel.backbtn.observe(this, {
            intentMain()
        })
        
        settingviewmodel.passwordchange.observe(this, {
            if (sharedPreferences.getString("PASSWORD", "").toString().isEmpty()) {
                var intent = Intent(this, password::class.java)
                intent.putExtra("비밀번호 설정", "비밀번호 설정")
                startActivity(intent)
            }
            else{
                var intent = Intent(this, password::class.java)
                intent.putExtra("비밀번호 변경", "비밀번호 변경")
                startActivity(intent)
            }
        })

        settingviewmodel.passwordhint.observe(this, {
                startActivity(Intent(this, Passwordhint::class.java))
        })

        settingviewmodel.shortcuts.observe(this, {
            startActivity(Intent(this, Shortcuts::class.java))
        })

        settingviewmodel.usedtag.observe(this, {
            startActivity(Intent(this, tag_setting::class.java))
        })
    }

    fun intentMain(){
        startActivity(Intent(this, MainActivity::class.java))
    }
    fun darkON(){
        binding.darkSwitch.isChecked = true
        darkmodechagend = "ON"
        
        //글자
        binding.settingMenu.setTextColor(Color.parseColor("#FB9909"))
        binding.passwordChange.setTextColor(Color.WHITE)
        binding.dataBackup.setTextColor(Color.WHITE)
        binding.diaryLock.setTextColor(Color.WHITE)
        binding.darkmode.setTextColor(Color.WHITE)
        binding.shortcut.setTextColor(Color.WHITE)
        binding.usedTag.setTextColor(Color.WHITE)
        //binding.diaryInstruction.setTextColor(Color.WHITE)
        binding.diaryCount.setTextColor(Color.WHITE)
        binding.passwordHint.setTextColor(Color.WHITE)
        binding.shortcutSize.setTextColor(Color.WHITE)
        
        //레이아웃
        binding.settingMainLayout.setBackgroundColor(Color.parseColor("#272626"))
        binding.dataBackupLayout.setBackgroundResource(R.drawable.layout_click_dark)
        binding.passwordChangeLayout.setBackgroundResource(R.drawable.layout_click_dark)
        binding.passwordHintInput.setBackgroundResource(R.drawable.layout_click_dark)
        binding.shortcutLayout.setBackgroundResource(R.drawable.layout_click_dark)
        binding.usedTagLayout.setBackgroundResource(R.drawable.layout_click_dark)
        //binding.diaryInstructionLayout.setBackgroundResource(R.drawable.layout_click_dark)

        //이미지
        binding.image1.setImageResource(R.drawable.darkmode_arrowright)
        binding.image2.setImageResource(R.drawable.darkmode_arrowright)
        binding.image3.setImageResource(R.drawable.darkmode_arrowright)
        binding.image4.setImageResource(R.drawable.darkmode_arrowright)
        //binding.image5.setImageResource(R.drawable.darkmode_arrowright)
        binding.passwordHintImage.setImageResource(R.drawable.darkmode_arrowright)
        binding.backArrow.setImageResource(R.drawable.darkmode_backarrow)

        //보조 메뉴 글자
        binding.systemSetting.setTextColor(Color.parseColor("#B5B2B2"))
        binding.userConvinence.setTextColor(Color.parseColor("#B5B2B2"))
        binding.shortcutSetting.setTextColor(Color.parseColor("#B5B2B2"))
    }

    fun darkOFF(){
        binding.darkSwitch.isChecked = false
        darkmodechagend = "OFF"
        
        //글자
        binding.settingMenu.setTextColor(Color.BLACK)
        binding.passwordChange.setTextColor(Color.BLACK)
        binding.dataBackup.setTextColor(Color.BLACK)
        binding.diaryLock.setTextColor(Color.BLACK)
        binding.darkmode.setTextColor(Color.BLACK)
        binding.shortcut.setTextColor(Color.BLACK)
        binding.usedTag.setTextColor(Color.BLACK)
        binding.passwordHint.setTextColor(Color.BLACK)
        //binding.diaryInstruction.setTextColor(Color.BLACK)
        binding.diaryCount.setTextColor(Color.BLACK)
        binding.shortcutSize.setTextColor(Color.BLACK)

        //레이아웃
        binding.settingMainLayout.setBackgroundColor(Color.parseColor("#E8E8E8"))
        binding.dataBackupLayout.setBackgroundResource(R.drawable.layout_click)
        binding.passwordChangeLayout.setBackgroundResource(R.drawable.layout_click)
        binding.passwordHintInput.setBackgroundResource(R.drawable.layout_click)
        binding.shortcutLayout.setBackgroundResource(R.drawable.layout_click)
        binding.usedTagLayout.setBackgroundResource(R.drawable.layout_click)
        //binding.diaryInstructionLayout.setBackgroundResource(R.drawable.layout_click)
        
        //사진
        binding.image1.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.image2.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.image3.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.image4.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        //binding.image5.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.passwordHintImage.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.backArrow.setImageResource(R.drawable.ic_baseline_arrow_back_24)

        //보조 메뉴 글자
        binding.systemSetting.setTextColor(Color.parseColor("#6A6A6A"))
        binding.userConvinence.setTextColor(Color.parseColor("#6A6A6A"))
        binding.shortcutSetting.setTextColor(Color.parseColor("#6A6A6A"))
    }
}
/*
* intent로 액티비티 만든곳에 구글 드라이브 복원 및 백업 구현하고 데이터 초기화 누를시 모든 리사이클러뷰, room 데이터 삭제시키기.
* /이미지들 스크린샷찍어서 설명하기.
                    var intent = Intent(this, tag_setting::class.java)
                    intent.putExtra("태그 설정", taglist_array)
                    startActivity(intent)
* */