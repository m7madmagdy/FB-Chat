package com.example.socialmedia

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.socialmedia.databinding.BottomSheetEditProfileBinding
import com.example.socialmedia.databinding.FragmentProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater)
        initFirebase()
        initUserClicks()
        return binding.root
    }

    private fun initUserClicks() {
        binding.apply {
            signOut.setOnClickListener {
                alertUserSignOut()
            }

            editProfile.setOnClickListener {
                bottomSheetEditProfile()
            }
            addUserImage.setOnClickListener { bottomSheetEditProfile() }
            addCoverPhoto.setOnClickListener { bottomSheetEditProfile() }
        }
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

                        Glide.with(requireContext())
                            .load(userImage)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(avatarImage)

                        Glide.with(requireActivity())
                            .load(getString(R.string.cover_image_url))
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(coverPhoto)

                        shimmerLayout.visibility = View.INVISIBLE
                    }
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
            userName.setOnClickListener {
                showNamePhoneUpdateDialog("name")
            }
            userPhone.setOnClickListener {
                showNamePhoneUpdateDialog("phone")
            }
        }

        binding.closeSheetBtn.setOnClickListener {
            bottomSheet.dismiss()
        }
    }

    private fun showNamePhoneUpdateDialog(key: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Update $key")
        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(20, 20, 20, 20)

        val editText = EditText(requireContext())
        editText.hint = "Edit $key"
        linearLayout.addView(editText)
        builder.setView(linearLayout)

        builder.setPositiveButton("UPDATE") { _, _ ->
            val value = editText.text.toString().trim()

            if (!TextUtils.isEmpty(value)) {
                val result = hashMapOf<String, Any>()
                result.put(key, value)

                databaseReference.child(firebaseUser.uid).updateChildren(result)

                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show()
                    }

                    .addOnFailureListener {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Please Enter $key", Toast.LENGTH_SHORT).show()

            }
        }
        builder.setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}