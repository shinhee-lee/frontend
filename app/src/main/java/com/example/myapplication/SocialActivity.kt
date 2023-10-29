package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.TypedValue
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivitySocialBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class SocialActivity :AppCompatActivity() {
    private lateinit var backbtn: Button
    private lateinit var objectbtn: Button
    private lateinit var backgroundbtn: Button
    private lateinit var objectLayout: LinearLayout
    private lateinit var startbtn: Button

    /*private lateinit var iArray: Array<String>
    private lateinit var sArray: Array<String>*/
    private lateinit var oArray: Array<String>
    private lateinit var bArray: Array<String>
    private lateinit var osArray: Array<String>
    private lateinit var bsArray: Array<String>
    var checkNum = 0

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social)
        supportActionBar?.hide()
        val idfromMakeStory = intent.getStringExtra("idInput")
        val pwfromMakeStory = intent.getStringExtra("pwInput")

        oArray = arrayOf("1")
        osArray = arrayOf("1")
        bArray = arrayOf("1")
        bsArray = arrayOf("1")

        backbtn = findViewById(R.id.backbtn)
        objectbtn = findViewById(R.id.objectbtn)
        backgroundbtn = findViewById(R.id.backgroundbtn)
        objectLayout = findViewById(R.id.linearLayout)
        startbtn = findViewById(R.id.btn)

        val handler = Handler(Looper.getMainLooper())
        val delayMillis = 2000
        var isTimerRunning = true // 타이머 상태를 나타내는 변수 추가

        Toast.makeText(
            applicationContext,
            "오브젝트 수신 중",
            Toast.LENGTH_SHORT
        ).show()
        val context0 = this //수정
        val timerRunnable = object : java.lang.Runnable {
            override fun run() {
                if (checkNum == 0 && isTimerRunning) { // 타이머 상태를 검사하여 실행 여부 결정
                    rcvServer("o")
                    // finalImg가 아직 값이 없으면 주기적으로 확인합니다.
                } else if (checkNum == 1 && isTimerRunning) {
                    rcvServer("b")
                }
                else {
                    startbtn.isEnabled = true
                    startbtn.backgroundTintList = ContextCompat.getColorStateList(context0, R.color.colorBase)
                    objectbtn.isEnabled = true
                    objectbtn.backgroundTintList = ContextCompat.getColorStateList(context0, R.color.colorBase)
                    backgroundbtn.isEnabled = true
                    backgroundbtn.backgroundTintList = ContextCompat.getColorStateList(context0, R.color.colorBase)
                    Toast.makeText(applicationContext, "전체 수신 완료", Toast.LENGTH_SHORT).show()
                    isTimerRunning = false
                }

                if (isTimerRunning) {
                    handler.postDelayed(this, delayMillis.toLong())
                }
            }
        }
        handler.post(timerRunnable)

        //버튼
        //뒤로가기 버튼
        backbtn.setOnClickListener {
            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)
            intent.putExtra("pwInput", pwfromMakeStory)
            startActivity(intent)
        }

        //시작 버튼
        startbtn.setOnClickListener{
            objectLayout.removeAllViews()

            val gridLayout = GridLayout(this)
            gridLayout.columnCount = 4
            gridLayout.rowCount = 4
            println("그리드 레이아웃 생성")

            println("id: $idfromMakeStory")

            val context = this
            thread (start = true){
                for (i in osArray.indices){
                    val button = ImageButton(context)

                    //버튼 크기
                    val layoutParams = GridLayout.LayoutParams()
                    val dpValue = 80
                    val marginValue = 10

                    // 버튼 크기 설정
                    val pxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(), resources.displayMetrics).toInt()
                    layoutParams.width = pxValue
                    layoutParams.height = pxValue

                    // 마진 설정
                    val marginPxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, marginValue.toFloat(), resources.displayMetrics).toInt()
                    layoutParams.setMargins(marginPxValue, marginPxValue, marginPxValue, marginPxValue)

                    Handler(Looper.getMainLooper()).postDelayed({
                        // 버튼에 레이아웃 파라미터 적용
                        button.layoutParams = layoutParams

                        //버튼마다 사진 넣기
                        val stb1 = oArray[i]
                        val bitmap1 = stringToBitmap(stb1)
                        val resizedBitmap1 = resizeBitmap(bitmap1, 300, 300, 80)  //bitmap 리사이징
                        button.setImageBitmap(resizedBitmap1)

                        //버튼 클릭 시 실행될 것 >> 서버에 string 전송 (sndServer)
                        button.setOnClickListener{
                            val stb2 = oArray[i]
                            val sentence = osArray[i]
                            sndServer(idfromMakeStory!!, "o", stb2, sentence)
                        }
                        gridLayout.addView(button)
                    }, 100)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    objectLayout.addView(gridLayout)
                }, 100)
            }
        }

        //오브젝트 버튼
        objectbtn.setOnClickListener {
            objectLayout.removeAllViews()

            val gridLayout = GridLayout(this)
            gridLayout.columnCount = 4
            gridLayout.rowCount = 4
            println("그리드 레이아웃 생성")

            println("id: $idfromMakeStory")

            val context = this
            thread (start = true){
                for (i in osArray.indices){
                    val button = ImageButton(context)

                    //버튼 크기
                    val layoutParams = GridLayout.LayoutParams()
                    val dpValue = 80
                    val marginValue = 10

                    // 버튼 크기 설정
                    val pxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(), resources.displayMetrics).toInt()
                    layoutParams.width = pxValue
                    layoutParams.height = pxValue

                    // 마진 설정
                    val marginPxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, marginValue.toFloat(), resources.displayMetrics).toInt()
                    layoutParams.setMargins(marginPxValue, marginPxValue, marginPxValue, marginPxValue)

                    Handler(Looper.getMainLooper()).postDelayed({
                        // 버튼에 레이아웃 파라미터 적용
                        button.layoutParams = layoutParams

                        //버튼마다 사진 넣기
                        val stb1 = oArray[i]
                        val bitmap1 = stringToBitmap(stb1)
                        val resizedBitmap1 = resizeBitmap(bitmap1, 300, 300, 80)  //bitmap 리사이징
                        button.setImageBitmap(resizedBitmap1)

                        //버튼 클릭 시 실행될 것 >> 서버에 string 전송 (sndServer)
                        button.setOnClickListener{
                            val stb2 = oArray[i]
                            val sentence = osArray[i]
                            sndServer(idfromMakeStory!!, "o", stb2, sentence)
                        }
                        gridLayout.addView(button)
                    }, 100)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    objectLayout.addView(gridLayout)
                }, 100)
            }
        }

        //배경 버튼
        backgroundbtn.setOnClickListener {
            objectLayout.removeAllViews()

            val gridLayout = GridLayout(this)
            gridLayout.columnCount = 4
            gridLayout.rowCount = 4
            println("그리드 레이아웃 생성")

            println("id: $idfromMakeStory")

            val context = this
            thread (start = true){
                for (i in bsArray.indices){
                    val button = ImageButton(context)

                    //버튼 크기
                    val layoutParams = GridLayout.LayoutParams()
                    val dpValue = 80
                    val marginValue = 10

                    // 버튼 크기 설정
                    val pxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(), resources.displayMetrics).toInt()
                    layoutParams.width = pxValue
                    layoutParams.height = pxValue

                    // 마진 설정
                    val marginPxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, marginValue.toFloat(), resources.displayMetrics).toInt()
                    layoutParams.setMargins(marginPxValue, marginPxValue, marginPxValue, marginPxValue)

                    Handler(Looper.getMainLooper()).postDelayed({
                        // 버튼에 레이아웃 파라미터 적용
                        button.layoutParams = layoutParams

                        //버튼마다 사진 넣기
                        val stb1 = bArray[i]
                        val bitmap1 = stringToBitmap(stb1)
                        val resizedBitmap1 = resizeBitmap(bitmap1, 300, 300, 80)  //bitmap 리사이징
                        button.setImageBitmap(resizedBitmap1)

                        //버튼 클릭 시 실행될 것 >> 서버에 string 전송 (sndServer)
                        button.setOnClickListener{
                            val stb2 = bArray[i]
                            val sentence = bsArray[i]
                            sndServer(idfromMakeStory!!, "b", stb2, sentence)
                        }
                        gridLayout.addView(button)
                    }, 100)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    objectLayout.addView(gridLayout)
                }, 100)
            }
        }
    }

    //단위 변경 함수
    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    //사진 디코딩
    private fun stringToBitmap(string:String): Bitmap{
        val imageDataBytes = Base64.decode(string, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageDataBytes, 0, imageDataBytes.size)
    }

    //비트맵 리사이징
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int, quality: Int): Bitmap {
        // 원본 높이, 길이
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // 원본 비율
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

        // 리사이징
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        // 압축
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // 압축된 비트맵 반환
        return BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))
    }

    //17번 함수 (파라미터: key, category)
    //서버에서 배열 받는 함수
    private fun rcvServer(category: String){
        var imageArray: Array<String>? = emptyArray()
        var sentenceArray: Array<String>? = emptyArray()

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val json = JSONObject()
        json.put("key", "17")
        json.put("category", category)
        println("jsonobject 생성")

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
                        "Failed to send:" + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                println("onFailure")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()

                    if (bodyString != null) {
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")
                            runOnUiThread {
                                if (dataString == "1") {
                                    val imageDataArray = jsonObject.getJSONArray("result")
                                    val sentenceDataArray = jsonObject.getJSONArray("sentence")
                                    println("이미지, 문장 받음")

                                    imageArray = jsonArrayToStringArray(imageDataArray)
                                    sentenceArray = jsonArrayToStringArray(sentenceDataArray)
                                    println("함수 안: image, sentence Array 받음")
                                    println("imageArray: "+imageArray!!.joinToString())
                                    println("sentenceArray: "+sentenceArray!!.joinToString())

                                    if(category == "o"){
                                        oArray = imageArray!!
                                        osArray = sentenceArray!!
                                        println("oArray: "+oArray.joinToString())
                                        println("osArray: "+osArray.joinToString())

                                        checkNum = 1
                                        Toast.makeText(
                                            applicationContext,
                                            "물체 수신 완료",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else if(category == "b"){
                                        bArray = imageArray!!
                                        bsArray = sentenceArray!!
                                        println("bArray: "+bArray.joinToString())
                                        println("bsArray: "+bsArray.joinToString())

                                        checkNum = 2
                                        Toast.makeText(
                                            applicationContext,
                                            "배경 수신 완료",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    println("osArray: "+osArray.joinToString())
                                    println("bsArray: "+bsArray.joinToString())


                                } else if (dataString == "0") {
                                    Toast.makeText(
                                        applicationContext,
                                        "오브젝트 수신 실패",
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
                                "수신 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                response.close()
            }
        })
    }

    //jsonArray >> stringArray //아마 수정 필요????
    fun jsonArrayToStringArray(jsonArray: JSONArray): Array<String> {
        val stringArray = Array(jsonArray.length()) { "" }

        for (i in 0 until jsonArray.length()) {
            val element = jsonArray.optString(i)
            stringArray[i] = element
        }
        return stringArray
    }

    //18번 함수 (파라미터: id, category, image)
    //버튼 클릭 시 실행될 함수
    //이미지 string, id, category를 서버로 보내기
    private fun sndServer(id: String, category: String, image: String, sentence: String){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"

        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "18")
        json.put("id", id)
        json.put("category", category)
        json.put("image", image)
        json.put("sentence", sentence)

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
                if (response.isSuccessful){
                    val bodyString = response.body?.string()
                    if(bodyString != null) {
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")

                            runOnUiThread {
                                if (dataString == "1") {
                                    Toast.makeText(
                                        applicationContext,
                                        "서버에 object 송신 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (dataString == "0") {
                                    Toast.makeText(
                                        applicationContext,
                                        "서버에 object 송신 실패",
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
                    }
                } else{
                    runOnUiThread{
                        Toast.makeText(applicationContext, "Failed to send image", Toast.LENGTH_SHORT).show()
                    }
                }
                response.close()
            }
        })
    }
}