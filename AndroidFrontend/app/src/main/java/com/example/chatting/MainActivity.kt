package com.example.chatting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.chatting.R
import com.example.chatting.activities.ChatActivity
import com.example.chatting.activities.CreateActivity
import com.example.chatting.activities.JoinActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val intentCreate = Intent(this, ChatActivity::class.java)
        val intentCreate = Intent(this, CreateActivity::class.java)
        val intentJoin = Intent(this, JoinActivity::class.java)

        val createBtn: Button = findViewById(R.id.buttonCreate)
        val joinBtn: Button = findViewById(R.id.buttonJoin)


        createBtn.setOnClickListener {
            startActivity(intentCreate)
        }

        joinBtn.setOnClickListener {
            startActivity(intentJoin)
        }
    }
}