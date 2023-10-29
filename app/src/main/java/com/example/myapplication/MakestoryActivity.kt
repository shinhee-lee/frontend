package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMakestoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MakestoryActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMakestoryBinding
    /*private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase*/

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityMakestoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*//Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()*/

        val idfromHome = intent.getStringExtra("idInput")
        val pwfromHome = intent.getStringExtra("pwInput")
        println("idfromHome: $idfromHome")

        //버튼
        //뒤로가기 버튼
        binding.backbtn.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("idInput", idfromHome)
            intent.putExtra("pwInput", pwfromHome)
            startActivity(intent)
        }
        binding.downloadbtn.setOnClickListener{
            val intent = Intent(this, SocialActivity::class.java)
            intent.putExtra("idInput", idfromHome)
            intent.putExtra("pwInput", pwfromHome)
            startActivity(intent)
            /*finish()*/
        }
        //이미지 가져오기
        binding.gallerybtn.setOnClickListener{
            val intent = Intent(this, GalleryActivity::class.java)
            intent.putExtra("idInput", idfromHome)
            intent.putExtra("pwInput", pwfromHome)
            startActivity(intent)
            /*finish()*/
        }
        /*//직접 그리기(그림판)
        binding.selfdrawbtn.setOnClickListener{
            val intent = Intent(this, PaintActivity::class.java)
            intent.putExtra("idInput", idfromHome)
            startActivity(intent)
            *//*finish()*//*
        }*/
        //텍스트투이미지(글로 이미지 생성)
        binding.texttoimgbtn.setOnClickListener {
            val intent = Intent(this, TextToImageActivity::class.java)
            intent.putExtra("idInput", idfromHome)
            intent.putExtra("pwInput", pwfromHome)
            startActivity(intent)
        }
        //시나리오 생성
        binding.createstorybtn.setOnClickListener {
            val intent = Intent(this, CreateScenarioActivity::class.java) //ChatgptActivity
            intent.putExtra("idInput", idfromHome)
            intent.putExtra("pwInput", pwfromHome)
            startActivity(intent)
            /*finish()*/
        }
    }
}