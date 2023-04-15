package com.example.myapplication

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityMainBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //gif logo
        Glide.with(this).load(R.raw.logo1).into(binding.logo00)

        //시작하기 버튼
        binding.startbtn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        /*connServer()*/
    }

    /*//서버 연결 확인 코드 by json, url, okhttp
    private fun connServer(){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

        var url = "http://192.168.1.38:83/api/image"
        val client = OkHttpClient()

        val json = JSONObject()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Thread {
                    var str = response.body?.string()
                    println(str)
                }.start()
            }
        })
    }*/
}

