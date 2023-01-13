package com.example.socialmedia

import android.Manifest.permission.MANAGE_DOCUMENTS
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.socialmedia.databinding.FragmentFullScreenPhotoBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn

class FullScreenPhotoFragment : BaseFragment() {
    private var _binding: FragmentFullScreenPhotoBinding? = null
    private val binding get() = _binding!!
    private val arguments: FullScreenPhotoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullScreenPhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initScreenView()

        Log.d("permission", "OnCreated: ${checkPermission()}")
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigation()
        hideStatusBar()
        hideToolbar()
    }

    private fun initScreenView() {
        val imageUrl = arguments.imageUrl
        Glide.with(requireContext())
            .load(imageUrl)
            .into(binding.fullScreenPhoto)

        binding.saveImageBtn.setOnClickListener {
            saveImage()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun saveImage() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/jpeg"
            putExtra(Intent.EXTRA_TITLE, "yourImage.jpg")
        }
        signInResult.launch(intent)
    }

    private val signInResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmapDrawable = binding.fullScreenPhoto.drawable as BitmapDrawable
                val bitmap = bitmapDrawable.bitmap
                val uri = result.data?.data
                val outputStream = activity?.contentResolver?.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream?.flush()
                outputStream?.close()
                Toast.makeText(requireContext(), "Photo saved on this device", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkPermission() {
        val permission = MANAGE_DOCUMENTS
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(permission),
                PERMISSION_WRITE
            )
        }
    }

    companion object {
        private const val PERMISSION_WRITE = 99
    }

    override fun onStop() {
        super.onStop()
        showBottomNavigation()
        showStatusBar()
        showToolbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}