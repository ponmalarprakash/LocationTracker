package com.example.deviceinformation.common

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object InternetConnection {
    private fun hasNetworkAvailable(context: Context): Boolean {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager?
        val network = manager?.activeNetworkInfo
        return (network != null && network.isConnected)
    }

    fun hasInternetConnected(context: Context): Boolean {
        if (hasNetworkAvailable(context)) {
            try {
                val connection = URL("https://www.google.com").openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "ConnectionTest")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1000
                connection.connect()
                Log.d(
                    "hasInternetConnected",
                    "${(connection.responseCode == 200)}"
                )
                return (connection.responseCode == 200)
            } catch (e: IOException) {
                Log.e("Error Connection", e.toString())
            }
        } else {
            Log.w("No network", "No network available!")
        }
        Log.d("hasInternetConnected", "false")
        return false
    }
}