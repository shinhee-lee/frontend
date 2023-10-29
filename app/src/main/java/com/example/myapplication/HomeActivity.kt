package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    /*private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase*/
    private lateinit var idfromLogin: String
    private lateinit var pwfromLogin: String
    private lateinit var nickfromLogin: String

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idIntent = intent.getStringExtra("idInput")
        val pwIntent = intent.getStringExtra("pwInput")
        idfromLogin = idIntent!!
        pwfromLogin = pwIntent!!
        /*//Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()*/

        //닉네임 가져오기
        //로그인 페이지의 id를 가져오고, db에서 해당 id에 대한 닉네임 찾아서 화면에 노출
        if (idfromLogin != null) {
            bringNick(idfromLogin)
        }

        //메뉴 팝업창
        binding.menubtn.setOnClickListener{
            val popupMenu = PopupMenu(applicationContext, it)

            menuInflater?.inflate(R.menu.popmenu, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.fixinfobtn -> {
                        val intent = Intent(this, ModifyInfoActivity::class.java)
                        intent.putExtra("idInput", idfromLogin)
                        intent.putExtra("pwInput", pwfromLogin)
                        intent.putExtra("nickInput", nickfromLogin)
                        startActivity(intent)
                        /*finish()*/
                        return@setOnMenuItemClickListener true
                    }
                    R.id.logoutbtn -> {
                        Toast.makeText(applicationContext, "로그아웃", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        /*finish()*/
                        return@setOnMenuItemClickListener true
                    }else -> {
                    return@setOnMenuItemClickListener false
                    }
                }
            }
        }

        //이야기 만들기 버튼
        binding.createbtn.setOnClickListener{
            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", idfromLogin)
            intent.putExtra("pwInput", pwfromLogin)
            startActivity(intent)
            /*finish()*/
        }

        //이야기 열람 버튼
        binding.readbtn.setOnClickListener {
            val intent = Intent(this, SecondStoryListActivity::class.java)
            intent.putExtra("idInput", idfromLogin)
            intent.putExtra("pwInput", pwfromLogin)
            startActivity(intent)
            /*finish()*/
        }
    }

    //닉네임 변환
    private fun bringNick(id: String){

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "12")
        json.put("id", id)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to call nickname: " + e.message,
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
                            val dataNick = jsonObject.getString("nickname")

                            runOnUiThread{
                                if (dataString == "1"){
                                    nickfromLogin = dataNick
                                    binding.nickname.setText(dataNick)
                                } else if(dataString == "0"){
                                    Toast.makeText(
                                        applicationContext,
                                        "닉네임 부르기 실패",
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
                                "회원 정보 서버 전송 실패",
                                Toast.LENGTH_SHORT
                            ).show() }
                    }
                }
                response.close()
            }
        })
        /*val databaseRef = database.reference.child("users")
        val checkUser = databaseRef.orderByChild("id").equalTo(id)

        checkUser.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val idfromDB =
                    snapshot.child(id).child(Info.ID).getValue(
                        String::class.java
                    )
                val nickfromDB =
                    snapshot.child(id).child(Info.NICKNAME).getValue(
                        String::class.java
                    )

                if(idfromDB == id){
                    binding.nickname.setText(nickfromDB)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })*/
    }

    /*inner class popuplistener : PopupMenu.OnMenuItemClickListener{
        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.fixinfobtn -> Toast.makeText(applicationContext, "정보 수정 창으로 이동", Toast.LENGTH_SHORT).show()
                R.id.logoutbtn -> Toast.makeText(applicationContext, "로그아웃", Toast.LENGTH_SHORT).show()
            }
            return false
        }
    }*/
    /*private fun showPopup(v: View){
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(R.menu.popmenu, popup.menu)
        var listener = onMenuItemClick()
        popup.setOnMenuItemClickListener(listener)
        popup.show()
    }

    private fun onMenuItemClick(item: MenuItem?): Boolean{
        when (item?.itemId) {
            R.id.fixinfobtn -> Toast.makeText(applicationContext, "정보 수정 창으로 이동", Toast.LENGTH_SHORT).show()
            R.id.logoutbtn -> Toast.makeText(applicationContext, "로그아웃", Toast.LENGTH_SHORT).show()
        }
        return item != null
    }*/

    /*private fun popupMenu(){
        val popupMenu = PopupMenu(applicationContext, binding.menubtn)
        popupMenu.inflate(R.menu.popmenu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.fixinfobtn -> {
                    Toast.makeText(applicationContext, "정보 수정 창으로 이동", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.logoutbtn -> {
                    Toast.makeText(applicationContext, "로그아웃", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> true
            }
        }

        binding.menubtn.setOnLongClickListener{
            try {
                val popup = PopupMenu::class.java.getDeclaredField("mPopup")
                popup.isAccessible = true
                val menu = popup.get(popupMenu)
                menu.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(menu, true)
            } catch (e:Exception){
                e.printStackTrace()
            } finally{
                popupMenu.show()
            }
            true
        }
    }*/
}