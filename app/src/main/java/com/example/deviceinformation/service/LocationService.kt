package com.example.deviceinformation.service

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.deviceinformation.R
import com.example.deviceinformation.api.ServiceGenerator
import com.example.deviceinformation.common.InternetConnection
import com.example.deviceinformation.data.DeviceInfoResponse
import com.example.deviceinformation.data.LocationData
import com.example.deviceinformation.utils.DeviceUtils
import com.example.deviceinformation.view.MainActivity
import com.google.android.gms.location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


const val CHANNEL_ID = "Foreground_Service"

class LocationService : Service() {
    private val timer = Timer()
    private var location: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationList: ArrayList<LocationData> by lazy { ArrayList() }

    override fun onCreate() {
        super.onCreate()
        getCurrentLocation()
        callApi()
    }

    private fun callApi() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                val loader = Thread {
                    if (InternetConnection.hasInternetConnected(this@LocationService)) {
                        callUpdateHistoryAPI()
                    } else {
                        Log.d("Check Connection", "No network!")
                    }
                }
                loader.start()
            }
        }, 0, 7000)
    }

    private fun callUpdateHistoryAPI() {
        ServiceGenerator.apiService.getDeviceInfo(
            DeviceUtils.getAllInfo(
                this@LocationService,
                location,
                locationList
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

    private fun handleResponse(response: DeviceInfoResponse) {
        locationList.clear()
    }

    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 2000
        locationRequest.fastestInterval = 1000

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
            Looper.getMainLooper()
        )
    }


    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation = locationResult?.lastLocation
            if (mLastLocation != null) {
                locationList.add(
                    LocationData(
                        DeviceUtils.getLatitude(mLastLocation),
                        DeviceUtils.getLongitude(mLastLocation),
                        DeviceUtils.getAltitudeValue(mLastLocation),
                        DeviceUtils.getBearing(mLastLocation),
                        DeviceUtils.getSpeed(mLastLocation),
                        DeviceUtils.getAccuracy(mLastLocation),
                        DeviceUtils.getTime(mLastLocation)
                    )
                )
                location = mLastLocation
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText(getString(R.string.app_running))
            .setContentTitle(getString(R.string.app_name))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher)
            .setWhen(System.currentTimeMillis())
            .build()
        startForeground(474, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
        locationList.clear()
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

    private fun getNotification(): Notification {
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(
                this,
                MainActivity::class.java
            ), 0
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notifyId = 10
        val notification: Notification
        var builder: Notification.Builder? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "My Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            // Configure the notification channel.
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification.Builder(this, CHANNEL_ID)
                .setContentText(getString(R.string.app_running))
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())
        } else {
            builder = Notification.Builder(this)
                .setContentText(getString(R.string.app_running))
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())
        }

        notification = builder.build()
//        notificationManager.notify(notifyId, notification)
        return notification
    }
}
