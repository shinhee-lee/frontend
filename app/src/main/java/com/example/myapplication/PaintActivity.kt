package com.example.myapplication

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.MediaStore
import android.text.Layout
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.PaintView.Companion.colorList
import com.example.myapplication.PaintView.Companion.currentBrush
import com.example.myapplication.PaintView.Companion.pathList
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class PaintActivity : AppCompatActivity() {
    /*private lateinit var binding: ActivityPaintBinding*/

    companion object{
        var path = Path()
        var paintBrush = Paint()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*binding = ActivityPaintBinding.inflate(layoutInflater)
        setContentView(binding.root)*/

        setContentView(R.layout.activity_paint)

        val idfromMakeStory = intent.getStringExtra("idInput")
        var category: String
        var changedImage: String

        val backbtn = findViewById<Button>(R.id.backbtn)
        val penbtn = findViewById<ImageButton>(R.id.penbtn)
        val eraser = findViewById<ImageButton>(R.id.clearbtn)
        val savebtn = findViewById<ImageButton>(R.id.savebtn)
        val rView = findViewById<View>(R.id.relativeView)
        val sentence = findViewById<EditText>(R.id.sentence)

        //그림판 배경 색 하얀색으로 지정
        rView.setBackgroundColor(Color.WHITE)

        supportActionBar?.hide()

        //버튼
        //뒤로가기 버튼
        backbtn.setOnClickListener {
            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)
            startActivity(intent)
        }
        //펜 버튼
        penbtn.setOnClickListener{
            Toast.makeText(this, "펜 버튼을 누르셨습니다.", Toast.LENGTH_SHORT).show()
            paintBrush.color = Color.BLACK
            currentColor(paintBrush.color)
        }
        //모두 지우기 버튼
        eraser.setOnClickListener{
            Toast.makeText(this, "모두 지우기 버튼을 누르셨습니다.", Toast.LENGTH_SHORT).show()
            pathList.clear()
            colorList.clear()
            path.reset()
        }
        //저장 버튼
        savebtn.setOnClickListener{
            val popupMenu = PopupMenu(applicationContext, it)
            val pic = getBitmapFromView(rView)
            val intent = Intent(this, ShowPaintActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)

            menuInflater?.inflate(R.menu.popcategory, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.objectbtn -> {
                        category = "o"  //카테고리 지정
                        Toast.makeText(applicationContext, "오브젝트 선택", Toast.LENGTH_SHORT).show()
                        sndServer(intent, pic!!, category, idfromMakeStory!!, sentence.toString())  //서버로 전송 -> DB에 저장
                        return@setOnMenuItemClickListener true
                    }
                    R.id.backgroundbtn -> {
                        category = "b"
                        Toast.makeText(applicationContext, "배경 선택", Toast.LENGTH_SHORT).show()
                        sndServer(intent, pic!!, category, idfromMakeStory!!, sentence.toString())
                        return@setOnMenuItemClickListener true
                    }else -> {
                    return@setOnMenuItemClickListener false
                }
                }
            }

            /*if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ){
                //권한 요청
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
            } else{
                val pic = getBitmapFromView(rView)
                saveOnGallery(pic!!)
            }*/
        }
    }

    //색 지정
    private fun currentColor(color: Int){
        currentBrush = color
        path = Path()
    }

    //뷰 >> 비트맵
    private fun getBitmapFromView(view: View): Bitmap?{
        if(view.measuredWidth <= 0 || view.measuredHeight <= 0){
            Toast.makeText(this, "저장불가", Toast.LENGTH_SHORT).show()
            return null
        }

        Toast.makeText(this, "비트맵으로 변환 중", Toast.LENGTH_SHORT).show()

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    //갤러리에 저장
    private fun saveOnGallery(bitmap: Bitmap){
        val filename = "${System.currentTimeMillis()}.jpg"

        val values = ContentValues().apply{
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val outputStream = contentResolver.openOutputStream(uri!!)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream?.close()

        Toast.makeText(this, "갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }

    //비트맵 >> base64 인코딩
    private fun bitmapToString(bitmap:Bitmap): String{
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    //이미지 리사이징
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

        //인코딩 (bitmap >> bytearray >> base64string
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val encodedResizedImageString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        outputStream.close()

        return encodedResizedImageString
    }

    //6번 함수
    //서버로 그림 보내기
    private fun sndServer(intent: Intent, bitmap: Bitmap, category: String, id: String, sentence: String) {
        intent.putExtra("categoryFromPaint", category)

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        var client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        //이미지 인코딩
        val encodedString = bitmapToString(bitmap)
        val resizeEncodedString = resizeEncodedImageString(encodedString, 500, 500, 80)

        val json = JSONObject()
        json.put("key", "6")
        json.put("image", resizeEncodedString)
        json.put("sentence", sentence)
        json.put("id", id)
        json.put("category", category)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread{
                    Toast.makeText(applicationContext, "Failed to upload image: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                /*Thread{
                    var str = response.body?.string()
                    println(str)
                }.start()*/
                if (response.isSuccessful){
                    val bodyString = response.body?.string()
                    if(bodyString != null){
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val imageDataString = jsonObject.getString("result")
                            intent.putExtra("imgStringFromPaint", imageDataString)
                            /*changedImage = imageDataString*/
                            /*val imageDataBytes = Base64.decode(imageDataString, Base64.DEFAULT)*/
                            /*val bitmap = BitmapFactory.decodeByteArray(imageDataBytes, 0, imageDataBytes.size)*/

                            runOnUiThread{
                                Toast.makeText(applicationContext, "다음 페이지로 이동 준비 완료", Toast.LENGTH_SHORT).show()
                                startActivity(intent)

                            }
                        } catch (e: JSONException) {
                            runOnUiThread{
                                Toast.makeText(applicationContext, "Failed to parse server response: " + e.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else{
                    runOnUiThread{
                        Toast.makeText(applicationContext, "Failed to download image", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        })
    }

    //갤러리 접근 허용
    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //권한이 허용되면 이미지 선택 화면으로 넘어감
                *//*getBitmapFromView(v)*//*
                saveOnGallery(v)
            } else{
                Toast.makeText(applicationContext, "권한 에러", Toast.LENGTH_SHORT)
            }
        }
    }*/
}