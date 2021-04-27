package com.diary.diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diary.diary.databinding.ActivityPasswordBinding

class password : AppCompatActivity() {
    lateinit var binding:ActivityPasswordBinding
    var passwordarray:ArrayList<Int> = arrayListOf()
    var password_check_array:ArrayList<Int> = arrayListOf()
    var aa = 0
    var toast:Toast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.password0.setOnClickListener {
            password_add(0)
        }
        binding.password1.setOnClickListener {
            password_add(1)
        }
        binding.password2.setOnClickListener {
            password_add(2)
        }
        binding.password3.setOnClickListener {
            password_add(3)
        }
        binding.password4.setOnClickListener {
            password_add(4)
        }
        binding.password5.setOnClickListener {
            password_add(5)
        }
        binding.password6.setOnClickListener {
            password_add(6)
        }
        binding.password7.setOnClickListener {
            password_add(7)
        }
        binding.password8.setOnClickListener {
            password_add(8)
        }
        binding.password9.setOnClickListener {
            password_add(9)
        }
        binding.password0.setOnClickListener {
            password_add(0)
        }
        binding.passwordCancel.setOnClickListener {
            if (passwordarray.isNotEmpty()) {
                if(passwordarray.size < 4) {
                    passwordarray.removeAll(passwordarray)
                    binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                }
                else if (intent.hasExtra("비밀번호 설정")){
                    password_check_array.removeAll(password_check_array)
                    binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                }
            }
        }

        binding.passwordBack.setOnClickListener {
            if (passwordarray.isNotEmpty()) {
                when(passwordarray.size){
                    1 -> binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                    2 -> binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                    3 -> binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                    4 -> binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                }
                if(passwordarray.size < 4)
                    passwordarray.removeAt(passwordarray.size - 1)
                else if(intent.hasExtra("비밀번호 설정")) {
                        when (password_check_array.size) {
                            1 -> binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                            2 -> binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                            3 -> binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                            4 -> binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                        }
                        password_check_array.removeAt(password_check_array.size - 1)
                }
            }
        }

    }

    fun password_add(int:Int){
        if (passwordarray.size == 4) {
            if(intent.hasExtra("비밀번호 설정")) {
                password_check(int)
            }
        }
        if (passwordarray.size < 4) {
            passwordarray.add(int)
            when (passwordarray.size) {
                1 -> binding.passImage1.setBackgroundResource(R.drawable.password_input)
                2 -> binding.passImage2.setBackgroundResource(R.drawable.password_input)
                3 -> binding.passImage3.setBackgroundResource(R.drawable.password_input)
                4 -> binding.passImage4.setBackgroundResource(R.drawable.password_input)
            }
        }

        if(passwordarray.size == 4 && aa == 0){
            if(intent.hasExtra("비밀번호 설정")) {
                //if(intent.hasExtra("비밀번호 설정")){
                binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                binding.passwordCheck.setText("비밀번호를 한번 더 입력해주세요.")
                aa = 1
            }
        }
    }

    fun password_check(int:Int){
        if(password_check_array.size < 4) {
            password_check_array.add(int)
            when(password_check_array.size){
                1 -> binding.passImage1.setBackgroundResource(R.drawable.password_input)
                2 -> binding.passImage2.setBackgroundResource(R.drawable.password_input)
                3 -> binding.passImage3.setBackgroundResource(R.drawable.password_input)
                4 -> binding.passImage4.setBackgroundResource(R.drawable.password_input)
            }
        }

        if(password_check_array.size == 4) {
            for (i in passwordarray.indices) {
                if (password_check_array[i] != passwordarray[i]) {
                    if(toast == null)
                        toast = Toast.makeText(this, "비밀번호가 다릅니다.", Toast.LENGTH_SHORT)
                    else {
                        toast!!.cancel()
                        toast = Toast.makeText(this, "비밀번호가 다릅니다.", Toast.LENGTH_SHORT)
                    }
                    toast!!.show()
                    break
                }
                else{
                    //비밀번호를 저장시키기. 잃어버렸을 경우를 대비한 힌트 다이얼로그 생성하기.
                }
            }
        }
    }
}