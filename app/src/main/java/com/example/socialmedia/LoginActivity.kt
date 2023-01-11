package com.example.socialmedia

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.socialmedia.databinding.ActivityLoginBinding
import com.example.socialmedia.utils.ProgressDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider

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
            title = "Login"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun initForgetPassword() {
        binding.forgetPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Recover Password")
            val layout = LinearLayout(this)
            val emailEdt = EditText(this)
            emailEdt.apply {
                hint = getString(R.string.email)
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                minEms = 100
            }
            layout.addView(emailEdt)
            builder.setView(layout)

            builder.setPositiveButton("Recover") { _, _ ->
                val email = emailEdt.text.toString().trim()
                beginRecovery(email)
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            builder.create().show()
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

    private fun beginRecovery(email: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.show("Sending Email..")

        firebaseAuth.sendPasswordResetEmail(email)

            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.hideDialog()
                    Toast.makeText(this, "Email sent \nPlease check your mail", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            .addOnFailureListener {
                progressDialog.hideDialog()
                Toast.makeText(this, "Connection failed..", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Failed.", Toast.LENGTH_SHORT).show()
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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Connection failed.", Toast.LENGTH_SHORT).show()
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