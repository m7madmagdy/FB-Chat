package com.example.socialmedia

import android.content.Intent
import android.content.res.ColorStateList
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = Firebase.auth
        initActionBar()
        initUserRegister()
        initUserLogin()
        initIconsColor()
    }

    private fun initIconsColor() {
        binding.apply {
            nameEdt.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    nameLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        applicationContext, R.color.blue_firebase_btn)))
                }else{
                    nameLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        applicationContext, R.color.start_icon_tint)))
                }
            }

            phoneEdt.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    phoneLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        applicationContext, R.color.blue_firebase_btn)))
                }else{
                    phoneLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        applicationContext, R.color.start_icon_tint)))
                }
            }

            emailEdt.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    emailLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        applicationContext, R.color.blue_firebase_btn)))
                }else{
                    emailLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        applicationContext, R.color.start_icon_tint)))
                }
            }

            passwordEdt.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    passwordLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        applicationContext, R.color.blue_firebase_btn)))
                }else{
                    passwordLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        applicationContext, R.color.start_icon_tint)))
                }
            }
        }
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar?.apply {
            title = getString(R.string.create_account)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHomeAsUpIndicator(R.drawable.navigate_up_back_left)
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
                ds.isUnderlineText = true
            }
        }
        val recoverWord = getString(R.string.login)
        val start = havAccountText.text.indexOf(recoverWord)
        val end = start + recoverWord.length
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        havAccountText.text = spannableString
        havAccountText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initUserRegister() {
        binding.registerBtn.setOnClickListener {
            val name = binding.nameEdt.text.toString().trim()
            val phone = binding.phoneEdt.text.toString().trim()
            val email = binding.emailEdt.text.toString().trim()
            val password = binding.passwordEdt.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.apply {
                    emailLayout.error = getString(R.string.invalid_email)
                    emailEdt.isFocusable = true
                }
            } else if (password.length < 6 || password.isEmpty()) {
                binding.apply {
                    passwordLayout.error = getString(R.string.password_length_must_6_characters)
                    passwordEdt.isFocusable = true
                }
            } else {
                register(name, phone, email, password)
            }
        }
    }

    private fun register(name: String, phone: String, email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.show(getString(R.string.registering_user))

        firebaseAuth.createUserWithEmailAndPassword(email, password)

            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ProgressDialog(this).hideDialog()
                    firebaseUser = firebaseAuth.currentUser!!
                    val userEmail = firebaseUser.email.toString()
                    val userId = firebaseUser.uid
                    val userInfo = hashMapOf<Any, String>()
                    userInfo.apply {
                        put("email", userEmail)
                        put("uid", userId)
                        put("name", name)
                        put("phone", phone)
                        put("avatar", "")
                        put("cover", "")
                    }
                    val database = FirebaseDatabase.getInstance()
                    val reference = database.getReference("Users")
                    reference.child(userId).setValue(userInfo)
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                progressDialog.hideDialog()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
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