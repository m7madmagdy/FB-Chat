package com.example.socialmedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.socialmedia.databinding.ActivityLoginBinding
import com.example.socialmedia.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private var user: FirebaseUser? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        initActionBar()
        initUserLogin()
        initUserRegister()
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar?.apply {
            title = "Login"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun initUserRegister() {
        binding.notHavAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun initUserLogin() {
        binding.loginBtn.setOnClickListener {
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
                login(email, password)
            }
        }
    }

    private fun login(email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.show(getString(R.string.logging_user))

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.hideDialog()
                    user = firebaseAuth.currentUser
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                } else {
                    progressDialog.hideDialog()
                    Toast.makeText(this, "Authentication failed..", Toast.LENGTH_SHORT).show()
                }
            }

            .addOnFailureListener {
                progressDialog.hideDialog()
                Toast.makeText(this, "Connection failed..", Toast.LENGTH_SHORT).show()
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