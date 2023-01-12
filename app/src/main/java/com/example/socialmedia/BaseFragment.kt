package com.example.socialmedia

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

abstract class BaseFragment : Fragment() {
    //    lateinit var firebaseAuth: FirebaseAuth
//    lateinit var firebaseUser: FirebaseUser
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var appCompactActivity: AppCompatActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appCompactActivity = activity as AppCompatActivity
        bottomNavView = activity?.findViewById(R.id.bottom_navigation) as BottomNavigationView
//        firebaseAuth = FirebaseAuth.getInstance()
//        firebaseUser = firebaseAuth.currentUser!!
        backIndicator()
    }

    abstract fun backIndicator()

    fun showBottomNavigation() {
        bottomNavView.isVisible = true
    }

    fun hideBottomNavigation() {
        bottomNavView.isVisible = false
    }

    fun showToolbar() {
        appCompactActivity.supportActionBar?.apply { show() }
    }

    fun hideToolbar() {
        appCompactActivity.supportActionBar?.apply { hide() }
    }

    fun hideStatusBar() {
        appCompactActivity.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun showStatusBar() {
        appCompactActivity.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}