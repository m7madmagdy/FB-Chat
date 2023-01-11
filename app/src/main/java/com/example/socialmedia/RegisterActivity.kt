package com.example.socialmedia

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.socialmedia.databinding.ActivityRegisterBinding
import com.example.socialmedia.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private var user: FirebaseUser? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = Firebase.auth
        initActionBar()
        initUserRegister()
        initUserLogin()
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar?.apply {
            title = "Create Account"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun initUserLogin() {
        val havAccountText = binding.havAccountTv
        val spannableString = SpannableString(havAccountText.text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@RegisterActivity, R.color.blue_firebase_btn)
                ds.isUnderlineText = false
            }
        }
        val recoverWord = "Login"
        val start = havAccountText.text.indexOf(recoverWord)
        val end = start + recoverWord.length
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        havAccountText.text = spannableString
        havAccountText.movementMethod = LinkMovementMethod.getInstance()
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
            } else if (password.length < 6 || password.isEmpty()) {
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
        val progressDialog = ProgressDialog(this)
        progressDialog.show(getString(R.string.registering_user))

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ProgressDialog(this).hideDialog()
                    user = firebaseAuth.currentUser!!
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                } else {
                    progressDialog.hideDialog()
                    Toast.makeText(this, "User is already exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                progressDialog.hideDialog()
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