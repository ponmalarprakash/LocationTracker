package com.example.deviceinformation.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.deviceinformation.utils.DeviceUtils
//import com.example.deviceinformation.utils.DriverUtils
import com.example.deviceinformation.utils.SessionSave
import okhttp3.ResponseBody

import retrofit2.Call

import retrofit2.Callback
import retrofit2.Response

class SendDriverDeviceInfo {
  /*  fun sendInfo(context: Context, unique: String) {

        val base_url = SessionSave.getSession("base_url", context)
        val uri = Uri.parse(base_url)
        val path = uri.path
//        val client = ServiceGenerator(mContext, false, base_url.replace(path.toRegex(), "")).createService(CoreClient::class.java)
        val url = base_url.replace(path!!.toRegex(), "") + "/taxidispatch/report_push_notification"
        val client = MyApplication.getInstance().apiManagerWithEncryptBaseUrl
        val detail_infoCall = client.detail_infoCall(url,DetailInfo(DeviceUtils.getAllInfo(context), DriverUtils.driverInfo(context), unique), SessionSave.getSession("Lang", context))

        detail_infoCall.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.isSuccessful()) {
                        Log.v("FirebaseService", "" + response.message())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                t.printStackTrace()


            }

        })

    }*/
}