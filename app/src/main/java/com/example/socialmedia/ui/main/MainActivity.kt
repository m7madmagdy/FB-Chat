package com.example.socialmedia.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmedia.databinding.ActivityMainBinding
import com.example.socialmedia.ui.activities.LoginActivity
import com.example.socialmedia.ui.activities.RegisterActivity

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUserClick()
    }

    private fun initUserClick() {
        binding.registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    /* TODO: Part 11 We will do the followings.
      -> Show sent messages
         1- Design different layouts for sender and receiver
         2- Receiver layout on left will contain image, message and time
         3- Sender layout on right will contain message and time

    */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}