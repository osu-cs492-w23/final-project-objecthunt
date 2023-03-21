package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.activities.CreateActivity
import com.example.activities.JoinActivity
import com.example.chatting.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        val intentCreate = Intent(this, CreateActivity::class.java)
        val intentJoin = Intent(this, JoinActivity::class.java)

        val sharedPreference = getSharedPreferences("settings", MODE_PRIVATE)

        val createBtn: Button = findViewById(R.id.buttonCreate)
        val joinBtn: Button = findViewById(R.id.buttonJoin)
        //val cameraBtn: Button = findViewById(R.id.buttonCamera)

        val usernameTV: TextView = findViewById(R.id.username)

        val currentUsername =
            sharedPreference.getString("nickname", "User" + getUniqueNumber(4)).toString()

        Log.d("NEW CURRENT USERNAME!!!: ", currentUsername)
        usernameTV.text = currentUsername


        createBtn.setOnClickListener {
            startActivity(intentCreate)
        }

        joinBtn.setOnClickListener {
            startActivity(intentJoin)
        }

    }

    override fun onResume() {
        super.onResume()
        val usernameTV: TextView = findViewById(R.id.username)
        val sharedPreference = getSharedPreferences("settings", MODE_PRIVATE)

        val currentUsername =
            sharedPreference.getString("nickname", "User" + getUniqueNumber(4)).toString()

        usernameTV.text = currentUsername
        SocketHandler.closeConnection()
    }

    private fun getUniqueNumber(length: Int) = (0..9).shuffled().take(length).joinToString("")
}