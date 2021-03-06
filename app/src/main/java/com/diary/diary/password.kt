package com.diary.diary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diary.diary.Setting.Companion.diarylock
import com.diary.diary.databinding.ActivityPasswordBinding

class password : AppCompatActivity() {
    lateinit var binding: ActivityPasswordBinding
    var password: String = ""
    var password_check: String = ""
    var aa = 0
    var toast: Toast? = null
    var passwordchange = "νμΈμλ¨"

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
                } else if (intent.hasExtra("λΉλ°λ²νΈ μ€μ ") || intent.hasExtra("λΉλ°λ²νΈ λ³κ²½")) {
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

                else if (intent.hasExtra("λΉλ°λ²νΈ μ€μ ") || intent.hasExtra("λΉλ°λ²νΈ λ³κ²½") && password_check.isNotEmpty()) {
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

        if(intent.hasExtra("λΉλ°λ²νΈ λ³κ²½"))
            binding.passwordCheck.setText("νμ¬ μ¬μ©μ€μΈ λΉλ°λ²νΈλ₯Ό μλ ₯νμΈμ.")

    }

    fun password_add(int: Int) {
        var vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (intent.hasExtra("λΉλ°λ²νΈ μ€μ ") || passwordchange == "νμΈ") {
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
                binding.passwordCheck.setText("λΉλ°λ²νΈλ₯Ό νλ² λ μλ ₯ν΄μ£ΌμΈμ.")
                aa = 1
            }
        }
        else if(intent.hasExtra("λΉλ°λ²νΈ λ³κ²½") && passwordchange == "νμΈμλ¨"){
            if (password.length < 4) {
                binding.passwordCheck.setText("νμ¬ μ¬μ©μ€μΈ λΉλ°λ²νΈλ₯Ό μλ ₯νμΈμ.")
                password += int.toString()
                when (password.length) {
                    1 -> binding.passImage1.setBackgroundResource(R.drawable.password_input)
                    2 -> binding.passImage2.setBackgroundResource(R.drawable.password_input)
                    3 -> binding.passImage3.setBackgroundResource(R.drawable.password_input)
                    4 -> binding.passImage4.setBackgroundResource(R.drawable.password_input)
                }
            }

            if (password.length == 4 && password == sharedPreferences.getString("PASSWORD", "").toString()){
                passwordchange = "νμΈ"
                password = ""
                binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                binding.passwordCheck.setText("μλ‘ λ³κ²½ν  λΉλ°λ²νΈλ₯Ό μλ ₯νμΈμ.")
            }
            else if(password.length == 4 && password != sharedPreferences.getString("PASSWORD", "").toString()){
                password = ""
                binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                vibrator.vibrate(300)
                if(sharedPreferences.getString("PASSWORD_HINT", "").toString().isNotEmpty())
                    binding.passwordCheck.setText("λΉλ°λ²νΈκ° μΌμΉνμ§ μμ΅λλ€. \nννΈ : ${sharedPreferences.getString("PASSWORD_HINT", "").toString()}")
                else
                    binding.passwordCheck.setText("λΉλ°λ²νΈκ° μΌμΉνμ§ μμ΅λλ€.")
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
                intent.putExtra("λΉλ°λ²νΈON", "λΉλ°λ²νΈON")
                startActivity(intent)
            }
            else if(password.length == 4 && password != sharedPreferences.getString("PASSWORD", "").toString()){
                if (toast == null) {
                    toast = Toast.makeText(this, "λΉλ°λ²νΈκ° μΌμΉνμ§ μμ΅λλ€.", Toast.LENGTH_SHORT)
                    vibrator.vibrate(300)
                } else {
                    toast!!.cancel()
                    toast = Toast.makeText(this, "λΉλ°λ²νΈκ° μΌμΉνμ§ μμ΅λλ€.", Toast.LENGTH_SHORT)
                    vibrator.vibrate(300)
                }
                toast!!.show()
                binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                password = ""
                if(sharedPreferences.getString("PASSWORD_HINT", "").toString().isNotEmpty())
                    binding.passwordCheck.setText("λΉλ°λ²νΈκ° μΌμΉνμ§ μμ΅λλ€. \n\nλΉλ°λ²νΈ ννΈ : ${sharedPreferences.getString("PASSWORD_HINT", "").toString()}")
                else
                    binding.passwordCheck.setText("λΉλ°λ²νΈκ° μΌμΉνμ§ μμ΅λλ€.")
                binding.passwordCheck.setTextColor(Color.parseColor("#0E4181"))
            }
        }
    }

    fun password_check(int: Int) { // λΉλ°λ²νΈ μ€μ νλ ν¨μ.
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
                    toast = Toast.makeText(this, "λΉλ°λ²νΈκ° μΌμΉνμ§ μμ΅λλ€.", Toast.LENGTH_SHORT)
                    vibrator.vibrate(300)
                } else {
                    toast!!.cancel()
                    toast = Toast.makeText(this, "λΉλ°λ²νΈκ° μΌμΉνμ§ μμ΅λλ€.", Toast.LENGTH_SHORT)
                    vibrator.vibrate(300)
                }
                toast!!.show()
                binding.passImage1.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage2.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage3.setBackgroundResource(R.drawable.password_noinput)
                binding.passImage4.setBackgroundResource(R.drawable.password_noinput)
                password_check = ""
                binding.passwordCheck.setText("λΉλ°λ²νΈλ₯Ό λ€μ μλ ₯ν΄μ£ΌμΈμ.")
                binding.passwordCheck.setTextColor(Color.parseColor("#0E4181"))
            } else {
                if(intent.hasExtra("λΉλ°λ²νΈ μ€μ ")) {
                    Toast.makeText(this, "λΉλ°λ²νΈ μ€μ  μλ£", Toast.LENGTH_SHORT).show()
                    diarylock = "ON"
                }
                else
                    Toast.makeText(this, "λΉλ°λ²νΈ λ³κ²½ μλ£", Toast.LENGTH_SHORT).show()
                
                val editor = sharedPreferences.edit()
                editor.putString("PASSWORD", password) //λΉλ°λ²νΈ μ€μ ν¨
                editor.putString("LOCK_CHECK", "ON") //LOCK ONμΌλ‘ λ³κ²½ν¨.
                editor.apply()

                Log.d("ν¨μ€μλνμΈ", sharedPreferences.getString("PASSWORD", "").toString())
                finish()
            }
        }
    }
}