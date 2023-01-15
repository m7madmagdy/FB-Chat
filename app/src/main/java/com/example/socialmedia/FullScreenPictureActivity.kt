package com.example.socialmedia

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.socialmedia.databinding.ActivityFullscreenBinding

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
        binding.saveImageBtn.setOnClickListener { Toast.makeText(this, "Photo saved on this device", Toast.LENGTH_SHORT).show() }
    }

    private fun loadImage() {
        val avatar = intent.getStringExtra("avatar")
        Glide.with(this)
            .load(avatar)
            .error(android.R.drawable.stat_notify_error)
            .placeholder(android.R.drawable.stat_sys_download)
            .into(binding.fullscreenImage)
    }

    private fun fullScreenActivity() {
        supportActionBar?.hide()
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }
}