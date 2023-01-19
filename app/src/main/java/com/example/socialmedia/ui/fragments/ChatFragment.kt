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
import com.example.socialmedia.R
import com.example.socialmedia.databinding.FragmentChatBinding
import com.example.socialmedia.ui.main.BaseFragment
import com.example.socialmedia.utils.Constants

class ChatFragment : BaseFragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val arguments: ChatFragmentArgs by navArgs()
    private lateinit var permissions: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        initPermissions()
        initNotificationPermission()
        handleMessage()
        backFragment()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavigation()
        hideToolbar()
    }

    private fun backFragment() {
        binding.backBtn.setOnClickListener { findNavController().popBackStack() }
    }

    private fun handleMessage() {
        binding.messageEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.sendMessageBtn.isEnabled = s.toString().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.sendMessageBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Sending..", Toast.LENGTH_SHORT).show()
        }
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