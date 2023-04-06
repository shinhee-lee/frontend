package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

@SuppressLint("CheckResult")

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        //닉네임 확인
        val nickStream = RxTextView.textChanges(binding.nickInput02)
            .skipInitialValue()
            .map{name -> name.isEmpty()}
        nickStream.subscribe {
            showNickExistAlert(it)
        }
        //이메일 확인
        val emailStream = RxTextView.textChanges(binding.emailInput02)
            .skipInitialValue()
            .map{email -> !Patterns.EMAIL_ADDRESS.matcher(email).matches()}
        emailStream.subscribe{
            showEmailValidAlert(it)
        }
        //아이디 확인
        val idStream = RxTextView.textChanges(binding.idInput02)
            .skipInitialValue()
            .map{id -> id.length < 6}
        idStream.subscribe{
            showTextMinAlert(it, "UserID")
        }
        //패스워드 확인
        val pwStream = RxTextView.textChanges(binding.pwInput02)
            .skipInitialValue()
            .map{password -> password.length < 10}
        pwStream.subscribe{
            showTextMinAlert(it, "Password")
        }
        //패스워드 체크 확인
        val pwCheckStream = Observable.merge(
            RxTextView.textChanges(binding.pwInput02)
                .skipInitialValue()
                .map{password -> password.toString() != binding.pwCheckInput02.text.toString()},
            RxTextView.textChanges(binding.pwCheckInput02)
                .skipInitialValue()
                .map{checkPassword -> checkPassword.toString() != binding.pwInput02.text.toString()}
        )
        pwCheckStream.subscribe{
            showPwCheckAlert(it)
        }
        //회원가입 버튼 활성화
        val invalidFieldsStream = Observable.combineLatest(
            nickStream,
            emailStream,
            idStream,
            pwStream,
            pwCheckStream,
            { nickInvalid: Boolean, emailInvalid: Boolean, idInvalid: Boolean, pwInvalid: Boolean, pwCheckInvalid: Boolean ->
            !nickInvalid && !emailInvalid && !idInvalid && !pwInvalid && !pwCheckInvalid
            })

        invalidFieldsStream.subscribe{
            isValid ->
            if (isValid){
                binding.registerbtn.isEnabled = true
                binding.registerbtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.baseColor)
            }
            else {
                binding.registerbtn.isEnabled = false
                binding.registerbtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)
            }
        }

        //버튼
        //뒤로가기 버튼
        binding.back.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //회원가입 버튼
        binding.registerbtn.setOnClickListener{
            val email = binding.emailInput02.text.toString().trim()
            val password = binding.pwInput02.text.toString().trim()
            val id = binding.idInput02.text.toString().trim()
            val nickname = binding.nickInput02.text.toString().trim()
            registerUser(id, password, email, nickname)
        }
    }

    //아이디 최소 길이는 6자, 패스워드 최소 길이는 10자
    private fun showTextMinAlert(isNotValid: Boolean, text: String){
        if(text == "UserID")
            binding.idInput02.error = if (isNotValid) "아이디의 최소 길이는 6글자 입니다." else null
        else if(text == "Password")
            binding.pwInput02.error = if (isNotValid) "패스워드 최소 길이는 10글자 입니다." else null
    }

    //닉네임은 비워둘 수 없음
    private fun showNickExistAlert(isNotValid:Boolean){
        binding.nickInput02.error = if (isNotValid) "닉네임은 비워둘 수 없습니다." else null
    }

    //이메일 유효 확인
    private fun showEmailValidAlert(isNotValid: Boolean){
        binding.emailInput02.error = if (isNotValid) "Email 형식이 유효하지 않습니다." else null
    }

    //패스워드 확인
    private fun showPwCheckAlert(isNotValid: Boolean){
        binding.pwCheckInput02.error = if (isNotValid) "패스워드가 다릅니다." else null
    }

    private fun registerUser(id: String, password: String, email: String, nickname: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    /*var map = mutableMapOf<String, String>()
                    map["id"] = id
                    map["pw"] = password
                    map["email"] = email*/
                    Info.ID = id
                    Info.PASSWORD = password
                    Info.EMAIL = email
                    Info.NICKNAME = nickname
                    val databaseRef = database.getReference("users").child(id)
                    databaseRef.setValue(Info)
                    /*databaseRef.setValue(map)*/

                    startActivity(Intent(this, LoginActivity::class.java))
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}