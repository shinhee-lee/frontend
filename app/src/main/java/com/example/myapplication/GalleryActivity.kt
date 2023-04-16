package com.example.myapplication

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
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
import android.content.ContentValues
import android.graphics.drawable.BitmapDrawable
import android.media.MediaCodec.MetricsConstants.MIME_TYPE
import android.view.View
import org.json.JSONException
import java.util.concurrent.TimeUnit


class GalleryActivity : AppCompatActivity(){
    private lateinit var binding: ActivityGalleryBinding
    private var imageUri: Uri? = null
    private lateinit var launcher: ActivityResultLauncher<String>
    private val REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launcher = registerForActivityResult(
            ActivityResultContracts.GetContent())
        {uri: Uri? -> if (uri != null){
            imageUri = uri
            uploadImage(imageUri!!)
        } }

        binding.gallerybtn.setOnClickListener{
            //권한 허용 확인
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ){
                //권한 요청
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
            } else{
                openGallery()
            }
        }

        binding.okbtn.setOnClickListener{
            saveOnGallery(binding.imgview)
        }
    }

    //갤러리 열고 사진 선택
    private fun openGallery(){
        launcher.launch("image/*")
    }

    //절대경로로 변경 >> contentresolver로 해결
    fun getPathFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        var path = ""
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                if (columnIndex != -1) {
                    path = it.getString(columnIndex) ?: ""
                }
            }
        }
        return path
    }

    //서버에 이미지 업로드
    private fun uploadImage(uri: Uri) {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://192.168.1.38:83/image"
        var client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        /*val filePath: String = getPathFromUri(uri)
        val file = File(filePath)*/

        //contetResolver로 이미지 파일 읽기
        val inputStream = contentResolver.openInputStream(uri)
        val buffer = ByteArray(inputStream!!.available())
        inputStream.read(buffer)
        inputStream.close()
        //읽은 이미지 파일 인코딩
        val encodedString = Base64.encodeToString(buffer, Base64.DEFAULT)

        val json = JSONObject()
        json.put("image", encodedString)

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
                            val imageDataBytes = Base64.decode(imageDataString, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageDataBytes, 0, imageDataBytes.size)

                            runOnUiThread{
                                binding.imgview.setImageBitmap(bitmap)
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

    //변형된 사진 갤러리에 저장
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