package com.example.chatting.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatting.R
import com.example.chatting.SocketHandler
import io.socket.client.Ack

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
            val timelimits = 500

            // Check if both values are empty or not
            if (!TextUtils.isEmpty(nickname)) {
                mSocket.emit("echoTest", "CONNECTED")
                mSocket.emit("createRoom", nickname, timelimits, Ack { args ->
                    Log.d("CreateActivity", "Ack $args")
                    startActivity(intentChat)
                })
                Log.d("Create", "Hello?")

                // Need to block the back button or
                // warn the user that if they press the back button, it will go to the main
            }
        }
    }
}