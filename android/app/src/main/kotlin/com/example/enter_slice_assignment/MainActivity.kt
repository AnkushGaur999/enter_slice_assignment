package com.example.enter_slice_assignment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.example.enter_slice_assignment.database.LocationDatabaseHelper
import com.example.enter_slice_assignment.database.helper.LocationHelper
import com.example.enter_slice_assignment.services.LiveLocationService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import java.io.IOException
import java.util.Locale


class MainActivity : FlutterActivity(), LocationHelper.LastLocation {


    private var dataStream: EventChannel.EventSink? = null
    private lateinit var locationHelper:LocationHelper

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, "live_location/events")
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    dataStream = events!!
                }

                override fun onCancel(arguments: Any?) {

                }
            })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationHelper = LocationHelper(this.applicationContext)
        locationHelper.build()
        locationHelper.setListener(this)

        if (!checkPermissions()) {
            requestPermissions()
        } else {

            if (isLocationEnabled()) {
                startService(Intent(this, LiveLocationService::class.java))
                locationHelper.startLocationUpdates()
            }else{
                android.widget.Toast.makeText(this, "Please turn on gps.", Toast.LENGTH_SHORT).show()
                locationHelper.enableLocation(this)
            }
        }

    }


   private fun getAddressFromLatLng(context: Context?, lat: Double, long: Double): String {

        var add:String = ""
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, long, 1)
            if (addresses != null && addresses.size > 0) {
                val address =
                    addresses[0].getAddressLine(0)
                val city = addresses[0].locality
                val state = addresses[0].adminArea
                val country = addresses[0].countryName
                val postalCode = addresses[0].postalCode
                val knownName = addresses[0].featureName

                add = city+", "+ state+ " "+country+" "+ postalCode
                Log.d("add", "getAddressFromLatLng: $address $add")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return add
    }


    // method to check for permissions
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    }

    // method to request for permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf<String>(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 100
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    startService(Intent(this, LiveLocationService::class.java))
                    locationHelper.startLocationUpdates()
                }
            }
        }
    }

    override fun getLastLocation(location: Location) {
        val address = getAddressFromLatLng(this, location.latitude, location.longitude)
        dataStream?.success(" $address")
    }
}
