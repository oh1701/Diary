package com.diary.diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diary.diary.databinding.ActivityShortcutsBinding

class ShortcutsViewModel:ViewModel(){

}
class Shortcuts : AppCompatActivity() {

    lateinit var binding:ActivityShortcutsBinding
    lateinit var viewModel:ShortcutsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shortcuts)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_shortcuts)
        viewModel = ViewModelProvider(this).get(ShortcutsViewModel::class.java)
        binding.lifecycleOwner = this
        binding.shortcuts = viewModel
    }
}