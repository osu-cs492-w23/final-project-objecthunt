package com.example.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.SocketHandler
import com.example.chatting.R
import com.example.data.ItemToFind
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject

class GameActivity : AppCompatActivity() {
    var currentItem: ItemToFind? = null
    lateinit var userScoreTV: TextView
    lateinit var opponentScoreTV: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // text views for later UI updates
        userScoreTV = findViewById(R.id.user_score_tv)
        opponentScoreTV = findViewById(R.id.opponent_score_tv)
        val currentObjectTV: TextView = findViewById(R.id.current_object_tv)

        // set a click listener for the camera button
        val cameraBtn: Button = findViewById(R.id.buttonCameraMainScreen)

        // update the object text when the first object is up
        val mSocket = SocketHandler.getSocket()
        mSocket.on("itemGenerated") { params ->
            println("item generated received: $params")
            val itemParsed = params[0] as JSONObject
            runOnUiThread {
                // ...
                currentItem = ItemToFind(
                    itemParsed.getString("name"),
                    itemParsed.getLong("latitude"),
                    itemParsed.getLong("longtitude")
                )
                cameraBtn.isEnabled = true // Enable the camera button
                // ...
            }
            // set Corvallis as a test location
            val currentObjectLocation =
                currentItem?.longtitude?.let {
                    currentItem?.latitude?.let { it1 ->
                        LatLng(
                            it1.toDouble(),
                            it.toDouble()
                        )
                    }
                }

            // get reference to the map object
            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
            // Stuff that updates the UI

            Log.d("Current ITEM:", "$currentItem")

            runOnUiThread {
                currentObjectTV.text = "Current Object: ${currentItem?.name}"
                mapFragment?.getMapAsync { googleMap ->
                    if (currentObjectLocation != null) {
                        addMarker(googleMap, currentObjectLocation)
                    }
                    currentObjectLocation?.let {
                        CameraUpdateFactory.newLatLng(
                            it
                        )
                    }?.let { googleMap.moveCamera(it) }
//            // set camera starting position bounds to set location
////            googleMap.setOnMapLoadedCallback {
////                val bounds = LatLngBounds.builder()
////                bounds.include(testLocation)
////                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
////            }
                }
            }
        }

        // update the object text when the next object is up
        mSocket.on("newItem") { params ->
            val nextItem = params[0] as JSONObject
            val currentRoom = params[1] as JSONObject
            val playerListKeys = currentRoom.getJSONObject("players").keys()
            var userScore = 0
            var opponentScore = 0
            while(playerListKeys.hasNext()){
                val currentPlayerID: String = playerListKeys.next()
                val currentPlayerScore: Int = currentRoom.getJSONObject("players").getJSONObject(currentPlayerID).getInt("score")
                if(currentPlayerID === mSocket.id()){
                    userScore = currentPlayerScore
                } else {
                    opponentScore = currentPlayerScore
                }
            }
//            userScore = currentRoom.getJSONObject("players").getJSONObject(mSocket.id()).getInt("score")
//            opponentScore = currentRoom.getJSONObject("players").getJSONObject(mSocket.id()).getInt("score")
            runOnUiThread {
                // ...
                currentItem = ItemToFind(
                    nextItem.getString("name"),
                    nextItem.getLong("latitude"),
                    nextItem.getLong("longtitude")
                )
                cameraBtn.isEnabled = true // Enable the camera button
                // ...
            }


            // Stuff that updates the UI
            runOnUiThread {
                currentObjectTV.text = "Current Object: ${currentItem?.name}"
            //(params[1] as JSONObject).getJSONObject("players").getJSONObject("SMOu5ICTdmJcCrZlAAAJ").getInt("score")
                opponentScoreTV.text = "Opponent: $opponentScore"
                    userScoreTV.text = "You: $userScore"
            }
        }

        mSocket.on("gameEnded"){params ->
            val paramsObj = params[0] as JSONObject
            val resultIntent = Intent(this, ResultsActivity::class.java)
            resultIntent.putExtra("winner", paramsObj.getString("winner"))
            resultIntent.putExtra("room", paramsObj.getString("room"))
            startActivity(resultIntent)
        }

        cameraBtn.isEnabled = false // Disable the camera button initially

        cameraBtn.setOnClickListener {
            onCameraButtonClick()
        }
    }
    private fun onCameraButtonClick() {
        if (currentItem != null) {
            val intentCamera = Intent(this, CameraActivity::class.java)
            intentCamera.putExtra("currentItem", currentItem)
            startActivity(intentCamera)
        } else {
            Toast.makeText(this, "Current item has not been initialized", Toast.LENGTH_SHORT).show()
        }
    }

    // adds marker representations of the location given on the GoogleMap object
    private fun addMarker(googleMap: GoogleMap, location: LatLng) {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(location)
        )
    }
}