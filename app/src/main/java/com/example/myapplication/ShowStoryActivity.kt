package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityShowstoryBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class ShowStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowstoryBinding
    private lateinit var backwardBtn: ImageButton
    private lateinit var forwardBtn: ImageButton
    private var isDoublieClicked = false
    private var lastClickTime: Long = 0

    private lateinit var storyIdfromList: String
    private lateinit var idfromList: String
    private lateinit var pwfromList: String
    /*private var maxpage: Int = 0*/

    override fun onCreate(savedInstanceState: Bundle?){
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityShowstoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyIdintent = intent.getStringExtra("storyid")
        val idIntent = intent.getStringExtra("idInput")
        val pwIntent = intent.getStringExtra("pwInput")
        /*val pageIntent = intent.getIntExtra("pageCnt")*/
        /*val pageIntent = 5*/
        storyIdfromList = storyIdintent!!
        idfromList = idIntent!!
        pwfromList = pwIntent!!
        /*maxpage = pageIntent*/

        backwardBtn = binding.backwardButton
        forwardBtn = binding.forwardButton

        var currentPage: Int = 0

        //맨 처음
        rcvServerandSetViews(idfromList!!, storyIdfromList!!, currentPage)

        //왼쪽 버튼 눌렀을 때
        var lastClickTime: Long = 0
        backwardBtn.setOnClickListener{
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - lastClickTime

            if(elapsedTime <= 500){ // 500ms 이내에 두 번 클릭된 경우
                val intent = Intent(this, SecondStoryListActivity::class.java)
                intent.putExtra("idInput", idfromList)
                intent.putExtra("pwInput", pwfromList)
                startActivity(intent)
            }
            else{ // 일반적인 클릭
                if(currentPage > 0){
                    currentPage = currentPage - 1
                    rcvServerandSetViews(idfromList, storyIdfromList, currentPage)
                }
                else{
                    Toast.makeText(
                        applicationContext,
                        "두 번 누르면 스토리 리스트 화면으로 이동",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            lastClickTime = currentTime
        }

        //오른쪽 버튼 눌렀을 때
        forwardBtn.setOnClickListener{
            currentPage += 1
            rcvServerandSetViews(idfromList, storyIdfromList, currentPage)
            /*while(currentPage <= maxpage){
                currentPage = currentPage + 1
                if(currentPage == maxpage){
                    Toast.makeText(
                        applicationContext,
                        "스토리의 마지막 화면입니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    currentPage = currentPage - 1
                    break
                } else {
                    rcvServerandSetViews(idfromList, storyIdfromList, currentPage)
                    break
                }
            }*/
        }

    }

    //서버 통신 함수 + 이미지뷰, 텍스트뷰 지정
    //return 값은 json으로 받은 값
    fun rcvServerandSetViews(id: String, storyId: String, page: Int){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "11")
        json.put("id", id)
        json.put("storyid", storyId)
        json.put("page", page)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread{
                    Toast.makeText(applicationContext, "Failed to send: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                /*Thread{
                    var str = response.body?.string()
                    println(str)
                }.start()*/
                if (response.isSuccessful){
                    val bodyString = response.body?.string()
                    if(bodyString != null) {
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")

                            runOnUiThread {
                                if (dataString == "1") {
                                    val imageDataString = jsonObject.getString("image")
                                    val imageDataBytes =
                                        Base64.decode(imageDataString, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(
                                        imageDataBytes,
                                        0,
                                        imageDataBytes.size
                                    )
                                    binding.imgview.setImageBitmap(bitmap)

                                    val sentenceDataString = jsonObject.getString("story")
                                    binding.textView.setText(sentenceDataString)
                                } else if (dataString == "0") {
                                    Toast.makeText(
                                        applicationContext,
                                        "마지막 페이지 입니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: JSONException) {
                            runOnUiThread {
                                /*Toast.makeText(
                                    applicationContext,
                                    "서버에서 받은 값 없음: " + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()*/
                                Toast.makeText(
                                    applicationContext,
                                    "마지막 페이지 입니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else{
                    runOnUiThread{
                        /*Toast.makeText(applicationContext, "Failed to download from server", Toast.LENGTH_SHORT).show()*/
                    }
                }
                response.close()
            }
        })
    }

    //이미지 리사이징 함수 >> 필요한가??
    fun resizeEncodedImageString(encodedImageString: String, maxWidth: Int, maxHeight: Int, quality: Int): String {
        //base64 >> bitmap으로 decode
        val decodedByteArray = Base64.decode(encodedImageString, Base64.DEFAULT)
        val decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)

        //원본 높이, 길이
        val originalWidth = decodedBitmap.width
        val originalHeight = decodedBitmap.height

        //원본 비율
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

        val scaledWidth: Int
        val scaledHeight: Int
        if (originalWidth > originalHeight) {
            // 가로로 긴 사진
            scaledWidth = maxWidth
            scaledHeight = (scaledWidth / aspectRatio).toInt()
        } else if (originalHeight > originalWidth) {
            // 세로로 긴 사진
            scaledHeight = maxHeight
            scaledWidth = (scaledHeight * aspectRatio).toInt()
        } else {
            // 정사각형 사진
            scaledWidth = maxWidth
            scaledHeight = maxHeight
        }

        //리사이징
        val scaledBitmap = Bitmap.createScaledBitmap(decodedBitmap, scaledWidth, scaledHeight, true)

        //인코딩 (bitmap >> bytearray >> base64string)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val encodedResizedImageString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        outputStream.close()

        return encodedResizedImageString
    }
}