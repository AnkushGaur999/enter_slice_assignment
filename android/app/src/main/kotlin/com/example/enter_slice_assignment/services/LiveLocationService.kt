package com.example.enter_slice_assignment.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.enter_slice_assignment.MainActivity
import com.example.enter_slice_assignment.R
import com.example.enter_slice_assignment.database.LocationDatabaseHelper
import com.example.enter_slice_assignment.database.enitities.MyLocation
import com.example.enter_slice_assignment.database.helper.LocationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LastLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import java.io.IOException
import java.util.Locale


class LiveLocationService : Service(), LocationHelper.LastLocation{


    private val CHANNEL_ID = "101"
    private val SERVICE_LOCATION_REQUEST_CODE = 100
    private val LOCATION_SERVICE_NOTIF_ID = 1001
    private lateinit var databaseHelper: LocationDatabaseHelper
    private var locationHelper: LocationHelper? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()
        initData()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initData() {

        databaseHelper = LocationDatabaseHelper(applicationContext)

        locationHelper = LocationHelper(this.applicationContext)
        locationHelper!!.build()
        locationHelper?.setListener(this)

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        prepareForegroundNotification();
        locationHelper?.startLocationUpdates()

        return START_STICKY
    }




    private fun prepareForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            SERVICE_LOCATION_REQUEST_CODE,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentTitle(getString(R.string.app_notification_description))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(LOCATION_SERVICE_NOTIF_ID, notification)
    }

    override fun getLastLocation(location: Location) {

        databaseHelper.insertData(
            MyLocation(
                location.latitude.toString(),
                location.longitude.toString(),
                getAddressFromLatLng(this, location.latitude, location.longitude)
            )
        )
    }

    private fun getAddressFromLatLng(context: Context?, lat: Double, long: Double): String {

        var add:String = ""
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, long, 1)
            if (addresses != null && addresses.size > 0) {

                val locality = addresses[0].locality
                val adminArea = addresses[0].adminArea
                val city = addresses[0].locality
                val state = addresses[0].adminArea
                val country = addresses[0].countryName
                val postalCode = addresses[0].postalCode
                val knownName = addresses[0].featureName

                add = "$locality $adminArea $city, $state $country $postalCode"
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return add
    }

}