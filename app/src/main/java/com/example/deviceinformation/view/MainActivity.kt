package com.example.deviceinformation.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import com.example.deviceinformation.R
import com.example.deviceinformation.`interface`.DialogOnClickInterface
import com.example.deviceinformation.api.ServiceGenerator
import com.example.deviceinformation.common.CommonData
import com.example.deviceinformation.common.ViewProgressDialog.cancelDialog
import com.example.deviceinformation.common.ViewProgressDialog.showProgressDialog
import com.example.deviceinformation.data.EventRequest
import com.example.deviceinformation.data.EventResponse
import com.example.deviceinformation.service.LocationService
import com.example.deviceinformation.utils.CommonAlertDialog.alertDialog
import com.example.deviceinformation.utils.SessionSave
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val REQUEST_LOCATION_PERMISSION = 123

class MainActivity : AppCompatActivity(), DialogOnClickInterface {
    private lateinit var btnStartService: Button
    private lateinit var btnStopService: Button
    private lateinit var etUserId: EditText
    private lateinit var tvUserId: TextView
    private lateinit var shiftSwitch: SwitchCompat
    private lateinit var logInOutSwitch: SwitchCompat
    private lateinit var etTripId: EditText
    private lateinit var etTravelStatus: EditText
    private lateinit var etDeviceType: EditText
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnStartService = findViewById(R.id.start)
        btnStopService = findViewById(R.id.stop)
        etUserId = findViewById(R.id.et_user_id)
        tvUserId = findViewById(R.id.tv_user_id)
        shiftSwitch = findViewById(R.id.shiftSwitch)
        logInOutSwitch = findViewById(R.id.LogInOutSwitch)
        etTripId = findViewById(R.id.et_trip_id)
        etTravelStatus = findViewById(R.id.et_travel_status)
        etDeviceType = findViewById(R.id.et_device_type)
        checkSession()
    }

    private fun checkSession() {
        if (SessionSave.getUserId(this).isEmpty()) {
            showEditUserId()
        } else {
            showUserId()
        }
    }

    private fun checkLogInSwitch() {
        if (SessionSave.getUserId(this).isEmpty()) {
            logInOutSwitch.isChecked = false
            logInOutSwitch.text = getString(R.string.logOut)
        } else {
            logInOutSwitch.isChecked = true
            logInOutSwitch.text = getString(R.string.logIn)
        }
    }

    private fun checkShiftSwitch() {
        if (SessionSave.getShiftStatus(this)
                .isEmpty() || SessionSave.getShiftStatus(
                this
            ) == "OUT"
        ) {
            shiftSwitch.isChecked = false
            shiftSwitch.text = getString(R.string.shiftOut)
        } else {
            shiftSwitch.isChecked = true
            shiftSwitch.text = getString(R.string.shiftIn)
        }
    }

    private fun showEditUserId() {
        etUserId.visibility = View.VISIBLE
        tvUserId.visibility = View.GONE
        btnStartService.text = getString(R.string.submitTracker)
    }

    @SuppressLint("SetTextI18n")
    private fun showUserId() {
        etUserId.visibility = View.GONE
        tvUserId.visibility = View.VISIBLE
        btnStartService.text = getString(R.string.startTracker)
        tvUserId.text =
            getString(R.string.currentUserId) + SessionSave.getUserId(this)
        etUserId.text.clear()
        /*etTripId.text.clear()
        etTravelStatus.text.clear()*/
    }

    override fun onResume() {
        super.onResume()
        checkShiftSwitch()
        checkLogInSwitch()
        btnStartService.setOnClickListener {
            if (SessionSave.getUserId(this).isNullOrEmpty()) {
                val userId = etUserId.text.toString()
                if (userId.isEmpty()) {
                    showToast(getString(R.string.enterId))
                } else {
                    SessionSave.saveUserId(userId, this)
                    validateTripIdTravelStatus()
                    checkLogInSwitch()
                    callInsertEventApi("LI", 2)

                    if (shiftSwitch.isChecked && logInOutSwitch.isChecked
                    ) {
                        startForegroundServices()
                    } else {
                        showToast(getString(R.string.changeShift))
                        stopForegroundServices()
                    }
                }
            } else {
                validateTripIdTravelStatus()
                if (shiftSwitch.isChecked && logInOutSwitch.isChecked
                ) {
                    startForegroundServices()
                } else {
                    showToast(getString(R.string.changeShift))
                    stopForegroundServices()
                }
            }
        }

        btnStopService.setOnClickListener {
            stopForegroundServices()
        }

        shiftSwitch.setOnClickListener {
            if (SessionSave.getShiftStatus(this)
                    .isEmpty() || SessionSave.getShiftStatus(
                    this
                ) == "OUT"
            ) {
                if (SessionSave.getUserId(this).isEmpty()) {
                    showToast(getString(R.string.login))
                    shiftSwitch.isChecked = false
                    shiftSwitch.text = getString(R.string.shiftOut)
                    stopForegroundServices()
                } else {
                    shiftSwitch.isChecked = true
                    shiftSwitch.text = getString(R.string.shiftIn)
                    callInsertEventApi("SI", 1)
                }
            } else {
                if (SessionSave.getUserId(this).isEmpty()) {
                    showToast(getString(R.string.login))
                    shiftSwitch.isChecked = true
                    shiftSwitch.text = getString(R.string.shiftIn)
                } else {
                    shiftSwitch.isChecked = false
                    shiftSwitch.text = getString(R.string.shiftOut)
                    callInsertEventApi("SO", 1)
                }
            }
        }

        logInOutSwitch.setOnClickListener {
            if (SessionSave.getUserId(this).isEmpty()) {  //logout - login
                val userId = etUserId.text.toString()
                if (userId.isEmpty()) {
                    showToast(getString(R.string.enterId))
                    logInOutSwitch.isChecked = false
                    logInOutSwitch.text = getString(R.string.logOut)
                } else {
                    SessionSave.saveUserId(userId, this)
                    validateTripIdTravelStatus()
                    showUserId()
                    logInOutSwitch.isChecked = true
                    logInOutSwitch.text = getString(R.string.logIn)
                    callInsertEventApi("LI", 2)
                }
            } else {  //login - logout
                logInOutSwitch.isChecked = false
                logInOutSwitch.text = getString(R.string.logOut)
                callInsertEventApi("LO", 2)
            }
        }
    }

    private fun validateTripIdTravelStatus() {
        val tripId = etTripId.text.toString()
        val travelStatus = etTravelStatus.text.toString()
        val deviceType = etDeviceType.text.toString()
        SessionSave.saveTripId(tripId, this)
        SessionSave.saveTravelStatus(travelStatus, this)
        SessionSave.saveDeviceType(deviceType, this)
    }

    private fun callInsertEventApi(eventType: String, status: Int) {
        showProgressDialog(context = this)
        ServiceGenerator.apiService.getEventStatus(
            EventRequest(
                SessionSave.getUserId(this).toInt(), eventType
            )
        ).enqueue(object : Callback<EventResponse> {
            override fun onResponse(
                call: Call<EventResponse>,
                response: Response<EventResponse>
            ) {
                dismissProgressDialog()
                if (response.isSuccessful && response.body() != null) {
                    if (status == 1) {
                        handleEventResponseForShiftStatus(
                            response.body() as EventResponse,
                            eventType
                        )
                    } else {
                        handleEventResponseForLogInStatus(
                            response.body() as EventResponse,
                            eventType
                        )
                    }
                } else {
                    dismissAlertDialog()
                    dialog = alertDialog(
                        this@MainActivity,
                        this@MainActivity,
                        getString(R.string.something_went_wrong),
                        isCancelable = false
                    )
                }
            }

            override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                dismissProgressDialog()
                t.printStackTrace()
                val errorMessage = t.message ?: getString(R.string.error)
                showToast(errorMessage)
            }
        })
    }

    private fun handleEventResponseForShiftStatus(eventResponse: EventResponse, eventType: String) {
        showToast(eventResponse.message)
        if (eventResponse.status == 1) {
            if (eventType == "SO") {
                SessionSave.saveShiftStatus(
                    "OUT",
                    this@MainActivity
                )
                stopForegroundServices()
            } else {
                SessionSave.saveShiftStatus(
                    "IN",
                    this@MainActivity
                )
            }
        }
    }

    private fun handleEventResponseForLogInStatus(eventResponse: EventResponse, eventType: String) {
        showToast(eventResponse.message)
        if (eventResponse.status == 1) {
            if (eventType == "LO") {
                SessionSave.saveUserId("", this@MainActivity)
                SessionSave.saveTripId("", this@MainActivity)
                SessionSave.saveTravelStatus(
                    "",
                    this@MainActivity
                )
                SessionSave.saveDeviceType("", this@MainActivity)

                stopForegroundServices()
                showEditUserId()
            }
        }
    }

    private fun startForegroundServices() {
        if (checkLocationPermission()) {
            val myServiceIntent = Intent(this, LocationService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                startForegroundService(myServiceIntent)
            else startService(myServiceIntent)
        }
        showUserId()
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

    private fun dismissAlertDialog() {
        dialog?.let {
            if (it.isShowing)
                it.dismiss()
        }
    }

    private fun dismissProgressDialog() {
        cancelDialog()
    }

    override fun onPositiveButtonCLick(dialog: DialogInterface, alertType: Int) {
        dialog.dismiss()
    }

    override fun onNegativeButtonCLick(dialog: DialogInterface, alertType: Int) {
        dialog.dismiss()
    }
}