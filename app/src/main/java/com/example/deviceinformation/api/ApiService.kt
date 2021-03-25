package com.example.deviceinformation.api

import com.example.deviceinformation.data.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("updateHistory")
    fun getDeviceInfo(@Body requestData: DeviceInfo): Call<DeviceInfoResponse>

    @POST("insertEvent")
    fun getEventStatus(@Body eventData: EventRequest): Call<EventResponse>
}