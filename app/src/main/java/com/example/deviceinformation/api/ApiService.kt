package com.example.deviceinformation.api

import com.example.deviceinformation.data.DeviceInfo
import com.example.deviceinformation.data.DeviceInfoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("updateHistory")
    fun getDeviceInfo(@Body requestData: DeviceInfo): Call<DeviceInfoResponse>
}