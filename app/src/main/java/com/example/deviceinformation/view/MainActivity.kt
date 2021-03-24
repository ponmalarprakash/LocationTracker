package com.example.deviceinformation.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.deviceinformation.R
import com.example.deviceinformation.common.CommonData
import com.example.deviceinformation.service.LocationService
import com.example.deviceinformation.utils.SessionSave


private const val REQUEST_LOCATION_PERMISSION = 123

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartService: Button
    private lateinit var btnStopService: Button
    private lateinit var etUserId: EditText
    private lateinit var tvUserId: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnStartService = findViewById(R.id.start)
        btnStopService = findViewById(R.id.stop)
        etUserId = findViewById(R.id.et_user_id)
        tvUserId = findViewById(R.id.tv_user_id)

        checkSession()
    }

    private fun checkSession() {
        if (SessionSave.getUserId(CommonData.USER_ID, this).isEmpty()) {
            etUserId.visibility = View.VISIBLE
            tvUserId.visibility = View.GONE
            btnStartService.text = getString(R.string.submitTracker)
        } else {
            showUserId()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showUserId() {
        etUserId.visibility = View.GONE
        tvUserId.visibility = View.VISIBLE
        btnStartService.text = getString(R.string.startTracker)
        tvUserId.text =
            getString(R.string.currentUserId) + SessionSave.getUserId(CommonData.USER_ID, this)

    }

    override fun onResume() {
        super.onResume()

        btnStartService.setOnClickListener {
            if (etUserId.visibility == View.VISIBLE) {
                val userId = etUserId.text.toString()
                if (userId.isEmpty()) {
                    showToast(getString(R.string.enterId))
                } else {
                    SessionSave.saveUserId(CommonData.USER_ID, userId, this)
                    startForegroundServices()
                }
            } else {
                startForegroundServices()
            }
        }

        btnStopService.setOnClickListener {
            stopForegroundServices()
        }
    }

    private fun startForegroundServices() {
        if (checkLocationPermission()) {
            val myServiceIntent = Intent(this, LocationService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                startForegroundService(myServiceIntent)
            else startService(myServiceIntent)
            showUserId()
        }
    }

    private fun stopForegroundServices() {
        val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED else ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            false
        } else true
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACCESS_BACKGROUND_LOCATION else Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED /*&& grantResults[1] == PackageManager.PERMISSION_GRANTED*/) {
                startForegroundServices()
            } else {
                showToast(getString(R.string.permissionDenied))
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
    }
}