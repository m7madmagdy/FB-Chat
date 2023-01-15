package com.example.socialmedia.utils

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.example.socialmedia.R

class ProgressDialog(context: Context) : Dialog(context) {

    init {
        setContentView(R.layout.dialog_progress)
    }

    fun show(message: String) {
        val loadingMessage = findViewById<TextView>(R.id.loading_tv)
        loadingMessage.text = message
        super.show()
    }

    fun hideDialog() {
        super.dismiss()
    }
}