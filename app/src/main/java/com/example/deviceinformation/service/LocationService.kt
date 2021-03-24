package com.example.deviceinformation.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.deviceinformation.api.ServiceGenerator
import com.example.deviceinformation.data.DetailInfo
import com.example.deviceinformation.data.DeviceInfoResponse
import com.example.deviceinformation.utils.DeviceUtils
import com.example.deviceinformation.view.MainActivity
import com.google.android.gms.location.*
import com.google.gson.Gson
import retrofit2.Call
import java.util.*
import retrofit2.Callback
import retrofit2.Response


const val CHANNEL_ID = "Foreground_Service"

class LocationService : Service() {
    private val timer = Timer()
    private var location: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        getCurrentLocation()
        callApi()
    }

    private fun callApi() {
        timer.schedule(object : TimerTask() {
            override fun run() {
//                val json = Gson().toJson(
//                    DeviceUtils.getAllInfo(
//                        this@LocationService,
//                        location
//                    )
//                )
                ServiceGenerator.apiService.getDeviceInfo(
                    DeviceUtils.getAllInfo(
                        this@LocationService,
                        location
                    )
                ).enqueue(object : Callback<DeviceInfoResponse> {
                    override fun onResponse(
                        call: Call<DeviceInfoResponse>,
                        response: Response<DeviceInfoResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            handleResponse(response.body() as DeviceInfoResponse)
                        } else {
                            Log.d("Response Failure", "Something went wrong!")
                        }
                    }

                    override fun onFailure(call: Call<DeviceInfoResponse>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
            }
        }, 0, 5000)
    }

    private fun handleResponse(response: DeviceInfoResponse) {
        Log.d("msg", response.company_id.toString())
    }

    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }


    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation = locationResult?.lastLocation
            if (mLastLocation != null) {
                location = mLastLocation
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Notification")
            .setContentText("Location Tracker")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(474, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
        timer.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
