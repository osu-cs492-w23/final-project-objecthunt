package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.chatting.R
import com.example.activities.CameraActivity
import com.example.activities.CreateActivity
import com.example.activities.GameActivity
import com.example.activities.JoinActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentCreate = Intent(this, CreateActivity::class.java)
        val intentJoin = Intent(this, JoinActivity::class.java)
        val intentCamera = Intent(this, CameraActivity::class.java)
        //val intentGame = Intent(this, GameActivity::class.java)

        val createBtn: Button = findViewById(R.id.buttonCreate)
        val joinBtn: Button = findViewById(R.id.buttonJoin)
        val cameraBtn: Button = findViewById(R.id.buttonCamera)

        val usernameTV: TextView = findViewById(R.id.username)

        usernameTV.text = "User" + getUniqueNumber(4)


        createBtn.setOnClickListener {
            startActivity(intentCreate)
        }

        joinBtn.setOnClickListener {
            startActivity(intentJoin)
        }

        cameraBtn.setOnClickListener {
            startActivity(intentCamera)
        }
    }

    fun getUniqueNumber(length: Int) = (0..9).shuffled().take(length).joinToString("")
}