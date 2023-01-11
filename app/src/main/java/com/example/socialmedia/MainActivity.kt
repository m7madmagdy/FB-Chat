package com.example.socialmedia

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmedia.databinding.ActivityMainBinding

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
    /* TODO: Part 2 We will do the followings.

        01 - Make ProfileActivity Launcher
        02 - On app start Check if user signed in stay in ProfileActivity otherwise go to MainActivity
        03 - Create Login Activity
        04 - Login User with Email/Password
        05 - After Logging in go to Profile Activity
        06 - Add options menu for adding logout Options
        07 - After LoggingOut go to MainActivity
    */


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}