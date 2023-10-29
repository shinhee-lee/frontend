package com.example.myapplication

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityGalleryBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.http.Multipart
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.MediaCodec.MetricsConstants.MIME_TYPE
import android.opengl.ETC1.encodeImage
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.util.toAndroidPair
import com.example.temporary.CustomImageView
import com.google.gson.JsonArray
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")

class GalleryActivity : AppCompatActivity(), View.OnTouchListener {
    private lateinit var binding: ActivityGalleryBinding
    private var imageUri: Uri? = null
    private lateinit var launcher: ActivityResultLauncher<String>
    private val REQUEST_CODE = 100

    private lateinit var category: String
    private lateinit var changedImage: String
    private lateinit var viewImage: String //전송할 사진 encoded string

    private var coordinateArray: Array<Float> = arrayOf(0f, 0f)

    private lateinit var img1:String
    private lateinit var img2:String

    var checkInt = 0
    var checkInt2 = 0
    @SuppressLint("ClickableViewAccessibility")     //삭제?
    override fun onCreate(savedInstanceState: Bundle?){
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idfromMakeStory = intent.getStringExtra("idInput")
        val pwfromMakeStory = intent.getStringExtra("pwInput")

        //터치 리스너 설정(좌표 지정)
        binding.imgview.setOnTouchListener(this)

        launcher = registerForActivityResult(
            ActivityResultContracts.GetContent())
        {uri: Uri? -> if (uri != null){
            imageUri = uri
            //sentence와 img 서버로 보내기
            val handler = Handler(Looper.getMainLooper())
            val delayMillis = 3000
            var isTimerRunning = true // 타이머 상태를 나타내는 변수 추가

            val timerRunnable = object : Runnable {
                override fun run() {
                    if (checkInt == 0 && isTimerRunning) { // 타이머 상태를 검사하여 실행 여부 결정
                        sendSentenceImageToServer(imageUri!!)
                        // finalImg가 아직 값이 없으면 주기적으로 확인합니다.
                    } else {
                        // 원하는 처리가 끝났으면 타이머를 중지합니다.
                        isTimerRunning = false
                    }

                    if (isTimerRunning) {
                        handler.postDelayed(this, delayMillis.toLong())
                    }
                }
            }

            handler.post(timerRunnable)

            //좌표 저장
            //업로드 이미지
            /*uploadImage(imageUri!!)*/
        } }

        //imgview의 좌표 저장 + imgview의 좌표 변수 coordinateString에 저장

        //문장 들어왔는지 확인
        val sentenceStream = RxTextView.textChanges(binding.sentence)
            .skipInitialValue()
            .map{name -> name.isEmpty()}
        sentenceStream.subscribe {
            showSentenceExistAlert(it)
        }

        //갤러리 열기 버튼 활성화
        val invalidFieldStream = sentenceStream.map {sentenceInvalid -> !sentenceInvalid}
        invalidFieldStream.subscribe{
            isValid ->
            if(isValid){
                binding.gallerybtn.isEnabled = true
                binding.gallerybtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorBase)
                binding.gallerybtn.setTextColor(ContextCompat.getColorStateList(this, R.color.colorText))
            } else{
                binding.gallerybtn.isEnabled = false
                binding.gallerybtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)

                binding.selectbtn.isEnabled = false
                binding.selectbtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)

                binding.maskbtn.isEnabled = false
                binding.maskbtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)
            }
        }


        //버튼
        //뒤로가기 버튼
        binding.backbtn.setOnClickListener{
            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)
            intent.putExtra("pwInput", pwfromMakeStory)
            startActivity(intent)
        }
        //갤러리에서 가져오기 버튼
        binding.gallerybtn.setOnClickListener{
            //권한 허용 확인
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ){
                //권한 요청
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
            } else{
                //갤러리 열어 사진 선택 후
                openGallery()
            }
        }
        //마스크 선택 버튼
        binding.selectbtn.setOnClickListener{
            uploadImage(changedImage, coordinateArray)
            /*var looped = 1
            val handler = Handler(Looper.getMainLooper())
            val delayMillis = 3000
            var isTimerRunning = true // 타이머 상태를 나타내는 변수 추가

            val timerRunnable = object : Runnable {
                override fun run() {
                    if (checkInt2 == 0 && isTimerRunning) { // 타이머 상태를 검사하여 실행 여부 결정
                        if (looped >= 20) {
                            Toast.makeText(applicationContext, "다시 시도", Toast.LENGTH_SHORT).show()
                            isTimerRunning = false // 타이머 중지
                        } else {
                            uploadImage(changedImage, coordinateArray)

                            if (checkInt2 != 0) {
                                isTimerRunning = false // 네트워크 요청이 완료되면 타이머 중지
                            }
                            looped += 1
                        }
                    } else {
                        // 원하는 처리가 끝났으면 타이머를 중지합니다.
                        isTimerRunning = false
                    }

                    if (isTimerRunning) {
                        handler.postDelayed(this, delayMillis.toLong())
                    }
                }
            }

            handler.post(timerRunnable)*/

        }

        binding.maskbtn.setOnClickListener{
            val sentence = binding.sentence.text.toString()
            val intent = Intent(this, SelectMaskActivity::class.java)
            intent.putExtra("maskImg1", img1)
            intent.putExtra("maskImg2", img2)
            intent.putExtra("idInput", idfromMakeStory)
            intent.putExtra("pwInput", pwfromMakeStory)
            intent.putExtra("sentence", sentence)

            startActivity(intent)
        }

        //결정 버튼
        /*binding.okbtn.setOnClickListener{
            val popupMenu = PopupMenu(applicationContext, it)

            menuInflater?.inflate(R.menu.popcategory, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.objectbtn -> {
                        category = "o"  //카테고리 지정
                        chooseCategory(category, changedImage, idfromMakeStory!!)  //서버로 전송 -> DB에 저장
                        Toast.makeText(applicationContext, "오브젝트로 저장", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.backgroundbtn -> {
                        category = "b"
                        chooseCategory(category, changedImage, idfromMakeStory!!)
                        Toast.makeText(applicationContext, "배경으로 저장", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }else -> {
                    return@setOnMenuItemClickListener false
                }
                }
            }
        }*/
        /*binding.okbtn.setOnClickListener{
            saveOnGallery(binding.imgview)
        }*/
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            // 터치 이벤트 처리
            val imageViewLocation = IntArray(2)
            binding.imgview.getLocationOnScreen(imageViewLocation)

            // 좌표 저장
            val x = event.x
            val y = event.y

            coordinateArray = arrayOf(x, y)
            println("coordinateArray: ${coordinateArray[0]}, ${coordinateArray[1]}")

            showPoint(R.drawable.redcirclexml, x, y)
        }
        return true
    }

    //갤러리 열고 사진 선택
    private fun openGallery(){
        launcher.launch("image/*")
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

        //인코딩 (bitmap >> bytearray >> base64string)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val encodedResizedImageString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        outputStream.close()

        return encodedResizedImageString
    }

    //이미지 좌표 표시 >> 수정 필요
    private fun showPoint(resourceId: Int, x: Float, y: Float){
        val bitmapOptions = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeResource(resources, resourceId, bitmapOptions)

        val imageview = ImageView(this)
        imageview.setImageBitmap(bitmap)

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.leftMargin = x.toInt() // x 좌표 설정
        layoutParams.topMargin = y.toInt() // y 좌표 설정
        imageview.layoutParams = layoutParams

        val frameLayout = findViewById<FrameLayout>(R.id.framelayout)
        frameLayout.addView(imageview)
    }

    //서버에 문장과 이미지 보내기 (5번)
    //이미지 변형
    private fun sendSentenceImageToServer(uri:Uri){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        //문장
        val sentence = binding.sentence.text.toString()

        //이미지 >> 인코딩
        val inputStream = contentResolver.openInputStream(uri)
        val buffer = ByteArray(inputStream!!.available())
        inputStream.read(buffer)
        inputStream.close()
        val encodedString = Base64.encodeToString(buffer, Base64.DEFAULT)
        val resizeEncodedString = resizeEncodedImageString(encodedString, 512, 512, 80)

        val json = JSONObject()
        json.put("key", "5")
        json.put("image", resizeEncodedString)
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
                            println("oksign ok")

                            val httpVersion = response.protocol.toString()
                            println("HTTP Version: $httpVersion")

                            runOnUiThread {
                                if (dataString == "1") {
                                    binding.selectbtn.isEnabled = true
                                    binding.selectbtn.backgroundTintList = ContextCompat.getColorStateList(this@GalleryActivity, R.color.colorBase)
                                    binding.selectbtn.setTextColor(ContextCompat.getColorStateList(this@GalleryActivity, R.color.colorText))

                                    checkInt = 1
                                    //변형된 사진 수신
                                    println("변형된 사진 수신 완료")
                                    val imageDataString = jsonObject.getString("result")
                                    changedImage = imageDataString
                                    Toast.makeText(applicationContext, "변형된 사진 수신 완료", Toast.LENGTH_SHORT).show()

                                    //변형된 사진 뷰에 올리기
                                    val imageDataBytes = Base64.decode(imageDataString, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageDataBytes, 0, imageDataBytes.size)

                                    binding.imgview.setImageBitmap(bitmap)

                                } else if (dataString == "0") {
                                    Toast.makeText(applicationContext, "변형된 사진 수신 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: JSONException) {
                            runOnUiThread {
                                Toast.makeText(applicationContext, "서버에서 받은 값 없음: " + e.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else{
                    runOnUiThread{
                        Toast.makeText(applicationContext, "Failed to download image", Toast.LENGTH_SHORT).show()
                    }
                }
                response.close() // Close the response.
            }
        })
    }

    //서버에 이미지 업로드 (16번)
    //누끼 따기
    private fun uploadImage(encodedString: String, coordinate: Array<Float>){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val x = coordinate[0].toString()
        val y = coordinate[1].toString()
        println("x=$x, y=$y")

        val json = JSONObject()
        json.put("key", "16")
        json.put("image", encodedString)
        json.put("point", JSONArray(coordinate))

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        var imageDataString1: String? = null
        var imageDataString2: String? = null
        println("null 지정 완료")

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to upload image: " + e.message,
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
                            println("oksign ok")

                            runOnUiThread{
                                if (dataString == "1"){
                                    imageDataString1 = jsonObject.getString("result1")
                                    imageDataString2 = jsonObject.getString("result2")
                                    checkInt2 = 1
                                    println("result 1, 2 받음")
                                    println("imageDataString1: $imageDataString1")
                                    println("imageDataString2: $imageDataString2")
                                    img1 = imageDataString1!!
                                    img2 = imageDataString2!!

                                    binding.maskbtn.isEnabled = true
                                    binding.maskbtn.backgroundTintList = ContextCompat.getColorStateList(this@GalleryActivity, R.color.colorBase)
                                    binding.maskbtn.setTextColor(ContextCompat.getColorStateList(this@GalleryActivity, R.color.colorText))

                                    Toast.makeText(
                                        applicationContext,
                                        "마스크 수신 완료",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else if(dataString == "0"){
                                    Toast.makeText(
                                        applicationContext,
                                        "마스크 수신 실패",
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
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "회원 정보 서버 전송 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                response.close()
            }
        })
    }

    //변형된 사진 갤러리에 저장 >> 사용x 서버에 전송하면 끝
    private fun saveOnGallery(view: View){
        val imageView = binding?.imgview

        imageView?.let{
            val bitmap = (it.drawable as BitmapDrawable).bitmap

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
    }

    //오브젝트의 카테고리 고르기
    private fun chooseCategory(category: String, image: String, id: String) {

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
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

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback{
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

    //문장은 비워둘 수 없음
    private fun showSentenceExistAlert(isNotValid: Boolean){
        binding.sentence.error = if (isNotValid) "해당 칸은 비워둘 수 없습니다." else null
    }

    //권한 확인
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //권한이 허용되면 이미지 선택 화면으로 넘어감
                openGallery()
            } else{
                Toast.makeText(applicationContext, "권한 에러", Toast.LENGTH_SHORT)
            }
        }
    }


}