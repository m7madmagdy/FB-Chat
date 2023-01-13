package com.example.socialmedia

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.socialmedia.databinding.BottomSheetChooserBinding
import com.example.socialmedia.databinding.FragmentProfileBinding
import com.example.socialmedia.utils.Constants.CAMERA_REQUEST_CODE
import com.example.socialmedia.utils.Constants.IMAGE_PICK_CAMERA_CODE
import com.example.socialmedia.utils.Constants.IMAGE_PICK_GALLERY_CODE
import com.example.socialmedia.utils.Constants.STORAGE_REQUEST_CODE
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class ProfileFragment : BaseFragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>
    private lateinit var userImage: Any
    private lateinit var coverImage: Any
    private lateinit var imageUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater)
        initFirebase()
        initUserClicks()
        arrayPermissions()
        return binding.root
    }

    private fun initUserClicks() {
        binding.apply {
            signOut.setOnClickListener {
                alertUserSignOut()
            }

            avatarImage.setOnClickListener {
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToFullScreenPhotoFragment(
                        userImage.toString()
                    )
                findNavController().navigate(action)
            }

            coverPhoto.setOnClickListener {
                val imageUrl = getString(R.string.cover_image_url)
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToFullScreenPhotoFragment(
                        imageUrl
                    )
                findNavController().navigate(action)
            }

            addUserImage.setOnClickListener {
                dialogChooser()
            }

            addCoverPhoto.setOnClickListener {
                dialogChooser()
            }
        }
    }

    private fun dialogChooser() {
        val binding = BottomSheetChooserBinding.inflate(layoutInflater)

        val bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
            show()
        }

        binding.camera.setOnClickListener {
            if (!checkCameraPermission()) {
                requestCameraPermission()
            } else {
                pickFromCamera()
            }
            bottomSheetDialog.dismiss()
        }

        binding.gallery.setOnClickListener {
            if (!checkStoragePermission()) {
                    requestStoragePermission()
            } else {
                pickFromGallery()
            }
            bottomSheetDialog.dismiss()
        }

        binding.closeSheetBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun alertUserSignOut() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.sign_out))

        builder.setPositiveButton(getString(R.string.sign_out)) { _, _ ->
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }

    private fun initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("Users")

        val query = databaseReference.orderByChild("email").equalTo(firebaseUser.email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val name = "" + ds.child("name").value
                    val email = "" + ds.child("email").value
                    val phone = "" + ds.child("phone").value
                    userImage = "" + ds.child("image").value
                    coverImage = "" + ds.child("cover").value

                    binding.apply {
                        userName.text = name
                        userEmail.text = email
                        userPhone.text = phone

                        Glide.with(requireActivity())
                            .load(coverImage)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(coverPhoto)

                        Glide.with(requireContext())
                            .load(userImage)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(avatarImage)
                    }
                }
                binding.apply {
                    shimmerLayout.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun arrayPermissions() {
        cameraPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_MEDIA_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_MEDIA_LOCATION)
        } else {
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_MEDIA_LOCATION
            ) == (PackageManager.PERMISSION_DENIED)
        } else {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == (PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            storagePermissions,
            STORAGE_REQUEST_CODE
        )
    }

    private fun checkCameraPermission(): Boolean {
        val cameraResult: Boolean = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == (PackageManager.PERMISSION_GRANTED)

        val storageResult: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_MEDIA_LOCATION
            ) == (PackageManager.PERMISSION_GRANTED)
        } else {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == (PackageManager.PERMISSION_GRANTED)
        }

        return cameraResult && storageResult
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun pickFromCamera() {
        val values = ContentValues()
        values.apply {
            put(MediaStore.Images.Media.TITLE, "Temp Pic")
            put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
        }
        imageUri = activity?.contentResolver?.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )!!
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty())) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enable_permissions),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty())) {
                    val writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (writeStorageAccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enable_permissions),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK) {
            val selectedPhotoUri = data?.data
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}