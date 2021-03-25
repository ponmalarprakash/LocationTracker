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
        checkSession()
    }

    private fun checkSession() {
        if (SessionSave.getUserId(CommonData.USER_ID, this).isEmpty()) {
            showEditUserId()
        } else {
            showUserId()
        }
    }

    private fun checkLogInSwitch() {
        if (SessionSave.getUserId(CommonData.USER_ID, this).isEmpty()) {
            logInOutSwitch.isChecked = false
            logInOutSwitch.text = getString(R.string.logOut)
        } else {
            logInOutSwitch.isChecked = true
            logInOutSwitch.text = getString(R.string.logIn)
        }
    }

    private fun checkShiftSwitch() {
        if (SessionSave.getShiftStatus(CommonData.SHIFT_STATUS, this)
                .isEmpty() || SessionSave.getShiftStatus(
                CommonData.SHIFT_STATUS,
                this
            ) == "out"
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
            getString(R.string.currentUserId) + SessionSave.getUserId(CommonData.USER_ID, this)
        etUserId.text.clear()
        etTripId.text.clear()
        etTravelStatus.text.clear()
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
                    validateTripIdTravelStatus()
                    startForegroundServices()
                }
            } else {
                validateTripIdTravelStatus()
                startForegroundServices()
            }
        }

        btnStopService.setOnClickListener {
            stopForegroundServices()
        }

        checkShiftSwitch()
        shiftSwitch.setOnClickListener {
            if (SessionSave.getShiftStatus(CommonData.SHIFT_STATUS, this)
                    .isEmpty() || SessionSave.getShiftStatus(
                    CommonData.SHIFT_STATUS,
                    this
                ) == "out"
            ) {
                if (SessionSave.getUserId(CommonData.USER_ID, this).isEmpty()) {
                    showToast(getString(R.string.login))
                    shiftSwitch.isChecked = false
                    shiftSwitch.text = getString(R.string.shiftOut)
                } else {
                    SessionSave.saveShiftStatus(
                        CommonData.SHIFT_STATUS,
                        "in",
                        this
                    )
                    shiftSwitch.isChecked = true
                    shiftSwitch.text = getString(R.string.shiftIn)
                    callInsertEventApi("SI", 1)
                }
            } else {
                if (SessionSave.getUserId(CommonData.USER_ID, this).isEmpty()) {
                    showToast(getString(R.string.login))
                    shiftSwitch.isChecked = true
                    shiftSwitch.text = getString(R.string.shiftIn)
                } else {
                    SessionSave.saveShiftStatus(
                        CommonData.SHIFT_STATUS,
                        "out",
                        this
                    )
                    shiftSwitch.isChecked = false
                    shiftSwitch.text = getString(R.string.shiftOut)
                    callInsertEventApi("SO", 0)
                }
            }
        }

        checkLogInSwitch()
        logInOutSwitch.setOnClickListener {
            if (SessionSave.getUserId(CommonData.USER_ID, this).isEmpty()) {  //logout - login
                val userId = etUserId.text.toString()
                if (userId.isEmpty()) {
                    showToast(getString(R.string.enterId))
                    logInOutSwitch.isChecked = false
                    logInOutSwitch.text = getString(R.string.logOut)
                } else {
                    SessionSave.saveUserId(CommonData.USER_ID, userId, this)
                    validateTripIdTravelStatus()
                    showUserId()
                    logInOutSwitch.isChecked = true
                    logInOutSwitch.text = getString(R.string.logIn)
                    callInsertEventApi("LI", 1)
                }
            } else {  //login - logout
                showEditUserId()
                logInOutSwitch.isChecked = false
                logInOutSwitch.text = getString(R.string.logOut)
                callInsertEventApi("LO", 0)
            }
        }
    }

    private fun validateTripIdTravelStatus() {
        val tripId = etTripId.text.toString()
        val travelStatus = etTravelStatus.text.toString()
        SessionSave.saveTripId(CommonData.TRIP_ID, tripId, this)
        SessionSave.saveTravelStatus(CommonData.TRAVEL_STATUS, travelStatus, this)
    }

    private fun callInsertEventApi(eventType: String, status: Int) {
        showProgressDialog(context = this)
        ServiceGenerator.apiService.getEventStatus(
            EventRequest(
                SessionSave.getUserId(CommonData.USER_ID, this).toInt(), eventType
            )
        ).enqueue(object : Callback<EventResponse> {
            override fun onResponse(
                call: Call<EventResponse>,
                response: Response<EventResponse>
            ) {
                dismissProgressDialog()
                if (response.isSuccessful && response.body() != null) {
                    if (status == 0) {
                        stopServices(response.body() as EventResponse, eventType)
                    } else {
                        handleEventResponse(response.body() as EventResponse)
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

    private fun stopServices(eventResponse: EventResponse, eventType: String) {
        if (eventResponse.status == 1) {
            if (eventType == "LI") {
                SessionSave.saveUserId(CommonData.USER_ID, "", this@MainActivity)
                SessionSave.saveTripId(CommonData.TRIP_ID, "", this@MainActivity)
                SessionSave.saveTravelStatus(
                    CommonData.TRAVEL_STATUS,
                    "",
                    this@MainActivity
                )
                stopForegroundServices()
            } else {
                stopForegroundServices()
            }
        }
    }

    private fun handleEventResponse(eventResponse: EventResponse) {
        if (eventResponse.status == 1) {
            Log.d("EventStatus", eventResponse.message)
        }
    }

    /*private fun callShiftStatusApi(value: String) {
        showProgressDialog(context = this)
        ServiceGenerator.apiService.getShiftStatus(
            ShiftStatusRequest(
                SessionSave.getTripId(CommonData.TRIP_ID, this).toString(),
                SessionSave.getTravelStatus(CommonData.TRAVEL_STATUS, this).toString()
            )
        ).enqueue(object : Callback<ShiftStatusResponse> {
            override fun onResponse(
                call: Call<ShiftStatusResponse>,
                response: Response<ShiftStatusResponse>
            ) {
                dismissProgressDialog()
                if (response.isSuccessful && response.body() != null) {
                    handleShiftResponse(response.body() as ShiftStatusResponse)
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

            override fun onFailure(call: Call<ShiftStatusResponse>, t: Throwable) {
                dismissProgressDialog()
                t.printStackTrace()
                val errorMessage = t.message ?: getString(R.string.error)
                showToast(errorMessage)
            }
        })
    }


    private fun callLogInApi() {
        Log.e(
            "TripId and TravelStatus",
            SessionSave.getTripId(CommonData.TRIP_ID, this).toString() + "," +
                    SessionSave.getTravelStatus(CommonData.TRAVEL_STATUS, this).toString()
        )
        showProgressDialog(context = this)
        ServiceGenerator.apiService.getLogInStatus(
            LogInStatusRequest(
                SessionSave.getTripId(CommonData.TRIP_ID, this).toString(),
                SessionSave.getTravelStatus(CommonData.TRAVEL_STATUS, this).toString()
            )
        ).enqueue(object : Callback<LogInStatusResponse> {
            override fun onResponse(
                call: Call<LogInStatusResponse>,
                response: Response<LogInStatusResponse>
            ) {
                dismissProgressDialog()
                if (response.isSuccessful && response.body() != null) {
                    handleLogInResponse(response.body() as LogInStatusResponse)
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

            override fun onFailure(call: Call<LogInStatusResponse>, t: Throwable) {
                dismissProgressDialog()
                t.printStackTrace()
                val errorMessage = t.message ?: getString(R.string.error)
                showToast(errorMessage)
            }
        })
    }*/


    private fun startForegroundServices() {
        if (checkLocationPermission()) {
            val myServiceIntent = Intent(this, LocationService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                startForegroundService(myServiceIntent)
            else startService(myServiceIntent)
            checkLogInSwitch()
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