package com.diary.diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.diary.diary.databinding.ActivityShortCutsBinding

class short_cuts : AppCompatActivity() {
    lateinit var binding:ActivityShortCutsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShortCutsBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}