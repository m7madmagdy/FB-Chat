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
            Toast.makeText(this, "Register...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            Toast.makeText(this, "Login...", Toast.LENGTH_SHORT).show()
        }
    }


    /* TODO: Part 1 We will do the followings.

        01 - Add Internet permission to manifest file
        02 - Add Register and login Buttons in MainActivity
        03 - Create RegisterActivity
        04 - Create Firebase Project and connect app with that project
        05 - Check google-services.json file to make sure app is connected with firebase
        06 - User Registration with Email & Password
        07 - Create ProfileActivity
        08 - Make ProfileActivity Launcher
        09 - Go To ProfileActivity After Register/Login
        10 - Add Logout Feature
    */


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}