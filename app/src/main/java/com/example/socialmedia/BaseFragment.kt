package com.example.socialmedia

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

abstract class BaseFragment : Fragment() {
//    lateinit var firebaseAuth: FirebaseAuth
//    lateinit var firebaseUser: FirebaseUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        firebaseAuth = FirebaseAuth.getInstance()
//        firebaseUser = firebaseAuth.currentUser!!
        backIndicator()
    }

    abstract fun backIndicator()
}