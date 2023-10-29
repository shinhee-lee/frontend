package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivitySecondstorylistBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class SecondStoryListActivity :AppCompatActivity() {
    private lateinit var binding: ActivitySecondstorylistBinding
    private lateinit var idfromHome: String
    private lateinit var pwfromHome: String

    override fun onCreate(savedInstanceState: Bundle?){
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivitySecondstorylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idIntent = intent.getStringExtra("idInput")
        val pwIntent = intent.getStringExtra("pwInput")
        idfromHome = idIntent!!
        pwfromHome = pwIntent!!

        //버튼
        //뒤로가기 버튼
        binding.backbtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("idInput", idfromHome)
            intent.putExtra("pwInput", pwfromHome)
            startActivity(intent)
        }

        //배열 가져오는 함수 부르기 -> return값 있음
        //val로 선언해서 여기서 배열 만들기
        //그 배열을 파라미터로 하는 버튼 생성 함수 부르기
        rcvArrays(idfromHome) { storyIds, titles ->
            println("list1: " + Arrays.toString(storyIds))
            println("list2: " + Arrays.toString(titles))
            createBtn(storyIds, titles)
        }
    }

    //서버에서 storyid, title 배열 가져오는 함수
    private fun rcvArrays(id: String, callback: (Array<String>, Array<String>) -> Unit){
        var array1: Array<String> = emptyArray()
        var array2: Array<String> = emptyArray()

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "10")
        json.put("id", id)

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
                        "Failed: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()

                    if (bodyString != null) {
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")
                            val storyIdString = jsonObject.getJSONArray("title")
                            val titleString = jsonObject.getJSONArray("storyid")
                            /*totalPage = jsonObject.getInt("total_page")*/

                            runOnUiThread {
                                if (dataString == "1") {
                                    //JSONArray >> Array
                                    val storyIdsArray = Array<String>(storyIdString.length()) { i -> storyIdString.getString(i) }
                                    val titlesArray = Array<String>(titleString.length()) { i -> titleString.getString(i) }

                                    array1 = storyIdsArray
                                    array2 = titlesArray

                                    callback(array1, array2)
                                    println("storyIdsArray: "+ Arrays.toString(storyIdsArray))
                                    println("titlesArray: "+ Arrays.toString(titlesArray))
                                    println("array1: "+ Arrays.toString(array1))
                                    println("array2: "+ Arrays.toString(array2))

                                } else if (dataString == "0") {
                                    Toast.makeText(
                                        applicationContext,
                                        "실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: JSONException) {
                            runOnUiThread { Toast.makeText(applicationContext, "서버에서 받은 값 없음: " + e.message, Toast.LENGTH_SHORT).show() }
                        }
                    } else {
                        runOnUiThread { Toast.makeText(applicationContext, "서버 수신 내용 없음", Toast.LENGTH_SHORT).show() }
                    }
                }
                response.close()
            }
        })
    }

    //배열로 버튼 생성하는 함수
    private fun createBtn(storyIdArr: Array<String>, titleArr: Array<String>){
        val buttonLayout = binding.linearLayout
        val btnStoryIds = storyIdArr
        val btnTitles = titleArr

        for (i in btnTitles.indices) {
            val button = Button(this)

            // 레이아웃 크기
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                50.toPx() // Convert 50dp to pixels
            )
            layoutParams.setMargins(10.toPx(), 10.toPx(), 10.toPx(), 10.toPx())
            button.layoutParams = layoutParams

            // 태그와 텍스트 지정
            button.tag = btnStoryIds[i]
            button.text = btnTitles[i]

            // 버튼의 배경색
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.baseColor_lighter))

            // 텍스트 크기와 색상 설정
            val textSizeInSp = 18
            val textColorResId = R.color.colorText
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp.toFloat())
            button.setTextColor(ContextCompat.getColor(this, textColorResId))

            button.setOnClickListener {
                val intent = Intent(this, ShowStoryActivity::class.java)
                intent.putExtra("idInput", idfromHome)
                intent.putExtra("storyid", btnStoryIds[i])
                intent.putExtra("pwInput", pwfromHome)
                startActivity(intent)
            }

            buttonLayout.addView(button)
        }
    }

    // Extension function to convert dp to pixels
    fun Int.toPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}