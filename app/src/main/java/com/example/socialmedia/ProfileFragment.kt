package com.example.socialmedia

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.socialmedia.databinding.BottomSheetEditProfileBinding
import com.example.socialmedia.databinding.BottomSheetPickImageBinding
import com.example.socialmedia.databinding.FragmentProfileBinding
import com.example.socialmedia.utils.Constants.IMAGE_PICK_CAMERA_CODE
import com.example.socialmedia.utils.Constants.IMAGE_PICK_GALLERY_CODE
import com.example.socialmedia.utils.Constants.REQUEST_CODE
import com.example.socialmedia.utils.Constants.STORAGE_PATH
import com.example.socialmedia.utils.ProgressDialog
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage.getInstance
import com.google.firebase.storage.StorageReference

class ProfileFragment : BaseFragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userImage: Any
    private lateinit var coverImage: Any
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var profileOrCoverPhoto: String
    private lateinit var permissions: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater)
        initFirebase()
        initUserClicks()
        initFirebaseAdmin()
        initPermissions()
        requestPermissions()
        return binding.root
    }

    private fun initPermissions() {
        permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == (PackageManager.PERMISSION_GRANTED)
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == (PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }

    private fun initFirebaseAdmin() {
        if (firebaseUser.email.toString() != getString(R.string.admin_email)) {
            binding.userName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    private fun initUserClicks() {
        binding.apply {
            signOut.setOnClickListener { alertUserSignOut() }
            editProfile.setOnClickListener { bottomSheetEditProfile() }
            addUserImage.setOnClickListener {
                bottomSheetCameraOrGallery()
                profileOrCoverPhoto = "avatar"
            }
            addCoverPhoto.setOnClickListener {
                bottomSheetCameraOrGallery()
                profileOrCoverPhoto = "cover"
            }

            avatarImage.setOnClickListener { profilePictureFullScreen() }
        }
    }

    private fun profilePictureFullScreen() {
        val fullScreenIntent = Intent(requireContext(), FullScreenPictureActivity::class.java)
        fullScreenIntent.putExtra("avatar", userImage.toString())
        startActivity(fullScreenIntent)
    }

    private fun initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("Users")
        storageReference = getInstance().reference

        val query = databaseReference.orderByChild("email").equalTo(firebaseUser.email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    for (ds in snapshot.children) {
                        val name = "" + ds.child("name").value
                        val email = "" + ds.child("email").value
                        val phone = "" + ds.child("phone").value
                        userImage = "" + ds.child("avatar").value
                        coverImage = "" + ds.child("cover").value

                        binding.apply {
                            userName.text = name
                            userEmail.text = email
                            userPhone.text = phone

                            Glide.with(requireContext())
                                .load(userImage)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .into(avatarImage)

                            Glide.with(requireContext())
                                .load(coverImage)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .into(coverPhoto)

                            shimmerLayout.visibility = View.GONE
                            userName.visibility = View.VISIBLE
                        }
                    }
                } catch (e: NullPointerException) {
                    Log.d("Firebase", "onCatchError: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun bottomSheetEditProfile() {
        val binding = BottomSheetEditProfileBinding.inflate(layoutInflater)

        val bottomSheet = BottomSheetDialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
            show()
        }

        binding.apply {
            profilePicture.setOnClickListener {
                bottomSheet.dismiss()
                bottomSheetCameraOrGallery()
                profileOrCoverPhoto = "avatar"
            }
            coverPhoto.setOnClickListener {
                bottomSheet.dismiss()
                bottomSheetCameraOrGallery()
                profileOrCoverPhoto = "cover"
            }
        }

        binding.apply {
            userName.setOnClickListener {
                bottomSheet.dismiss()
                showNamePhoneUpdateDialog("name")
            }
            userPhone.setOnClickListener {
                bottomSheet.dismiss()
                showNamePhoneUpdateDialog("phone")
            }
        }

        binding.closeSheetBtn.setOnClickListener { bottomSheet.dismiss() }
    }

    private fun bottomSheetCameraOrGallery() {
        val binding = BottomSheetPickImageBinding.inflate(layoutInflater)

        val bottomSheet = BottomSheetDialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
            show()
        }

        binding.apply {
            camera.setOnClickListener {
                if (!checkCameraPermission()) {
                    requestPermissions()
                } else {
                    openCamera()
                    bottomSheet.dismiss()
                }
            }
            gallery.setOnClickListener {
                if (!checkStoragePermission()) {
                    requestPermissions()
                } else {
                    openGallery()
                    bottomSheet.dismiss()
                }
            }
        }

        binding.closeSheetBtn.setOnClickListener { bottomSheet.dismiss() }
    }

    private fun openGallery() {
        val galleryIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(MediaStore.ACTION_PICK_IMAGES)
        } else {
            Intent(Intent.ACTION_PICK)
        }
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun openCamera() {
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

    @SuppressLint("SetTextI18n")
    private fun showNamePhoneUpdateDialog(key: String) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_user_details, null);
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val editLayout = dialogView.findViewById(R.id.user_layout) as TextInputLayout
        val nameOrPhone = dialogView.findViewById(R.id.name_or_phone) as TextInputEditText
        val headerName = dialogView.findViewById(R.id.edit_name) as TextView
        val updateBtn = dialogView.findViewById(R.id.update_btn) as Button
        val cancelBtn = dialogView.findViewById(R.id.cancel_btn) as Button
        val userName = binding.userName.text.toString().trim()
        val userPhone = binding.userPhone.text.toString().trim()

        when (key) {
            "name" -> nameOrPhone.setText(userName)
            "phone" -> nameOrPhone.setText(userPhone)
        }

        headerName.text = "Update $key"
        nameOrPhone.hint = "Edit $key"
        val dialog = builder.create()
        dialog.show()

        cancelBtn.setOnClickListener { dialog.dismiss() }
        updateBtn.setOnClickListener {
            val value = nameOrPhone.text.toString().trim()

            if (value.isNotEmpty()) {
                val result = hashMapOf<String, Any>()
                result[key] = value

                databaseReference.child(firebaseUser.uid).updateChildren(result)

                    .addOnSuccessListener {
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show()
                    }

                    .addOnFailureListener {
                        dialog.dismiss()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
            } else {
                editLayout.error = "Please enter value"
            }
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_GALLERY_CODE -> {
                    imageUri = data?.data!!
                    uploadPhoto(imageUri)
                }
                IMAGE_PICK_CAMERA_CODE -> {
                    uploadPhoto(imageUri)
                }
            }
        }
    }

    private fun uploadPhoto(uri: Uri) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.show("Updating $profileOrCoverPhoto")
        val filePathAndName = STORAGE_PATH + "" + profileOrCoverPhoto + "_" + firebaseUser.uid
        val storageReference2 = storageReference.child(filePathAndName)
        storageReference2.putFile(uri)

            .addOnSuccessListener {
                val uriTask: Task<Uri> = it.storage.downloadUrl
                while (!uriTask.isSuccessful) {
                }
                val downloadUri = uriTask.result

                if (uriTask.isSuccessful) {
                    val results = hashMapOf<String, Any>()
                    results.apply { put(profileOrCoverPhoto, downloadUri.toString()) }
                    databaseReference.child(firebaseUser.uid).updateChildren(results)
                        .addOnSuccessListener { progressDialog.hideDialog() }
                        .addOnFailureListener {
                            progressDialog.hideDialog()
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                }
            }

            .addOnFailureListener {
                progressDialog.hideDialog()
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}