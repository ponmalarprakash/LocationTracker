package com.example.deviceinformation.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceGenerator {
    private const val BASE_URL = "http://54.158.70.208:8809/api/v1/"
    private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private var gson = GsonBuilder()
        .setLenient()
        .create()
    private var okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logger)

    private val builder =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson))
            .client(
                okHttp.build()
            )
    private val retrofit = builder.build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}