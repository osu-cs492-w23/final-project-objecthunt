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
import org.json.JSONObject

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val mSocket = SocketHandler.getSocket()

        val winner = intent.getSerializableExtra("winner")
        val room = JSONObject(intent.getSerializableExtra("room").toString())

        // update the score for the results of the game
        val userResultsTV: TextView = findViewById(R.id.user_results_tv)
        val opponentResultsTV: TextView = findViewById(R.id.opponent_results_tv)
        val winnerResultsTV: TextView = findViewById(R.id.winner_results_tv)

        when (winner) {
            "draw" -> {
                winnerResultsTV.text = "Game Draw"
            }
            mSocket.id() -> {
                winnerResultsTV.text = "You won!"
            }
            else -> {
                winnerResultsTV.text = "You lost :("
            }
        }

        val playerListKeys = room.getJSONObject("players").keys()
        while(playerListKeys.hasNext()){
            val currentPlayerID: String = playerListKeys.next()
            val currentPlayerScore: Int = room.getJSONObject("players").getJSONObject(currentPlayerID).getInt("score")
            if(currentPlayerID == mSocket.id()){
                userResultsTV.text = "Your score: $currentPlayerScore"
            } else {
                opponentResultsTV.text = "Opponent score: $currentPlayerScore"
            }
        }


        // set a click listener for the Main Menu button, could end socket here too
        val mainMenuBtn: Button = findViewById(R.id.buttonMainMenu)
        val intentMainMenu = Intent(this, MainActivity::class.java)
        mainMenuBtn.setOnClickListener {
            // mSocket.close() ??
            startActivity(intentMainMenu)
        }
    }
}