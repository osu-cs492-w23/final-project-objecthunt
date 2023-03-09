package com.example.objecthuntmaps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set Corvallis as a test location
        val testLocation = LatLng(44.571651, -123.277702)

        // get reference to the map object
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment

        mapFragment?.getMapAsync { googleMap ->
            addMarker(googleMap, testLocation)
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(testLocation))
            // set camera starting position bounds to set location
//            googleMap.setOnMapLoadedCallback {
//                val bounds = LatLngBounds.builder()
//                bounds.include(testLocation)
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
//            }
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