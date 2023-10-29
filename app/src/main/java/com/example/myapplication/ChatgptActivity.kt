package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityChatgptBinding
import com.jakewharton.rxbinding2.widget.RxTextView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")

class ChatgptActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatgptBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatgptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idfromMakeStory = intent.getStringExtra("idInput")

        //sentence 확인
        val sentenceStream = RxTextView.textChanges(binding.sentence)
            .skipInitialValue()
            .map { sentence -> sentence.isEmpty() }
        sentenceStream.subscribe {
            showSentenceExistAlert(it)
        }

        //chatgpt 버튼 활성화
        val invalidFieldStream = sentenceStream.map { sentenceInvalid -> !sentenceInvalid }

        invalidFieldStream.subscribe { isValid ->
            if (isValid) {
                binding.usebtn.isEnabled = true
                binding.usebtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.baseColor)
                binding.usebtn.setTextColor(
                    ContextCompat.getColorStateList(
                        this,
                        R.color.colorText
                    )
                )
            } else {
                binding.usebtn.isEnabled = false
                binding.usebtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)
            }
        }

        //버튼
        //뒤로가기 버튼
        binding.backbtn.setOnClickListener {
            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)
            startActivity(intent)
        }
        //사용 버튼
        binding.usebtn.setOnClickListener {
            val sentence = binding.sentence.text.toString()
            /*val intent = Intent(this, PageListActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)*/
            sndRcvServer(sentence, intent)
            /*startActivity(intent)*/
        }
        //비사용 버튼
        binding.notusebtn.setOnClickListener {
            /*val intent = Intent(this, PageListActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)*/
            /*startActivity(intent)*/
        }
    }

    //문장 입력 경고
    private fun showSentenceExistAlert(isNotValid: Boolean) {
        binding.sentence.error = if (isNotValid) "chatGPT 사용 시 문장 입력 필수" else null
    }

    //7번 함수
    //서버로 문장 보내고 split된 배열 받기
    private fun sndRcvServer(snd: String, intent: Intent) {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        var client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "7")
        json.put("sentence", snd)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to convert story: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()

                    if (bodyString != null) {
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")
                            val changedStory = jsonObject.getJSONArray("answer")

                            runOnUiThread {
                                if (dataString == "1") {
                                    Toast.makeText(
                                        applicationContext,
                                        "split된 이야기 받기 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    println("changedStory: "+changedStory)

                                    val answers = Array(changedStory.length()) { i -> changedStory.getString(i) }
                                    println("kotlinarray: "+ Arrays.toString(answers))

                                    /*//다음 페이지에 보내줄 내용 배열
                                    intent.putExtra("splittedStory", changedStory)
                                    startActivity(intent)*/

                                } else if (dataString == "0") {
                                    Toast.makeText(
                                        applicationContext,
                                        "split된 이야기 받기 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: JSONException) {
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "서버에서 받은 값 없음: " + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "문장 서버 전송 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }

    //JSON 배열을 Kotlin 배열로 변경
    private fun jsonArraytoKotlinArray(jsonArray: JSONArray): Array<String>{
        // 1. JSONArray를 Kotlin List로 변환합니다.
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.getString(i)
            list.add(value)
        }

        // 2. Kotlin List를 Kotlin 배열로 변환합니다.
        return list.toTypedArray()
    }
}