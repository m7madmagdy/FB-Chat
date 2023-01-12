package com.example.socialmedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
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
        initUserSignOut()
        initNavController()
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar?.apply {
            title = getString(R.string.profile)
        }
    }

    private fun initNavController() {
        val bottomNavView = binding.bottomNavigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    private fun checkUserStatus() {
        val user: FirebaseUser? = firebaseAuth.currentUser

        if (user != null) {
            Toast.makeText(this, user.email, Toast.LENGTH_SHORT).show()
//            binding.user.text = user.email
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun initUserSignOut() {
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

    override fun onSupportNavigateUp(): Boolean {
        val navController =
            Navigation.findNavController(this@ProfileActivity, R.id.fragmentContainerView)
        navController.navigateUp()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}