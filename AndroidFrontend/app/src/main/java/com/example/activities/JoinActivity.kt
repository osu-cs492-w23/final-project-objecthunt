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
import org.json.JSONObject

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

        val intentChat = Intent(this, ChatActivity::class.java)
        val sharedPreference = getSharedPreferences("settings", MODE_PRIVATE)
        val editor = sharedPreference.edit()

        joinBtn.setOnClickListener {
            val roomID = roomIDEntry.text.toString()
            val nickname = usernameEntry.text.toString()

            // Check if both values are empty or not
            if (!TextUtils.isEmpty(nickname)) {
                mSocket.emit("joinRoom", JSONObject("{'roomID': ${roomID}, 'nickname': ${nickname}}"), Ack { args ->
                    if ("${((args[0] as JSONObject)).get("status")}" == "ok") {
                        editor.putString("guest", nickname)

                        mSocket.emit("getChatHistory", Ack { args ->
                            editor.putString("chatHistory", "${((args.get(0) as JSONObject).get("chatHistory"))}")
                            editor.commit()
                        })

                        editor.commit()
                        startActivity(intentChat)
                        finish()

                    }
                    else {
                        Log.d("Join Activity", "Unable to join a room")
                    }
                })

                // Need to block the back button or
                // warn the user that if they press the back button, it will go to the main
            }
        }

    }
}