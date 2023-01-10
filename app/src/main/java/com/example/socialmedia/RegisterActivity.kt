package com.example.socialmedia

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmedia.databinding.ActivityRegisterBinding
import com.example.socialmedia.utils.ProgressDialog.hideProgressDialog
import com.example.socialmedia.utils.ProgressDialog.showProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class RegisterActivity : AppCompatActivity() {
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private var user: FirebaseUser? = null
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = Firebase.auth
        initActionBar()
        initUserRegister()
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar?.apply {
            title = "Create Account"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun initUserRegister() {
        binding.registerBtn.setOnClickListener {
            val email = binding.emailEdt.text.toString().trim()
            val password = binding.passwordEdt.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.apply {
                    emailLayout.error = "Invalid Email"
                    emailEdt.isFocusable = true
                }
            } else if (password.length < 6) {
                binding.apply {
                    passwordLayout.error = "Password length at least 6 characters"
                    passwordEdt.isFocusable = true
                }
            } else {
                register(email, password)
            }
        }
    }

    private fun register(email: String, password: String) {
        showProgressDialog(this)

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    hideProgressDialog()
                    user = mAuth.currentUser!!
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                } else {
                    hideProgressDialog()
                    Toast.makeText(this, "User is already exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                hideProgressDialog()
                Toast.makeText(this, "Connection Failed.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}