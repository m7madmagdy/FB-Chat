package com.example.socialmedia.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.socialmedia.R
import com.example.socialmedia.databinding.FragmentChatBinding
import com.example.socialmedia.ui.main.BaseFragment
import com.example.socialmedia.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChatFragment : BaseFragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val arguments: ChatFragmentArgs by navArgs()
    private val hisUid by lazy { arguments.user.uid }
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userDatabaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var permissions: Array<String>
    private lateinit var myUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        initFirebase()
        initPermissions()
        initNotificationPermission()
        initSendMessage()
        initUserInfo()
        initNavigateUp()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavigation()
        hideToolbar()
    }

    private fun initNavigateUp() {
        binding.backBtn.setOnClickListener { findNavController().popBackStack() }
    }

    private fun initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()
        userDatabaseReference = firebaseDatabase.getReference("Users")
    }

    private fun initUserInfo() {
        Toast.makeText(requireContext(), "Chating with ${arguments.user.name}", Toast.LENGTH_SHORT)
            .show()

        val userQuery = userDatabaseReference.orderByChild("uid").equalTo(hisUid)
        userQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { ds ->
                    val name = "" + ds.child("name").value
                    val image = "" + ds.child("avatar").value
                    binding.apply {
                        Glide.with(requireContext())
                            .load(image)
                            .into(avatar)
                        userName.text = name
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initSendMessage() {
        binding.messageEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.sendMessageBtn.isEnabled = s.toString().isNotEmpty()
                binding.sendMessageBtn.setOnClickListener { sendMessage(s.toString()) }
            }
        })
    }

    private fun sendMessage(message: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val values = hashMapOf<String, Any>()
        values.apply {
            put("sender", myUid)
            put("receiver", hisUid!!)
            put("message", message)
            databaseRef.child("Chats").push().setValue(values)
        }
        binding.messageEdt.text.clear()
    }

    private fun initPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun initNotificationPermission() {
        if (!checkNotificationPermission()) {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), permissions, Constants.REQUEST_CODE)
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == (PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }

    private fun alertNotificationPermission() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.enable_notification))

        builder.setPositiveButton(getString(R.string.enable)) { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }

    private fun checkUserStatus() {
        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user != null) {
            myUid = user.uid // Currently signed in user
        }
    }

    override fun onStart() {
        checkUserStatus()
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        showBottomNavigation()
        showToolbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}