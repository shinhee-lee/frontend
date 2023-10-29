package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class SelectMaskActivity : AppCompatActivity() {
    private lateinit var imgview1: ImageView
    private lateinit var imgview2: ImageView
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var backbtn: Button

    private lateinit var sendString: String //서버로 보낼 선택된 이미지 string
    private lateinit var category: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectmask)
        supportActionBar?.hide()

        //intent 받아오기
        val idfromGallery = intent.getStringExtra("idInput")
        val pwfromGallery = intent.getStringExtra("pwInput")
        val maskImg1 = intent.getStringExtra("maskImg1")
        val maskImg2 = intent.getStringExtra("maskImg2")
        val sentence = intent.getStringExtra("sentence")

        //이미지 뷰에 이미지 띄우기
        //이미지 디코딩 및 리사이징
        val decodeFirst:Bitmap = resizeDecodeImageString(maskImg1!!, 768, 768, 80)
        val decodeSecond:Bitmap = resizeDecodeImageString(maskImg2!!, 768, 768, 80)
        //뷰 찾기
        imgview1 = findViewById(R.id.mask1)
        imgview2 = findViewById(R.id.mask2)
        //뷰에 이미지 띄우기
        imgview1.setImageBitmap(decodeFirst)
        imgview2.setImageBitmap(decodeSecond)

        backbtn = findViewById(R.id.backbtn)
        backbtn.setOnClickListener{
            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", idfromGallery)
            intent.putExtra("pwInput", pwfromGallery)
            startActivity(intent)
        }

        //버튼
        //버튼 찾기
        btn1 = findViewById(R.id.mask1btn)
        btn2 = findViewById(R.id.mask2btn)
        //1번 버튼 누르면 sendString에 maskImg1 넣고,
        //2번 버튼 누르면 sendString에 maskImg2 넣음
        //버튼 누르면 카테고리 선택
        btn1.setOnClickListener{
            sendString = maskImg1
            val popupMenu = PopupMenu(applicationContext, it)

            menuInflater?.inflate(R.menu.popcategory, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.objectbtn -> {
                        category = "o"  //카테고리 지정
                        showPopup(category, sendString, idfromGallery!!, pwfromGallery!!, sentence!!)
                        return@setOnMenuItemClickListener true
                        /*chooseCategory(category, sendString, idfromGallery!!)
                        Toast.makeText(applicationContext, "오브젝트로 저장", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MakestoryActivity::class.java)
                        startActivity(intent)
                        return@setOnMenuItemClickListener true*/
                    }
                    R.id.backgroundbtn -> {
                        category = "b"
                        showPopup(category, sendString, idfromGallery!!, pwfromGallery!!, sentence!!)
                        /*chooseCategory(category, sendString, idfromGallery!!)
                        Toast.makeText(applicationContext, "배경으로 저장", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MakestoryActivity::class.java)
                        startActivity(intent)*/
                        return@setOnMenuItemClickListener true
                    }else -> {
                    return@setOnMenuItemClickListener false
                }
                }
            }
        }
        btn2.setOnClickListener{
            sendString = maskImg2
            val popupMenu = PopupMenu(applicationContext, it)

            menuInflater?.inflate(R.menu.popcategory, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.objectbtn -> {
                        category = "o"  //카테고리 지정
                        showPopup(category, sendString, idfromGallery!!, pwfromGallery!!, sentence!!)
                        /*chooseCategory(category, sendString, idfromGallery!!)
                        Toast.makeText(applicationContext, "오브젝트로 저장", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MakestoryActivity::class.java)
                        startActivity(intent)*/
                        return@setOnMenuItemClickListener true
                    }
                    R.id.backgroundbtn -> {
                        category = "b"
                        showPopup(category, sendString, idfromGallery!!, pwfromGallery!!, sentence!!)
                        /*chooseCategory(category, sendString, idfromGallery!!)
                        Toast.makeText(applicationContext, "배경으로 저장", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MakestoryActivity::class.java)
                        startActivity(intent)*/
                        return@setOnMenuItemClickListener true
                    }else -> {
                    return@setOnMenuItemClickListener false
                }
                }
            }
        }
        //페이지 나가기(startactivity)
    }

    //공유 여부 팝업창
    private fun showPopup(category: String, image: String, id: String, pw: String, sentence: String){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_share, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("공유 여부 창")

        val alertDialog = dialogBuilder.create()

        val yesButton = dialogView.findViewById<Button>(R.id.yesButton)
        val noButton = dialogView.findViewById<Button>(R.id.noButton)

        lateinit var share: String
        yesButton.setOnClickListener{
            share = "1"
            chooseCategory(category, image, id, share, sentence)
            Toast.makeText(applicationContext, "공유되었습니다", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", id)
            intent.putExtra("pwInput", pw)
            startActivity(intent)

            alertDialog.dismiss()
        }
        alertDialog.show()
        noButton.setOnClickListener{
            share = "0"
            chooseCategory(category, image, id, share, sentence)
            Toast.makeText(applicationContext, "공유하지않았습니다", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", id)
            intent.putExtra("pwInput", pw)
            startActivity(intent)

            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    //카테고리 고르는 함수 (13번)
    private fun chooseCategory(category: String, image: String, id: String, share:String, sentence: String) {

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "13")
        json.put("id", id)
        json.put("category", category)
        json.put("image", image)
        json.put("share",share)
        json.put("sentence", sentence)
        println("sentence: "+sentence)

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
                response.close()
            }
        })
    }

    //이미지 리사이징, 디코딩 함수
    private fun resizeDecodeImageString(imageString: String, maxWidth: Int, maxHeight: Int, quality: Int): Bitmap {
        //base64 >> bitmap으로 decode
        val decodedByteArray = Base64.decode(imageString, Base64.DEFAULT)
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

        return scaledBitmap
    }
}