package com.example.socialmedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.socialmedia.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ProfileActivity : AppCompatActivity() {
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        initActionBar()
        userSignOut()
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar?.apply {
            title = getString(R.string.profile)
        }
    }

    private fun checkUserStatus() {
        val user: FirebaseUser? = firebaseAuth.currentUser

        if (user != null) {
            binding.user.text = user.email
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun userSignOut() {
        binding.signOut.setOnClickListener {
            alertUserSignOut()
        }
    }

    private fun alertUserSignOut() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.sign_out))

        builder.setPositiveButton(getString(R.string.sign_out)) { _, _ ->
            firebaseAuth.signOut()
            checkUserStatus()
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }

    override fun onStart() {
        checkUserStatus()
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}