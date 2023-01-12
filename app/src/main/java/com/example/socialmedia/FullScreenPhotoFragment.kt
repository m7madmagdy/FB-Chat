package com.example.socialmedia

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.socialmedia.databinding.FragmentFullScreenPhotoBinding

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
        hideBottomNavigation()
        initScreenView()
        hideStatusBar()
        hideToolbar()
    }

    private fun initScreenView() {
        val imageUrl = arguments.imageUrl
        Glide.with(requireContext())
            .load(imageUrl)
            .into(binding.fullScreenPhoto)

        binding.saveImageBtn.setOnClickListener {
            Log.d("TAG", "initScreenView: Clicked")
            Log.d("TAG", checkPermission().toString())

            if (checkPermission()) {
                saveImage()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.photo_saved_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun saveImage() {
        val bitmapDrawable = binding.fullScreenPhoto.drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap
        @Suppress("DEPRECATION") val bitmapPath =
            MediaStore.Images.Media.insertImage(activity?.contentResolver, bitmap, "", null)
        val bitmapUri = Uri.parse(bitmapPath)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/jpg/png"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "")
        shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        startActivity(Intent.createChooser(shareIntent, ""))
    }

    private fun checkPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(WRITE_EXTERNAL_STORAGE),
                PERMISSION_WRITE
            )
            false
        }
    }

    companion object {
        private const val PERMISSION_WRITE = 0
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