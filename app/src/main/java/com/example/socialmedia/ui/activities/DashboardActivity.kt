package com.example.socialmedia.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.socialmedia.R
import com.example.socialmedia.databinding.ActivityDashboardBinding
import com.example.socialmedia.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.perf.FirebasePerformance

class DashboardActivity : AppCompatActivity() {
    private var _binding: ActivityDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navController: NavController
    private val trace = FirebasePerformance.getInstance().newTrace("social-media")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        initNavController()
        trace.start()
    }

    @SuppressLint("RestrictedApi")
    private fun initNavController() {
        val toolbar = binding.dashboardToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.show()
        val menu = MenuBuilder(this)
        val inflater = menuInflater
        inflater.inflate(R.menu.bottom_nav_menu, menu)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomBar.setupWithNavController(menu, navController)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {
                alertUserSignOut()
                true
            }
            else -> false
        }
    }

    private fun alertUserSignOut() {
        val user: FirebaseUser? = firebaseAuth.currentUser

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.sign_out))

        builder.setPositiveButton(getString(R.string.sign_out)) { _, _ ->
            if (user == null) {
                Toast.makeText(this, "Failed..", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }

    private fun checkUserStatus() {
        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        checkUserStatus()
        super.onStart()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController =
            Navigation.findNavController(this@DashboardActivity, R.id.fragmentContainerView)
        navController.navigateUp()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        trace.stop()
    }
}
