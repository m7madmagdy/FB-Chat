package com.example.socialmedia.ui.main

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.socialmedia.R
import nl.joery.animatedbottombar.AnimatedBottomBar

open class BaseFragment : Fragment() {
    lateinit var bottomNavView: AnimatedBottomBar
    lateinit var appCompactActivity: AppCompatActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appCompactActivity = activity as AppCompatActivity
        bottomNavView = activity?.findViewById(R.id.bottom_bar) as AnimatedBottomBar
        clearGlideMemory()
        backBtnIndicator()
    }

    private fun clearGlideMemory() {
        Glide.get(requireContext()).clearMemory()
    }

    private fun backBtnIndicator() {
        appCompactActivity.supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.navigate_up_back_left)
        }
    }

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

    @Suppress("DEPRECATION")
    fun hideStatusBar() {
        appCompactActivity.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    @Suppress("DEPRECATION")
    fun showStatusBar() {
        appCompactActivity.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun showKeyboard(view: View) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }
}