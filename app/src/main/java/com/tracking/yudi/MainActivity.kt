package com.tracking.yudi

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var map: GoogleMap? = null
    private var locationPermissionGranted: Boolean = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        Places.initialize(this, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        //The LocationServices interface is the main entry point for Android location services.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        this.map = googleMap
//        val sydney = LatLng(-33.852, 151.211)
//        googleMap.addMarker(
//            MarkerOptions()
//                .position(sydney)
//                .title("Marker in Sydney")
//        )
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        getLocationPermission()
        // Turn on the My Location layer and the related control on the map.
//        updateLocationUI()

    }


    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true

                // Get the current location of the device and set the position of the map.
                getDeviceLocation()

            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }

    }

    private fun getDeviceLocation() {
        /*
      * Get the best and most recent location of the device, which may be null in rare
      * cases when a location is not available.
      */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation

                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        lastKnownLocation?.let {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(it.latitude, it.longitude),
                                    DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }

                    }else{
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }

                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }

    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val DEFAULT_ZOOM = 15
        private val TAG = MainActivity::class.java.simpleName
    }
}