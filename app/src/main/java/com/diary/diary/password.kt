package com.diary.diary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diary.diary.databinding.ActivityPasswordBinding

class password : AppCompatActivity() {
    lateinit var binding: ActivityPasswordBinding
    var password: String = ""
    var password_check: String = ""
    var aa = 0
    var toast: Toast? = null

    companion object {
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("LOCK_PASSWORD", 0)


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
            if (password.isNotEmpty()) {
                if (password.length < 4) {
                    password = ""
                    binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                } else if (intent.hasExtra("비밀번호 설정")) {
                    password_check = ""
                    binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                    binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                }
            }
        }

        binding.passwordBack.setOnClickListener {
            if (password.isNotEmpty()) {
                when (password.length) {
                    1 -> binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                    2 -> binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                    3 -> binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                    4 -> binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                }
                if (password.length < 4 && password.isNotEmpty())
                    password = password.substring(0, password.length - 1)

                else if (intent.hasExtra("비밀번호 설정") && password_check.isNotEmpty()) {
                    when (password_check.length) {
                        1 -> binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                        2 -> binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                        3 -> binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                        4 -> binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                    }
                    if(password_check.length < 4 && password_check.isNotEmpty())
                        password_check = password_check.substring(0, password_check.length - 1)
                }
            }
        }
    }

    fun password_add(int: Int) {
        var vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (intent.hasExtra("비밀번호 설정")) {
            if (password.length == 4) {
                password_check(int)
            }

            if (password.length < 4) {
                password += int.toString()
                when (password.length) {
                    1 -> binding.passImage1.setBackgroundResource(R.drawable.password_input)
                    2 -> binding.passImage2.setBackgroundResource(R.drawable.password_input)
                    3 -> binding.passImage3.setBackgroundResource(R.drawable.password_input)
                    4 -> binding.passImage4.setBackgroundResource(R.drawable.password_input)
                }
            }

            if (password.length == 4 && aa == 0) {
                binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                binding.passwordCheck.setText("비밀번호를 한번 더 입력해주세요.")
                aa = 1
            }
        }
        else{
            if (password.length < 4) {
                password += int.toString()
                when (password.length) {
                    1 -> binding.passImage1.setBackgroundResource(R.drawable.password_input)
                    2 -> binding.passImage2.setBackgroundResource(R.drawable.password_input)
                    3 -> binding.passImage3.setBackgroundResource(R.drawable.password_input)
                    4 -> binding.passImage4.setBackgroundResource(R.drawable.password_input)
                }
            }

            if(password.length == 4 && password == sharedPreferences.getString("PASSWORD", "").toString()){
                var intent = Intent(this, MainActivity::class.java)
                intent.putExtra("비밀번호ON", "비밀번호ON")
                startActivity(intent)
            }
            else if(password.length == 4 && password != sharedPreferences.getString("PASSWORD", "").toString()){
                if (toast == null) {
                    toast = Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT)
                    vibrator.vibrate(300)
                } else {
                    toast!!.cancel()
                    toast = Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT)
                    vibrator.vibrate(300)
                }
                toast!!.show()
                binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                password = ""
                binding.passwordCheck.setText("비밀번호를 다시 입력해주세요.")
                binding.passwordCheck.setTextColor(Color.parseColor("#0E4181"))
            }
        }
    }

    fun password_check(int: Int) { // 비밀번호 설정하는 함수.
        var vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (password_check.length < 4) {
            password_check += int.toString()
            when (password_check.length) {
                1 -> binding.passImage1.setBackgroundResource(R.drawable.password_input)
                2 -> binding.passImage2.setBackgroundResource(R.drawable.password_input)
                3 -> binding.passImage3.setBackgroundResource(R.drawable.password_input)
                4 -> binding.passImage4.setBackgroundResource(R.drawable.password_input)
            }
        }

        if (password_check.length == 4) {
            if (password_check != password) {
                if (toast == null) {
                    toast = Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT)
                    vibrator.vibrate(300)
                } else {
                    toast!!.cancel()
                    toast = Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT)
                    vibrator.vibrate(300)
                }
                toast!!.show()
                binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                password_check = ""
                binding.passwordCheck.setText("비밀번호를 다시 입력해주세요.")
                binding.passwordCheck.setTextColor(Color.parseColor("#0E4181"))
            } else {
                Toast.makeText(this, "실행행", Toast.LENGTH_SHORT).show()
                val editor = sharedPreferences.edit()
                editor.putString("PASSWORD", password) //비밀번호 설정함
                editor.putString("LOCK_CHECK", "ON") //LOCK ON으로 변경함.
                editor.apply()

                Log.d("패스워드확인", sharedPreferences.getString("PASSWORD", "").toString())
                //비밀번호를 저장시키고. 잃어버렸을 경우를 대비한 힌트 다이얼로그 생성하기.
            }
        }
    }
}