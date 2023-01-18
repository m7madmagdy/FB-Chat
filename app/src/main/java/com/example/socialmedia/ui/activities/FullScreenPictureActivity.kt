package com.example.socialmedia.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.socialmedia.R
import com.example.socialmedia.databinding.ActivityFullscreenBinding
import com.example.socialmedia.utils.Constants.REQUEST_CODE

class FullScreenPictureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreenActivity()
        initUserClicks()
        loadImage()
    }

    private fun initUserClicks() {
        binding.backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.saveImageBtn.setOnClickListener { saveImage() }
    }

    private fun loadImage() {
        val avatar = intent.getStringExtra("avatar")
        Glide.with(this)
            .load(avatar)
            .error(android.R.drawable.stat_notify_error)
            .placeholder(android.R.drawable.stat_sys_download)
            .into(binding.fullscreenImage)
    }

    private fun saveImage() {
        if (!checkStoragePermission()) {
            requestStoragePermissions()
        } else {
            val bitmapDrawable = binding.fullscreenImage.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            val bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Firebase Social Media App", null)
            Uri.parse(bitmapPath)
            Toast.makeText(this, getString(R.string.photo_saved_on_device), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == (PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE
        )
    }

    private fun fullScreenActivity() {
        supportActionBar?.hide()
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }
}