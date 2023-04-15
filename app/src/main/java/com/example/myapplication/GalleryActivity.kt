package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityGalleryBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.http.Multipart
import java.io.File
import java.io.IOException

class GalleryActivity : AppCompatActivity(){
    private lateinit var binding: ActivityGalleryBinding
    private var imageUri: Uri? = null
    private lateinit var launcher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launcher = registerForActivityResult(
            ActivityResultContracts.GetContent())
        {uri: Uri? -> if (uri != null){
            imageUri = uri
            uploadImage(imageUri!!)
            binding.imgview.setImageURI(imageUri)
        } }

        binding.gallerybtn.setOnClickListener{
            openGallery()
        }
    }

    //갤러리 열고 사진 선택
    private fun openGallery(){
        launcher.launch("image/*")
    }

    //절대경로로 변경
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
        var client = OkHttpClient()

        val filePath: String = getPathFromUri(uri)
        val file = File(filePath)

        val json = JSONObject()
        json.put("image", file)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                Thread{
                    var str = response.body?.string()
                    /*Toast.makeText(applicationContext, "response\n"+str, Toast.LENGTH_SHORT).show()*/
                    println(str)
                }.start()
            }
        })
    }


}