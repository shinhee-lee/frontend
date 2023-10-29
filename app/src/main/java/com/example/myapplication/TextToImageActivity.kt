package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityTexttoimageBinding
import com.jakewharton.rxbinding2.widget.RxTextView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")

class TextToImageActivity :AppCompatActivity() {
    private lateinit var binding: ActivityTexttoimageBinding
    private lateinit var img: String

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityTexttoimageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idfromMakeStory = intent.getStringExtra("idInput")
        val pwfromMakeStory = intent.getStringExtra("pwInput")

        //문장 들어왔는지 확인
        val sentenceStream = RxTextView.textChanges(binding.sentence)
            .skipInitialValue()
            .map{name -> name.isEmpty()}
        sentenceStream.subscribe{
            showSentenceExistAlert(it)
        }

        //버튼 활성화
        val invalidFieldStream = sentenceStream.map{sentenceInvalid -> !sentenceInvalid}
        invalidFieldStream.subscribe{
            isValid ->
            if(isValid){
                binding.sentencebtn.isEnabled = true
                binding.sentencebtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorBase)
                binding.sentencebtn.setTextColor(ContextCompat.getColorStateList(this, R.color.colorText))
            } else{
                binding.sentencebtn.isEnabled = false
                binding.sentencebtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)

                binding.selectbtn.isEnabled = false
                binding.selectbtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)

                binding.rebtn.isEnabled = false
                binding.rebtn.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.darker_gray)
            }
        }


        //버튼 이벤트
        //뒤로가기 버튼
        binding.backbtn.setOnClickListener{
            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)
            intent.putExtra("pwInput", pwfromMakeStory)
            startActivity(intent)
        }
        //완료 버튼
        //버튼 눌리면 selectbtn, rebtn 활성화
        binding.sentencebtn.setOnClickListener {
            binding.announceMent.setText("이미지 변환중")
            sndSentenceRcvImg()
        }
        //결정 버튼
        binding.selectbtn.setOnClickListener {
            showPopup("b", img, idfromMakeStory!!)
        }
        //다시하기 버튼
        binding.rebtn.setOnClickListener {
            binding.announceMent.setText("이미지 변환중")
            sndSentenceRcvImg()
        }
    }

    //서버에 문장 보내고 이미지 받기 (6번)
    private fun sndSentenceRcvImg(){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/

        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        //문장
        val sentence = binding.sentence.text.toString()

        val json = JSONObject()
        json.put("key", "6")
        json.put("sentence", sentence)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        println("문장 전송 완료")

        client.newCall(request).enqueue(object: Callback {
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

                            runOnUiThread {
                                if (dataString == "1") {
                                    binding.selectbtn.isEnabled = true
                                    binding.selectbtn.backgroundTintList = ContextCompat.getColorStateList(this@TextToImageActivity, R.color.colorBase)
                                    binding.selectbtn.setTextColor(ContextCompat.getColorStateList(this@TextToImageActivity, R.color.colorText))

                                    binding.rebtn.isEnabled = true
                                    binding.rebtn.backgroundTintList = ContextCompat.getColorStateList(this@TextToImageActivity, R.color.colorBase)
                                    binding.rebtn.setTextColor(ContextCompat.getColorStateList(this@TextToImageActivity, R.color.colorText))

                                    binding.announceMent.setText("")
                                    //변형된 사진 수신
                                    println("변형된 사진 수신 완료")
                                    val imageDataString = jsonObject.getString("result")
                                    img = imageDataString

                                    /*//변형된 사진 뷰에 올리기
                                    val imageDataBytes = Base64.decode(imageDataString, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageDataBytes, 0, imageDataBytes.size)*/


                                    //이미지 디코딩 및 리사이징
                                    val decodedImg: Bitmap = resizeDecodeImageString(img, 768, 768, 80)

                                    binding.imgview.setImageBitmap(decodedImg)

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
                response.close()
            }
        })
    }

    //서버에 그림 보내기 (13번)
    private fun sndImg(id: String, image: String, category: String, share: String){
        val sentence = binding.sentence.text.toString()

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
        json.put("share",share)
        json.put("sentence", sentence)

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
                                        "이미지를 '배경'에 저장했습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else if(dataString == "0"){
                                    Toast.makeText(
                                        applicationContext,
                                        "이미지 저장에 실패하였습니다.",
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

    //이미지 디코딩 및 리사이징
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

    //공유 여부 팝업창
    private fun showPopup(category: String, image: String, id: String){
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
            sndImg(id, image, category, share)
            Toast.makeText(applicationContext, "공유되었습니다", Toast.LENGTH_SHORT).show()

            alertDialog.dismiss()
        }
        alertDialog.show()
        noButton.setOnClickListener{
            share = "0"
            sndImg(id, image, category, share)
            Toast.makeText(applicationContext, "공유하지않았습니다", Toast.LENGTH_SHORT).show()

            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    //문장은 비워둘 수 없음
    private fun showSentenceExistAlert(isNotValid: Boolean){
        binding.sentence.error = if (isNotValid) "해당 칸은 비워둘 수 없습니다." else null
    }
}