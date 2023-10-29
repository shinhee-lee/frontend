package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityModifyinfoBinding
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class ModifyInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModifyinfoBinding

    private lateinit var idfromHome: String
    private lateinit var pwfromHome: String
    private lateinit var nickfromHome: String

    override fun onCreate(savedInstanceState: Bundle?){
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityModifyinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idIntent = intent.getStringExtra("idInput")
        val pwIntent = intent.getStringExtra("pwInput")
        val nickIntent = intent.getStringExtra("nickInput")
        idfromHome = idIntent!!
        pwfromHome = pwIntent!!
        nickfromHome = nickIntent!!

        binding.nickInput.setText(nickfromHome)
        binding.pwInput.setText(pwfromHome)

        //뒤로가기 버튼
        binding.backbtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("idInput", idfromHome)
            intent.putExtra("pwInput", pwfromHome)
            startActivity(intent)
        }
        //닫기 버튼
        binding.closebtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("idInput", idfromHome)
            intent.putExtra("pwInput", pwfromHome)
            startActivity(intent)
        }
        //정보 수정 버튼
        binding.sndbtn.setOnClickListener {
            val nickname = binding.nickInput.text.toString().trim()
            val password = binding.pwInput.text.toString().trim()

            println("id: "+idfromHome)
            println("nickname: "+nickname)
            println("password: "+password)

            modifyInfo(idfromHome, nickname, password)
        }
        //닉네임 확인
        val nickStream = RxTextView.textChanges(binding.nickInput)
            .skipInitialValue()
            .map { nickname -> nickname.toString().trim() }
            .map { nickname -> nickname.isNotEmpty() }

        //패스워드 확인
        val pwStream = RxTextView.textChanges(binding.pwInput)
            .skipInitialValue()
            .map{password -> password.length < 10}
        pwStream.subscribe {
            showTextMinAlert(it, "Password")
        }

    }
    //패스워드 형식 확인
    private fun showTextMinAlert(isNotValid: Boolean, text: String){
        if(text == "Password")
            binding.pwInput.error = if (isNotValid) "패스워드 최소 길이는 10글자 입니다." else null
    }
    //정보 전송
    private fun modifyInfo(id: String, nickname: String, password: String){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        var client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "4")
        json.put("id", id)
        json.put("nickname", nickname)
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
                        "Failed to modify: " + e.message,
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
                                    binding.stateText.setText("성공적으로 수정되었습니다.")
                                } else if(dataString == "0"){
                                    binding.stateText.setText("수정에 실패하였습니다. 다시 시도해주세요.")
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
    }
}