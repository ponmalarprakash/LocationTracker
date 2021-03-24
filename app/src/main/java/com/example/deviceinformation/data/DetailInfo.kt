package com.example.deviceinformation.data

data class DetailInfo(
    val deviceInfo: DeviceInfo,
    val driverInfo: ModelDriverInfo,
    val unique: String = ""
)