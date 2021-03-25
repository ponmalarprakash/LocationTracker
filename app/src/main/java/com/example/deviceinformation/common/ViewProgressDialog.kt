package com.example.deviceinformation.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import com.example.deviceinformation.R

object ViewProgressDialog {
    private lateinit var progressDialog: Dialog

    fun showProgressDialog(context: Context, isCancelable: Boolean = false): Dialog {
        progressDialog = Dialog(context, R.style.NewDialog)
        progressDialog.setContentView(R.layout.layout_progress)
        progressDialog.window?.apply {
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        progressDialog.setCancelable(isCancelable)
        progressDialog.show()
        return progressDialog
    }

    fun showColoredProgressDialog(context: Context, isCancelable: Boolean = false): Dialog {
        progressDialog = Dialog(context, R.style.NewDialog)
        progressDialog.setContentView(R.layout.layout_full_progress)
        progressDialog.window?.apply {
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        progressDialog.setCancelable(isCancelable)
        progressDialog.show()
        return progressDialog
    }

    fun cancelDialog() {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}