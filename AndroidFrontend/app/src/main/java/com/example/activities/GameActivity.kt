package com.example.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatting.R
import com.example.SocketHandler

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val mSocket = SocketHandler.getSocket()

    }
}