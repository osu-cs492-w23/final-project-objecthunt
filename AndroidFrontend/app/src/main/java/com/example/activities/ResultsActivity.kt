package com.example.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.MainActivity
import com.example.SocketHandler
import com.example.chatting.R

class ResultsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val mSocket = SocketHandler.getSocket()

        // update the score for the results of the game
        val userResultsTV: TextView = findViewById(R.id.user_results_tv)
        val opponentResultsTV: TextView = findViewById(R.id.opponent_results_tv)

        // mSocket.on()

        // update the images/names for object list(s)
        val userObjectsTV: TextView = findViewById(R.id.user_object_list)
        val opponentObjectsTV: TextView = findViewById(R.id.opponent_object_list)
        val userImage: ImageView = findViewById(R.id.user_image)
        val opponentImage: ImageView = findViewById(R.id.opponent_image)

        // mSocket.on()

        // set a click listener for the Main Menu button, could end socket here too
        val mainMenuBtn: Button = findViewById(R.id.buttonMainMenu)
        val intentMainMenu = Intent(this, MainActivity::class.java)
        mainMenuBtn.setOnClickListener {
            // mSocket.close() ??
            startActivity(intentMainMenu)
        }
    }
}