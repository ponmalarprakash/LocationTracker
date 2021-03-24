package com.example.deviceinformation.utils

import android.net.TrafficStats
import java.util.*

object InternetSpeedChecker {
    lateinit var mDownloadSpeedOutput: String
    lateinit var mUnits: String
    fun getDownloadSpeed(): String {

        val mRxBytesPrevious = TrafficStats.getTotalRxBytes()
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val mRxBytesCurrent = TrafficStats.getTotalRxBytes()

        val mDownloadSpeed = mRxBytesCurrent - mRxBytesPrevious

        val mDownloadSpeedWithDecimals: Float

        if (mDownloadSpeed >= 1000000000) {
            mDownloadSpeedWithDecimals = mDownloadSpeed.toFloat() / 1000000000.toFloat()
            mUnits = " GB"
        } else if (mDownloadSpeed >= 1000000) {
            mDownloadSpeedWithDecimals = mDownloadSpeed.toFloat() / 1000000.toFloat()
            mUnits = " MB"

        } else {
            mDownloadSpeedWithDecimals = mDownloadSpeed.toFloat() / 1000.toFloat()
            mUnits = " KB"
        }

        if (mUnits != " KB" && mDownloadSpeedWithDecimals < 100) {
            mDownloadSpeedOutput = String.format(Locale.US, "%.1f", mDownloadSpeedWithDecimals)
        } else {
            mDownloadSpeedOutput = Integer.toString(mDownloadSpeedWithDecimals.toInt())
        }

        println("mDownloadSpeedOutput$mDownloadSpeedOutput")
        return mDownloadSpeedOutput
    }
}