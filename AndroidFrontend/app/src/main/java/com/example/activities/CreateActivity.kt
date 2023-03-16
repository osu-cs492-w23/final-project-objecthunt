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

class CreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        // connect to the node server
        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        val mSocket = SocketHandler.getSocket()

        val usernameEntry = findViewById<EditText>(R.id.hostusername)
        val minEntry = findViewById<EditText>(R.id.time_minute)
        val secEntry = findViewById<EditText>(R.id.time_second)
        val CreateBtn = findViewById<Button>(R.id.buttonCreate)

        val intentChat = Intent(this, ChatActivity::class.java)
        val sharedPreference = getSharedPreferences("settings", MODE_PRIVATE)
        val editor = sharedPreference.edit()

        CreateBtn.setOnClickListener {
            val nickname = usernameEntry.text.toString()
            val min = minEntry.text.toString()
            val sec = secEntry.text.toString()
            val timelimit = calculateTime(min, sec)

            // Check if both values are empty or not
            if (!TextUtils.isEmpty(nickname) && timelimit > 0) {
                mSocket.emit("echoTest", "CONNECTED")
                mSocket.emit("createRoom", JSONObject("{'nickname': ${nickname}, 'itemsList': ['mouse', 'bottle'], 'timeLimit': ${timelimit}}"), Ack { args ->
                    Log.d("CreateActivity", "Ack ${((args.get(0) as JSONObject).get("room") as JSONObject)}")
                    if ("${((args[0] as JSONObject)).get("status")}" == "ok") {
                        editor.putString("roomID", "${((args.get(0) as JSONObject).get("room") as JSONObject).get("roomID")}")
                        editor.putString("host", nickname)
                        editor.putInt("timelimit", timelimit)
                        editor.commit()
                        startActivity(intentChat)

                        // Make the user go to the main screen
                        finish()
                    }
                    else {
                        Log.d("Create Activity", "Unable to create a room")
                    }
                })
                Log.d("Create", "Hello?")

                // Need to block the back button or
                // warn the user that if they press the back button, it will go to the main
            }
        }
    }

    fun calculateTime(min: String, sec: String): Int {
        var result: Int = 0

        try {
            result = min.toInt() * 60 + sec.toInt()

        } catch (e: NumberFormatException ) {
            Log.d("Calcuation: ", "Invalid inputs")
            return -1
        }

        return result
    }
}