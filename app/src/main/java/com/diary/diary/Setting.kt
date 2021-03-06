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
            binding.diaryCount.setText("???????????? ????????? ????????? ${MainActivity.room.size}??? ?????????.")

        observe()
    }

    override fun onResume() {
        super.onResume()

        binding.shortcutSize.text = "?????? ${short_size}?????? ???????????? ???????????????."

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

    fun observe() {
        val editor = sharedPreferences.edit()

        settingviewmodel.darkmode.observe(this, { //????????????
            when (darkmodechagend) {
                "ON" -> {
                    editor.putString("DARK_MODE", "OFF")
                    editor.apply()
                    darkOFF()
                    Log.d("??????11", "??????11")
                }
                "OFF" -> {
                    editor.putString("DARK_MODE", "ON")
                    editor.apply()
                    darkON()
                    Log.d("??????22", "??????22")
                }
            }
        })

        settingviewmodel.diarylock.observe(this, { //?????? ??????
            when (diarylock) {
                "ON" -> {
                    diarylock = "OFF"
                    editor.putString("LOCK_CHECK", "OFF")
                    editor.apply()
                    binding.lockSwitch.isChecked = false
                    Log.d("??????", sharedPreferences.getString("LOCK_CHECK", "").toString())
                }
                "OFF" -> {
                    if (sharedPreferences.getString("PASSWORD", "").toString().isEmpty()) { //???????????? ????????? ??????????????? ??????
                        var intent = Intent(this, password::class.java)
                        intent.putExtra("???????????? ??????", "???????????? ??????")
                        startActivity(intent)
                        binding.lockSwitch.isChecked = false
                        Log.d("?????????1", "??????11")
                    }
                    else { //???????????? ?????????????????????.
                        diarylock = "ON"
                        editor.putString("LOCK_CHECK", "ON")
                        editor.apply()
                        binding.lockSwitch.isChecked = true
                        Log.d("?????????2", "??????22")
                    }
                    //???????????? ?????? ??????????????? ??????, ?????????????????? ???????????? ON?????? ????????????.
                }
            }
        })

        settingviewmodel.datadrive.observe(this, {
            toast = when (toast) {
                null -> Toast.makeText(this, "???????????? ???????????????.", Toast.LENGTH_SHORT)
                else -> {
                    toast!!.cancel()
                    Toast.makeText(this, "???????????? ???????????????.", Toast.LENGTH_SHORT)
                }
            }
            toast!!.show()
        })

        settingviewmodel.backbtn.observe(this, {
            onBackPressed()
            //intentMain()
        })
        
        settingviewmodel.passwordchange.observe(this, {
            if (sharedPreferences.getString("PASSWORD", "").toString().isEmpty()) {
                var intent = Intent(this, password::class.java)
                intent.putExtra("???????????? ??????", "???????????? ??????")
                startActivity(intent)
            }
            else{
                var intent = Intent(this, password::class.java)
                intent.putExtra("???????????? ??????", "???????????? ??????")
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

    fun darkON(){
        binding.darkSwitch.isChecked = true
        darkmodechagend = "ON"
        
        //??????
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
        
        //????????????
        binding.settingMainLayout.setBackgroundColor(Color.parseColor("#272626"))
        binding.dataBackupLayout.setBackgroundResource(R.drawable.layout_click_dark)
        binding.passwordChangeLayout.setBackgroundResource(R.drawable.layout_click_dark)
        binding.passwordHintInput.setBackgroundResource(R.drawable.layout_click_dark)
        binding.shortcutLayout.setBackgroundResource(R.drawable.layout_click_dark)
        binding.usedTagLayout.setBackgroundResource(R.drawable.layout_click_dark)
        //binding.diaryInstructionLayout.setBackgroundResource(R.drawable.layout_click_dark)

        //?????????
        binding.image1.setImageResource(R.drawable.darkmode_arrowright)
        binding.image2.setImageResource(R.drawable.darkmode_arrowright)
        binding.image3.setImageResource(R.drawable.darkmode_arrowright)
        binding.image4.setImageResource(R.drawable.darkmode_arrowright)
        //binding.image5.setImageResource(R.drawable.darkmode_arrowright)
        binding.passwordHintImage.setImageResource(R.drawable.darkmode_arrowright)
        binding.backArrow.setImageResource(R.drawable.darkmode_backarrow)

        //?????? ?????? ??????
        binding.systemSetting.setTextColor(Color.parseColor("#B5B2B2"))
        binding.userConvinence.setTextColor(Color.parseColor("#B5B2B2"))
        binding.shortcutSetting.setTextColor(Color.parseColor("#B5B2B2"))
    }

    fun darkOFF(){
        binding.darkSwitch.isChecked = false
        darkmodechagend = "OFF"
        
        //??????
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

        //????????????
        binding.settingMainLayout.setBackgroundColor(Color.parseColor("#E8E8E8"))
        binding.dataBackupLayout.setBackgroundResource(R.drawable.layout_click)
        binding.passwordChangeLayout.setBackgroundResource(R.drawable.layout_click)
        binding.passwordHintInput.setBackgroundResource(R.drawable.layout_click)
        binding.shortcutLayout.setBackgroundResource(R.drawable.layout_click)
        binding.usedTagLayout.setBackgroundResource(R.drawable.layout_click)
        //binding.diaryInstructionLayout.setBackgroundResource(R.drawable.layout_click)
        
        //??????
        binding.image1.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.image2.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.image3.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.image4.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        //binding.image5.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.passwordHintImage.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
        binding.backArrow.setImageResource(R.drawable.ic_baseline_arrow_back_24)

        //?????? ?????? ??????
        binding.systemSetting.setTextColor(Color.parseColor("#6A6A6A"))
        binding.userConvinence.setTextColor(Color.parseColor("#6A6A6A"))
        binding.shortcutSetting.setTextColor(Color.parseColor("#6A6A6A"))
    }
}
/*
* intent??? ???????????? ???????????? ?????? ???????????? ?????? ??? ?????? ???????????? ????????? ????????? ????????? ?????? ??????????????????, room ????????? ???????????????.
* /???????????? ????????????????????? ????????????.
                    var intent = Intent(this, tag_setting::class.java)
                    intent.putExtra("?????? ??????", taglist_array)
                    startActivity(intent)
* */