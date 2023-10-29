package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityIdpwsearchBinding
import com.jakewharton.rxbinding2.widget.RxTextView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class IdpwSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIdpwsearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityIdpwsearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //뒤로가기 버튼
        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        //닫기 버튼
        binding.closebtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        //이메일 전송 버튼
        binding.emailsndbtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            sendEmail(email)
        }

        //이메일 확인
        val emailStream = RxTextView.textChanges(binding.emailInput)
            .skipInitialValue()
            .map { email -> !Patterns.EMAIL_ADDRESS.matcher(email).matches() }
        emailStream.subscribe {
            showEmailValidAlert(it)
        }

        //전송 버튼 활성화
        val invalidFieldStream = emailStream.map { emailInvalid ->
            !emailInvalid
        }

        invalidFieldStream.subscribe { isValid ->
            if (isValid) {
                binding.emailsndbtn.isEnabled = true
                binding.emailsndbtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.baseColor)
                binding.emailsndbtn.setTextColor(
                    ContextCompat.getColorStateList(
                        this,
                        R.color.colorText
                    )
                )

            } else {
                binding.emailsndbtn.isEnabled = false
                binding.emailsndbtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)
            }
        }
    }

    //이메일 형식 확인
    private fun showEmailValidAlert(isNotValid: Boolean) {
        binding.emailInput.error = if (isNotValid) "Email 형식이 유효하지 않습니다." else null
    }

    //이메일 전송
    private fun sendEmail(email: String){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        var client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "14")
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
                        "Failed to send: " + e.message,
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
                                    binding.stateText.setText("성공적으로 보냈습니다. 메일함을 확인해주세요.")
                                } else if(dataString == "0"){
                                    binding.stateText.setText("가입되지 않은 이메일 주소입니다.")
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