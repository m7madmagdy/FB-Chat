package com.example.socialmedia.utils

import android.app.Dialog
import android.content.Context
import com.example.socialmedia.R

object ProgressDialog {

    private lateinit var progressDialog: Dialog

    fun showProgressDialog(context: Context) {
        progressDialog = Dialog(context)
        progressDialog.setContentView(R.layout.dialog_progress)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun hideProgressDialog() {
        progressDialog.hide()
    }
}