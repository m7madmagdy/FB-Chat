package com.example.socialmedia.ui.activities

import android.app.Activity
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.socialmedia.R
import com.example.socialmedia.databinding.ActivityLoginBinding
import com.example.socialmedia.utils.ProgressDialog
import com.example.socialmedia.ui.fragments.RecoverPasswordFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private var user: FirebaseUser? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initGoogleSignIn()
        initActionBar()
        initUserLogin()
        initUserRegister()
        initForgetPassword()
        initIconsColor()
    }

    private fun initIconsColor() {
        binding.apply {
            emailEdt.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    emailLayout.setStartIconTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                applicationContext, R.color.blue_firebase_btn
                            )
                        )
                    )
                } else {
                    emailLayout.setStartIconTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                applicationContext, R.color.start_icon_tint
                            )
                        )
                    )
                }
            }

            passwordEdt.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    passwordLayout.setStartIconTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                applicationContext, R.color.blue_firebase_btn
                            )
                        )
                    )
                } else {
                    passwordLayout.setStartIconTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                applicationContext, R.color.start_icon_tint
                            )
                        )
                    )
                }
            }
        }
    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar?.apply {
            title = getString(R.string.login)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHomeAsUpIndicator(R.drawable.navigate_up_back_left)
        }
    }

    private fun initForgetPassword() {
        binding.forgetPassword.setOnClickListener {
            val bottomSheetFragment = RecoverPasswordFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun initUserRegister() {
        val notHaveAccount = binding.notHavAccountTv
        val spannableString = SpannableString(notHaveAccount.text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                finish()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.blue_firebase_btn)
                ds.isUnderlineText = true
            }
        }
        val recoverWord = "Register"
        val start = notHaveAccount.text.indexOf(recoverWord)
        val end = start + recoverWord.length
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        notHaveAccount.text = spannableString
        notHaveAccount.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initUserLogin() {
        binding.loginBtn.setOnClickListener {
            val email = binding.emailEdt.text.toString().trim()
            val password = binding.passwordEdt.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.apply {
                    emailLayout.error = getString(R.string.invalid_email)
                    emailEdt.isFocusable = true
                }
            } else if (password.length < 6) {
                binding.apply {
                    passwordLayout.error = getString(R.string.password_length_must_6_characters)
                    passwordEdt.isFocusable = true
                }
            } else {
                login(email, password)
            }
        }

        binding.googleSignInBtn.setOnClickListener {
            val signInIntent = googleSignInClient?.signInIntent
            signInResult.launch(signInIntent)
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

                    if (task.result.additionalUserInfo?.isNewUser!!) {
                        val userEmail = user!!.email.toString()
                        val uid = user!!.uid
                        val userInfo = hashMapOf<Any, String>()
                        userInfo.apply {
                            put("email", userEmail)
                            put("uid", uid)
                            put("name", "")
                            put("phone", "")
                            put("avatar", "")
                            put("cover","")
                        }
                        val database = FirebaseDatabase.getInstance()
                        val reference = database.getReference("Users")
                        reference.child(uid).setValue(userInfo)
                    }
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            }

            .addOnFailureListener {
                progressDialog.hideDialog()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private val signInResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.result
                    firebaseAuthWithGoogle(account)
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = firebaseAuth.currentUser
                    Toast.makeText(this, "Welcome back \n${user?.email}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
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