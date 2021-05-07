package com.diary.diary

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diary.diary.databinding.PasswordhintBinding

class HintViewModel:ViewModel(){
    var hintclick = MutableLiveData<String>()

    fun hintClick(){
        hintclick.value = "CLICK"
    }
}

class Passwordhint:AppCompatActivity() {
    lateinit var binding:PasswordhintBinding
    lateinit var viewModel:HintViewModel
    lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.passwordhint)
        viewModel = ViewModelProvider(this).get(HintViewModel::class.java)
        binding.lifecycleOwner = this
        binding.hint = viewModel

        preferences = getSharedPreferences("LOCK_PASSWORD", 0)

        observe()
    }

    fun observe(){
        val editor = preferences.edit()
        viewModel.hintclick.observe(this, {
            editor.putString("PASSWORD_HINT", "${binding.passwordHintEdit.text}")
            editor.apply()
            finish()
        })
    }
}