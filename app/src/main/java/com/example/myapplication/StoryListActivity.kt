package com.example.myapplication

import kotlin.text.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityStorylistBinding
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class StoryListActivity : AppCompatActivity(){
    private lateinit var binding: ActivityStorylistBinding
    private var storyIds = mutableListOf<String>()
    private var titles = mutableListOf<String>()
    private lateinit var idfromHome: String
    private var totalPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idIntent = intent.getStringExtra("idInput")
        idfromHome = idIntent!!

        lifecycleScope.launch {
            val deferredData = async { rcvArrays(idfromHome) }
            val (list1, list2) = deferredData.await()

            storyIds.addAll(list1)
            titles.addAll(list2)
            println("storyIds: "+storyIds)

            binding.recyclerView.layoutManager = LinearLayoutManager(this@StoryListActivity)
            binding.recyclerView.adapter = StoryAdapter(titles, storyIds)
        }

        //버튼
        //뒤로가기 버튼
        binding.backbtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("idInput", idfromHome)
            startActivity(intent)
        }
    }

    //recyclingView 어댑터
    inner class StoryAdapter(private val titles: List<String>, private val storyIds: List<String>) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>(){

        override fun getItemCount(): Int {
            return titles.size
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): StoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return StoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: StoryAdapter.StoryViewHolder, position: Int) {
            holder.bind(titles[position])
        }

        inner class StoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
                View.OnClickListener {
            private val titleTextView: TextView = itemView.findViewById(android.R.id.text1)

            init {
                itemView.setOnClickListener(this)
            }

            fun bind(title: String) {
                titleTextView.text = title
            }

            override fun onClick(view: View?) {
                val position = adapterPosition
                val storyId = storyIds[position]
                Toast.makeText(
                    applicationContext,
                    "스토리 열람 페이지로\n storyId: " + storyId,
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@StoryListActivity, ShowStoryActivity::class.java)
                intent.putExtra("storyid", storyId)
                intent.putExtra("idInput", idfromHome)
                /*intent.putExtra("pageCnt", totalPage)*/
                startActivity(intent)
            }
        }
    }

    //storyid, title array들 받는 함수
    //= withContext(Dispatchers.IO)
    fun rcvArrays(id:String): Pair<MutableList<String>, MutableList<String>>{
        var storyIdList = mutableListOf<String>()
        var titleList = mutableListOf<String>()

        val handler = Handler(Looper.getMainLooper())

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
                            val storyIdString = jsonObject.getJSONArray("storyid")
                            val titleString = jsonObject.getJSONArray("title")
                            /*totalPage = jsonObject.getInt("total_page")*/

                            //runonuithread
                            //handler.post
                            runOnUiThread {
                                if (dataString == "1") {
                                    Toast.makeText(applicationContext, "storyid, title 수신 성공", Toast.LENGTH_SHORT).show()

                                    //1) JSONArray >> Array
                                    val storyIdsArray = Array<String>(storyIdString.length()) { i -> storyIdString.getString(i) }
                                    val titlesArray = Array<String>(titleString.length()) { i -> titleString.getString(i) }

                                    //2) Array >> MutableList
                                    storyIdList = storyIdsArray.toMutableList()
                                    titleList = titlesArray.toMutableList()

                                    Toast.makeText(applicationContext, "storyid, title 수신 성공", Toast.LENGTH_SHORT).show()
                                    /*for (i in 0 until storyIdString.length()){
                                        storyIds.add(storyIdString.getString(i))
                                    }
                                    for (i in 0 until titleString.length()){
                                        titles.add(titleString.getString(i))
                                    }*/

                                    /*storyIds = MutableList(storyIdString.length()) {i -> storyIdString.getString(i)}
                                    titles = MutableList(titleString.length()) {i -> titleString.getString(i)}*/

                                    println("storyIdsString: "+storyIdString)
                                    println("storyId List: "+storyIdList)

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
            }
        })
        return Pair(storyIdList, titleList)
        //@withContext
    }
}