package com.example.socialmedia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.socialmedia.databinding.FragmentProfileBinding
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
    private lateinit var userImage: Any

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

            avatarImage.setOnClickListener {
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToFullScreenPhotoFragment(
                        userImage.toString()
                    )
                findNavController().navigate(action)
            }

            coverPhoto.setOnClickListener {
                val imageUrl = getString(R.string.cover_image)
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToFullScreenPhotoFragment(
                        imageUrl
                    )
                findNavController().navigate(action)
            }

            addUserImage.setOnClickListener {
                Toast.makeText(requireContext(), getString(R.string.adding_photo), Toast.LENGTH_SHORT).show()
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
                    binding.apply {
                        userName.text = name
                        userEmail.text = email
                        userPhone.text = phone
                        Glide.with(requireContext()).load(userImage)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(avatarImage)
                    }
                }
                binding.apply {
                    shimmerLayout.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        // TODO: Loading Cover photo using Glide
        Glide.with(requireContext()).load(getString(R.string.cover_image))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.coverPhoto)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}