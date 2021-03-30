package com.example.deviceinformation.data

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val bearing: Double = 0.0,
    val speed: Int = 0,
    val accuracy: Double = 0.0,
)