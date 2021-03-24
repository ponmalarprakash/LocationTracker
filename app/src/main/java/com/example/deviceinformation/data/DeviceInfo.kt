package com.example.deviceinformation.data

data class DeviceInfo(
    val manufacture: String?,
    val model: String = "",
    val sdk: Int = 0,

    val versioncode: String = "",
    var carrierName: String = "",
    val googlePlayVersion: Int = 0,
    val batteryPercent: Int = 0,
    val availMem: String = "",
    val availStorage: String = "",
    val totalMem: String = "",
    val appVersionCode: Int = 0,
    val appversionName: String = "",
    val current_time: String = "",
    //static android 1
    val deviceType: String = "1",
    val deviceToken: String = "",
    val deviceId: String = "",
    val internetSpeed: String = "",

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val bearing: Int = 0,
    val speed: String = "",
    val accuracy: Double = 0.0,
    val userId: Int= 0
)