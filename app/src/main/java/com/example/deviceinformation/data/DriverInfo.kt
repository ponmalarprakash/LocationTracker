package com.example.deviceinformation.data

data class DriverInfo(
    val carNo: String = "",
    val driverId: Int = 0,
    val lastLocation: String = "",
    val model: String = "",
    val shiftStatus: String = "",
    val status: String = "",
    val travelStatus: String = "",
    val tripId: String
)