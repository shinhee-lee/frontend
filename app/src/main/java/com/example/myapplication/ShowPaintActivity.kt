package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityShowpaintBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ShowPaintActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowpaintBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowpaintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idfromPaint = intent.getStringExtra("idInput")
        val category = intent.getStringExtra("categoryFromPaint")
        val imgString = intent.getStringExtra("imgStringFromPaint")

        //이미지 뷰에 변형된 사진 노출
        val imageDataBytes =
            Base64.decode(imgString, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(
            imageDataBytes,
            0,
            imageDataBytes.size
        )
        binding.imgview.setImageBitmap(bitmap)

        //버튼
        //뒤로가기 버튼
        binding.backbtn.setOnClickListener {
            val intent = Intent(this, PaintActivity::class.java)
            intent.putExtra("idInput", idfromPaint)
            startActivity(intent)
        }
        //저장 버튼
        binding.savebtn.setOnClickListener {
            sndServer(idfromPaint!!, category!!, imgString!!)
            Toast.makeText(applicationContext, "서버 전송 완료", Toast.LENGTH_SHORT).show()
        }
    }

    //서버로 전송
    private fun sndServer(id: String, category: String, image: String){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        var client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "13")
        json.put("id", id)
        json.put("category", category)
        json.put("image", image)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread{
                    Toast.makeText(applicationContext, "Failed to save image: " + e.message, Toast.LENGTH_SHORT).show()
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
                                    //저장 성공 메시지
                                    Toast.makeText(
                                        applicationContext,
                                        "DB에 사진 저장 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if(dataString == "0"){
                                    Toast.makeText(
                                        applicationContext,
                                        "DB에 사진 저장 실패",
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
                                "사진 서버 전송 실패",
                                Toast.LENGTH_SHORT
                            ).show() }
                    }
                }
            }
        })
    }
}