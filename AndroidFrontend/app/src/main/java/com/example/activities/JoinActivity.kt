package com.example.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.SocketHandler
import com.example.chatting.R
import com.google.android.material.snackbar.Snackbar
import io.socket.client.Ack
import org.json.JSONObject


class JoinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_new)

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

        val defaultValue = sharedPreference.getString("nickname", "User" + getUniqueNumber(4)).toString()
        usernameEntry.setText(defaultValue)

        joinBtn.setOnClickListener {
            val roomID = roomIDEntry.text.toString()
            val nickname = usernameEntry.text.toString()

            // Check if both values are empty or not
            if (!TextUtils.isEmpty(nickname)) {
                mSocket.emit(
                    "joinRoom",
                    JSONObject("{'roomID': ${roomID}, 'nickname': ${nickname}}"),
                    Ack { args ->
                        if ("${((args[0] as JSONObject)).get("status")}" == "ok") {
                            editor.putString("nickname", nickname)

                            mSocket.emit("getChatHistory", Ack { args ->
                                editor.putString(
                                    "chatHistory",
                                    "${((args.get(0) as JSONObject).get("chatHistory"))}"
                                )
                                editor.commit()
                            })

                            editor.commit()
                            Log.d("Join Activity", "Unable to join a room")
                            startActivity(intentChat)
                            finish()

                        } else {
                            Log.d("Join Activity", "Unable to join a room")
                            editor.putString("nickname", nickname)
                            editor.commit()
                            val snackbar = Snackbar
                                .make(
                                    it,
                                    "ERROR: Invalid RoomID. Please check your roomID again.",
                                    Snackbar.LENGTH_LONG
                                )
                            snackbar.show()
                        }
                    })

                // Need to block the back button or
                // warn the user that if they press the back button, it will go to the main
            }
        }

    }

    private fun getUniqueNumber(length: Int) = (0..9).shuffled().take(length).joinToString("")
}