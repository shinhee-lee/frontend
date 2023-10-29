package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Base64
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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    /*private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase*/

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*//Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()*/

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

        //아이디 중복 확인 버튼 활성화
        val invalidFieldStream = idStream.map { idInvalid ->
            !idInvalid
        }

        invalidFieldStream.subscribe{
                isValid ->
            if (isValid){
                binding.iddupbtn.isEnabled = true
                binding.iddupbtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.baseColor)
                binding.iddupbtn.setTextColor(ContextCompat.getColorStateList(this, R.color.colorText))

            }
            else {
                binding.iddupbtn.isEnabled = false
                binding.iddupbtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)
            }
        }

        //회원가입 버튼 활성화
        val invalidFieldsStream = Observable.combineLatest(
            nickStream,
            emailStream,
            idStream,
            pwStream,
            pwCheckStream,
            { nickInvalid: Boolean, emailInvalid: Boolean, idInvalid: Boolean, pwInvalid: Boolean, pwCheckInvalid: Boolean ->
                !nickInvalid && !emailInvalid && !idInvalid && !pwInvalid && !pwCheckInvalid }
        )

        invalidFieldsStream.subscribe{
            isValid ->
            if (isValid){
                binding.registerbtn.isEnabled = true
                binding.registerbtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.baseColor)
                binding.registerbtn.setTextColor(ContextCompat.getColorStateList(this, R.color.colorText))

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

        //아이디 중복 확인 버튼
        binding.iddupbtn.setOnClickListener{
            val id = binding.idInput02.text.toString().trim()
            idDuplicateCheck(id)
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

    //뒤로가기
    override fun onBackPressed() {
        /*saveData()*/
        super.onBackPressed()
    }


    //아이디 최소 길이는 6자, 패스워드 최소 길이는 10자
    private fun showTextMinAlert(isNotValid: Boolean, text: String){
        if(text == "UserID")
            binding.idInput02.error = if (isNotValid) "아이디의 최소 길이는 6글자 입니다." else null
        else if(text == "Password")
            binding.pwInput02.error = if (isNotValid) "패스워드 최소 길이는 10글자 입니다." else null
    }

    //중복되는 아이디 존재할 경우
    private fun showIdDupAlert(isNotValid: Boolean, text: String){
        binding.iddupbtn.error = if (isNotValid) "중복되는 아이디 존재" else null
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

    //회원가입
    private fun registerUser(id: String, password: String, email: String, nickname: String) {
        val intent = Intent(this, LoginActivity::class.java)

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        var client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "3")
        json.put("id", id)
        json.put("password", password)
        json.put("nickname", nickname)
        json.put("email", email)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to register: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful){
                    val bodyString = response.body?.string()

                    if(bodyString != null){
                        try{
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")

                            runOnUiThread{
                                if (dataString == "1"){
                                    Toast.makeText(
                                        applicationContext,
                                        "회원가입 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(intent)   //로그인 액티비티로 넘어가기
                                } else if(dataString == "0"){
                                    Toast.makeText(
                                        applicationContext,
                                        "회원가입 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch(e:JSONException){
                            runOnUiThread{
                                Toast.makeText(
                                    applicationContext,
                                    "서버에서 받은 값 없음: "+e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else{
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "회원 정보 서버 전송 실패",
                                Toast.LENGTH_SHORT
                            ).show() }
                    }
                }
                response.close()
            }
        })
        /*auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    *//*var map = mutableMapOf<String, String>()
                    map["id"] = id
                    map["pw"] = password
                    map["email"] = email*//*
                    Info.ID = id
                    Info.PASSWORD = password
                    Info.EMAIL = email
                    Info.NICKNAME = nickname
                    val databaseRef = database.getReference("users").child(id)
                    databaseRef.setValue(Info)
                    *//*databaseRef.setValue(map)*//*

                    startActivity(Intent(this, LoginActivity::class.java))
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }*/
    }

    //아이디 중복 확인
    private fun idDuplicateCheck(id: String){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        var client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "2")
        json.put("id", id)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to check duplication: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful){
                    val bodyString = response.body?.string()

                    if(bodyString != null){
                        try{
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")

                            runOnUiThread{
                                if (dataString == "1"){
                                    Toast.makeText(
                                        applicationContext,
                                        "중복된 아이디 없음",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if(dataString == "0"){
                                    Toast.makeText(
                                        applicationContext,
                                        "중복된 아이디 존재",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch(e:JSONException){
                            runOnUiThread{
                                Toast.makeText(
                                    applicationContext,
                                    "서버에서 받은 값 없음: "+e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else{
                        runOnUiThread {
                            Toast.makeText(
                            applicationContext,
                            "아이디 값 서버 전송 실패",
                            Toast.LENGTH_SHORT
                        ).show() }
                    }
                }
                response.close()
            }
        })
    }
}