package com.example.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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
    lateinit var currentItem: ItemToFind
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // set Corvallis as a test location
        val testLocation = LatLng(44.571651, -123.277702)

        // get reference to the map object
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment

        mapFragment?.getMapAsync { googleMap ->
            addMarker(googleMap, testLocation)
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(testLocation))
//            // set camera starting position bounds to set location
////            googleMap.setOnMapLoadedCallback {
////                val bounds = LatLngBounds.builder()
////                bounds.include(testLocation)
////                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
////            }
        }

        // update the score text when someone finds an object successfully
        val userScoreTV: TextView = findViewById(R.id.user_score_tv)
        val opponentScoreTV: TextView = findViewById(R.id.opponent_score_tv)

        // update the object text when the next object is up
        val currentObjectTV: TextView = findViewById(R.id.current_object_tv)

        // set a click listener for the camera button
        val cameraBtn: Button = findViewById(R.id.buttonCameraMainScreen)
        val intentCamera = Intent(this, CameraActivity::class.java)
        cameraBtn.setOnClickListener {
            startActivity(intentCamera)
        }

        val mSocket = SocketHandler.getSocket()
        mSocket.on("itemGenerated"){params ->
            println("item generated received: $params")
            val itemParsed = params[0] as JSONObject
            currentItem = ItemToFind(
                itemParsed.getString("name"),
                itemParsed.getLong("latitude"),
                itemParsed.getLong("longtitude")
            )
            // Stuff that updates the UI
            runOnUiThread {
                currentObjectTV.text = "Current Object: ${currentItem.name}"
            }
        }

        mSocket.on("newItem"){params ->
            val nextItem = params[0] as JSONObject
            val currentRoom = params[0] as JSONObject
            currentItem = ItemToFind(
                nextItem.getString("name"),
                nextItem.getLong("latitude"),
                nextItem.getLong("longtitude")
            )
            // Stuff that updates the UI
            runOnUiThread {
                currentObjectTV.text = "Current Object: ${currentItem.name}"
            }
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