package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    /*private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase*/

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*//Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()*/

        //gif logo
        Glide.with(this).load(R.raw.logo1).into(binding.logo01)

        //아이디 확인
        val idStream = RxTextView.textChanges(binding.idInput01)
            .skipInitialValue()
            .map{id -> id.isEmpty()}
        idStream.subscribe{
            showTextMinAlert(it, "UserID")
        }
        //패스워드 확인
        val pwStream = RxTextView.textChanges(binding.pwInput01)
            .skipInitialValue()
            .map{password -> password.isEmpty()}
        idStream.subscribe{
            showTextMinAlert(it, "Password")
        }

        //로그인 버튼 활성화
        val invalidFieldsStream = Observable.combineLatest(
            idStream,
            pwStream,
            { idInvalid: Boolean, pwInvalid: Boolean ->
                !idInvalid && !pwInvalid
            })

        invalidFieldsStream.subscribe{
                isValid ->
            if (isValid){
                binding.loginbtn.isEnabled = true
                binding.loginbtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorBase)
                binding.loginbtn.setTextColor(ContextCompat.getColorStateList(this, R.color.colorText))
            }
            else {
                binding.loginbtn.isEnabled = false
                binding.loginbtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)
            }
        }

        //버튼
        //로그인 버튼
        binding.loginbtn.setOnClickListener{
            val id = binding.idInput01.text.toString().trim()
            val password = binding.pwInput01.text.toString().trim()
            loginUser(id, password)

            /*//입력이 없을 경우
            if(binding.idInput01.text.isNullOrBlank()&&binding.pwInput01.text.isNullOrBlank()){
                Toast.makeText(this, "ID와 PW를 모두 입력하세요", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "${binding.idInput01.text} 로그인 성공", Toast.LENGTH_SHORT).show()
            }*/
/*
            Toast.makeText(this, "${binding.idInput01.text} 로그인 성공", Toast.LENGTH_SHORT).show()
*/
            /*val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()*/
        }

        //회원가입 버튼
        binding.registerbtn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            /*finish()*/
        }

        //아이디 비번 찾기 버튼
        binding.idpwSearchbtn.setOnClickListener {
            val intent = Intent(this, IdpwSearchActivity::class.java)
            startActivity(intent)
            /*finish()*/
        }

    }
    //아이디 최소 길이는 6자, 패스워드 최소 길이는 10자
    private fun showTextMinAlert(isNotValid: Boolean, text: String){
        if(text == "UserID")
            binding.idInput01.error = if (isNotValid) "아이디를 입력하세요." else null
        else if(text == "Password")
            binding.pwInput01.error = if (isNotValid) "패스워드를 입력하세요." else null
    }


    private fun loginUser(id: String, password: String){
        val intent = Intent(this, HomeActivity::class.java)

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        /*val idArray = JSONArray()
            .put("test1")
            .put("test2")
            .put("test3")*/

        val json = JSONObject()
        json.put("key", "1")
        json.put("id", id)
        json.put("password", password)

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
                                    /*Toast.makeText(
                                        applicationContext,
                                        "로그인 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()*/

                                    intent.putExtra("idInput", id) //홈 액티비티로 id값 보내기
                                    intent.putExtra("pwInput", password)
                                    startActivity(intent)   //홈 액티비티로 넘어가기

                                } else if(dataString == "0"){
                                    Toast.makeText(
                                        applicationContext,
                                        "로그인 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch(e: JSONException){
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

        /*val databaseRef = database.reference.child("users")
        val checkUser = databaseRef.orderByChild("id").equalTo(id)

        checkUser.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){

                    binding.idInput01.error = null

                    val idfromDB =
                        snapshot.child(id).child(Info.ID).getValue(
                            String::class.java
                        )
                    val pwfromDB =
                        snapshot.child(id).child(Info.PASSWORD).getValue(
                            String::class.java
                        )

                    if(idfromDB == id){
                        binding.idInput01.error = null

                        if(pwfromDB == password){
                            binding.pwInput01.error = null

                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.putExtra("idInput", id)
                            startActivity(intent)

                            *//*Toast.makeText(this@LoginActivity, "${binding.idInput01.text} 로그인 성공", Toast.LENGTH_SHORT).show()*//*

                        } else{
                            binding.pwInput01.error = "잘못된 패스워드"
                        }
                    } else{
                        binding.idInput01.error = "아이디 재입력"
                    }
                } else{
                    binding.idInput01.error = "존재하지 않는 사용자"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })*/
    }
}