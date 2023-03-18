package com.example.chatting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.chatting.activities.CameraActivity
import com.example.chatting.activities.CreateActivity
import com.example.chatting.activities.JoinActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentCreate = Intent(this, CreateActivity::class.java)
        val intentJoin = Intent(this, JoinActivity::class.java)
        val intentCamera = Intent(this, CameraActivity::class.java)

        val createBtn: Button = findViewById(R.id.buttonCreate)
        val joinBtn: Button = findViewById(R.id.buttonJoin)
        val cameraBtn: Button = findViewById(R.id.buttonCamera)


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
}