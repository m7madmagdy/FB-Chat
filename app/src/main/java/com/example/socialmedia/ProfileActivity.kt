package com.example.socialmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.socialmedia.databinding.ActivityProfileBinding
import com.example.socialmedia.databinding.ActivityRegisterBinding

class ProfileActivity : AppCompatActivity() {
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActionBar()
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar?.title = "Profile"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}