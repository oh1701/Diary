package com.diary.diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diary.diary.databinding.ActivitySettingBinding

class Shortviewmodel: ViewModel(){
    var darkmode = MutableLiveData(0)

    fun darkClick(){
        if(darkmode.value == 0){
            darkmode.value = 1
        }
        else{
            darkmode.value = 0
        }
    }
}
class Setting : AppCompatActivity() {
    lateinit var binding:ActivitySettingBinding
    lateinit var shortviewmodel: Shortviewmodel

    companion object{
        var a = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        shortviewmodel = ViewModelProvider(this).get(Shortviewmodel::class.java)
        binding.lifecycleOwner = this
        binding.setting = shortviewmodel
        
        observe()
    }
    
    fun observe(){
        shortviewmodel.darkmode.observe(this, {
            if (it == 1){
                a = 222
            }
            else {
                a = 111
            }
        })
    }
}