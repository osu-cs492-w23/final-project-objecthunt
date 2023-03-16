package com.example.chatting.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatting.R
import com.example.chatting.SocketHandler

class JoinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        // connect to the node server
        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        val mSocket = SocketHandler.getSocket()

        val roomIDEntry = findViewById<EditText>(R.id.roomID)
        val usernameEntry = findViewById<EditText>(R.id.guestusername)
        val joinBtn = findViewById<Button>(R.id.buttonJoin2)

        joinBtn.setOnClickListener {
            val roomID = roomIDEntry.text.toString()
            val nickname = usernameEntry.text.toString()

            // Check if both values are empty or not
            if (!TextUtils.isEmpty(roomID) && !TextUtils.isEmpty(nickname)) {
                mSocket.emit("joinRoom", roomID, nickname)
            }
        }
    }
}