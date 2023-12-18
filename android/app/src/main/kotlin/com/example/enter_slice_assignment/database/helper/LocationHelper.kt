package com.example.enter_slice_assignment.database.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import java.util.Timer


class LocationHelper(private val context: Context) {

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    private var lostLocation:LastLocation? = null

    fun setListener(location: LastLocation){
        this.lostLocation = location
    }

    fun startLocationUpdates() {

        startGpsReading()
    }


    fun build(){
        locationRequest = LocationRequest.create()
        locationRequest?.setInterval(5000L)
        locationRequest?.setFastestInterval(5000L)

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context.applicationContext);
    }

    private val locationListener = object :LocationCallback() {

        override fun onLocationResult(location: LocationResult) {
            super.onLocationResult(location)
            lostLocation?.getLastLocation(location.lastLocation!!)
            Log.d("Last Location", "onLocationResult: $location")
        }

    }

    fun enableLocation(activity: Activity){
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            val state = locationSettingsResponse.locationSettingsStates

            val label =
                "GPS >> (Present: ${state?.isGpsPresent}  | Usable: ${state?.isGpsUsable} ) \n\n" +
                        "Network >> ( Present: ${state?.isNetworkLocationPresent} | Usable: ${state?.isNetworkLocationUsable} ) \n\n" +
                        "Location >> ( Present: ${state?.isLocationPresent} | Usable: ${state?.isLocationUsable} )"


        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {

                    startLocationUpdates()

                } catch (sendEx: IntentSender.SendIntentException) {

                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startGpsReading() {
        Log.i(javaClass.simpleName, "Starting GPS readings")
                mFusedLocationClient!!.requestLocationUpdates(
            locationRequest!!,
            locationListener, Looper.myLooper()
        )
    }

    interface LastLocation{
        fun getLastLocation(location: Location)

    }

}