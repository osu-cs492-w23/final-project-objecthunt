package com.example.chatting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.chatting.R
import com.example.chatting.activities.ChatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, ChatActivity::class.java)

        val goButton: Button = findViewById(R.id.buttonTest)

        goButton.setOnClickListener {
            startActivity(intent)
        }
    }
}