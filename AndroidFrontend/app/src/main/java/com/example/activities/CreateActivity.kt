package com.example.chatting.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatting.R
import com.example.chatting.SocketHandler

class CreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        // connect to the node server
        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        val mSocket = SocketHandler.getSocket()

        val usernameEntry = findViewById<EditText>(R.id.hostusername)
        val joinBtn = findViewById<Button>(R.id.buttonCreate)

        val intentChat = Intent(this, ChatActivity::class.java)

        joinBtn.setOnClickListener {
            val nickname = usernameEntry.text.toString()

            // Check if both values are empty or not
            if (!TextUtils.isEmpty(nickname)) {
                //mSocket.emit("createRoom")
                startActivity(intentChat)
                // Need to block the back button or
                // warn the user that if they press the back button, it will go to the main
            }
        }
    }
}