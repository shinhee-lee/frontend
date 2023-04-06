package com.example.myapplication

import android.annotation.SuppressLint
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

@SuppressLint("CheckResult")
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        //닉네임 가져오기
        //로그인 페이지의 id를 가져오고, db에서 해당 id에 대한 닉네임 찾아서 화면에 노출
        val idfromLogin = intent.getStringExtra("idInput")
        if (idfromLogin != null) {
            bringNick(idfromLogin)
        }

        //메뉴 팝업창
        binding.menubtn.setOnClickListener{
            var popupMenu = PopupMenu(applicationContext, it)

            menuInflater?.inflate(R.menu.popmenu, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.fixinfobtn -> {
                        Toast.makeText(applicationContext, "정보 수정 창으로 이동", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.logoutbtn -> {
                        Toast.makeText(applicationContext, "로그아웃", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }else -> {
                    return@setOnMenuItemClickListener false
                    }
                }
            }
        }
    }

    private fun bringNick(id: String){
        val databaseRef = database.reference.child("users")
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
        })
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