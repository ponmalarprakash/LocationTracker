package com.example.deviceinformation.utils

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.deviceinformation.BuildConfig
import com.example.deviceinformation.common.CommonData
import com.example.deviceinformation.data.DeviceInfo
import com.google.android.gms.common.GoogleApiAvailability
import java.io.File
import java.text.DecimalFormat
import java.util.*


/**
 * Created on 10th October by developer at NDOT Technologies
 * Singleton class to get device information like manufacturer, model, etc...
 *
 * object represents singleton instance for this class
 * <b>https://kotlinlang.org/docs/reference/object-declarations.html</b>
 */
object DeviceUtils {
    fun getAllInfo(
        context: Context,
        location: Location?
    ): DeviceInfo {
        try {
            val deviceInfo = DeviceInfo(
                getDeviceManufacture(),
                getDeviceModel(),
                getSdk(),
                getVersionCode(),
                getCarriername(context),
                getGooglePlayVersion(context),
                getBatteryLevel(context),
                getAvailableMemory(context),
                getAvailableStorage(),
                getTotalMemory(context),
                getAppVersionCode(),
                getAppVersionName(),
                CommonData.getCurrentTime(),
                deviceType(context),
                SessionSave.getSession(CommonData.DEVICE_TOKEN, context),
                getDeviceID(context),
                InternetSpeedChecker.getDownloadSpeed(),
                getLatitude(location),
                getLongitude(location),
                getAltitudeValue(location),
                getBearing(location),
                getSpeed(location),
                getAccuracy(location),
                SessionSave.getUserId(context).toInt(),
                SessionSave.getTripId(context),
                getTravelStatus(context)
            )
            return deviceInfo
        } catch (e: Exception) {
            e.printStackTrace()
            return DeviceInfo(e.localizedMessage)
        }
    }

    private fun deviceType(context: Context): String {
        val deviceType = SessionSave.getDeviceType(context)
        return if (deviceType.isNullOrEmpty()) {
            "1"
        } else {
            deviceType
        }
    }

    private fun getTravelStatus(context: Context): String {
        val travelStatus = SessionSave.getTravelStatus(context)
        return if (travelStatus.isNullOrEmpty()) {
            "F"
        } else {
            travelStatus
        }
    }

    private fun getLatitude(location: Location?): Double {
        return if (location != null) {
            val df = DecimalFormat("#.###")
            df.format(location.latitude).toDouble()
        } else {
            0.0
        }
        /*return *//*DecimalFormat("##.###").format(location.latitude).toDouble()*//*location?.latitude
            ?: 0.0*/
    }

    private fun getLongitude(location: Location?): Double {
        return if (location != null) {
            val df = DecimalFormat("#.###")
            df.format(location.longitude).toDouble()
        } else {
            0.0
        }
    }

    private fun getAltitudeValue(location: Location?): Double {
        return if (location != null) {
            val df = DecimalFormat("#.###")
            df.format(location.altitude).toDouble()
        } else {
            0.0
        }
    }

    private fun getBearing(location: Location?): Double {
        return if (location != null) {
            val df = DecimalFormat("#.###")
            df.format(location.bearing).toDouble()
        } else {
            0.0
        }
    }

    private fun getSpeed(location: Location?): Int {
        return location?.speed?.toInt() ?: 0
        /* val df = DecimalFormat("#.###")
         df.format(location.speed).toDouble()*/
    }

    private fun getAccuracy(location: Location?): Double {
        return if (location != null) {
            val df = DecimalFormat("#.###")
            df.format(location.accuracy).toDouble()
        } else {
            0.0
        }
    }


    private fun getDeviceID(context: Context) =
        Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)


    private fun getGooglePlayVersion(context: Context): Int {
        val v = context.packageManager.getPackageInfo(
            GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
            0
        ).versionCode
        return v
    }


    private fun getDeviceManufacture(): String {
        return Build.MANUFACTURER
    }


    private fun getDeviceModel(): String {
        return Build.MODEL
    }

    private fun getSdk(): Int {
        return Build.VERSION.SDK_INT
    }

    private fun getVersionCode(): String {
        return Build.VERSION.RELEASE
    }


    private fun getAvailableStorage(): String {
        val freeMemory = getFreeStorage(Environment.getDataDirectory())
        val fileinMB = readableFileSize(freeMemory)
        return fileinMB
    }

    private fun getAppVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    private fun getAppVersionName(): String {
        return BuildConfig.VERSION_NAME
    }


    private fun getAvailableMemory(context: Context): String {
        val mi = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        return readableFileSize(mi.availMem)
    }

    private fun getTotalMemory(context: Context): String {
        val mi = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        return readableFileSize(mi.totalMem)
    }


    private fun getBatteryLevel(context: Context): Int {
        val i = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = i!!.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        return level
    }


    private fun getSimOperatorName(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return (telephonyManager.getSimOperatorName())
    }


    private fun getCarriername(context: Context): String {
        var subsInfoList = ArrayList<String>()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subscriptionManager =
                    context.getSystemService(android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val permissionCheck =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    if (isSimSupport(context)) {
                        for (subscriptionInfo in subscriptionManager.activeSubscriptionInfoList!!) {
                            subsInfoList.add(subscriptionInfo.carrierName.toString())
                        }
                    }
                }
            } else {
                val manager: TelephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                //            val carrierName: String = manager.getNetworkOperatorName();
                if (isSimSupport(context)) {
                    subsInfoList.add(manager.networkOperatorName)
                }
            }
        } catch (e: Exception) {
        }
        return subsInfoList.toString()
    }

    private fun isSimSupport(context: Context): Boolean {
        val tm =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager  //gets the current TelephonyManager
        return tm.simState != TelephonyManager.SIM_STATE_ABSENT

    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        var result = ""
        result = DecimalFormat("#,##0.#").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
        return result
    }


    private fun getFreeStorage(path: File): Long {
        val stats = StatFs(path.absolutePath)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stats.availableBlocksLong * stats.blockSizeLong
        } else {
            stats.availableBlocksLong * stats.blockSizeLong
        }
    }
}