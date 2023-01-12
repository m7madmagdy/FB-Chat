package com.example.socialmedia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.socialmedia.databinding.FragmentHomeBinding
import com.example.socialmedia.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ProfileFragment : BaseFragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater)
        initFirebase()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.avatarImage.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked", Toast.LENGTH_SHORT).show()
        }
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
                    val image = "" + ds.child("image").value

                    binding.apply {
                        userName.text = name
                        userEmail.text = email
                        userPhone.text = phone
                        Glide.with(requireContext()).load(image).into(avatarImage)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        // TODO: Loading Cover photo using Glide
        Glide.with(requireContext()).load(getString(R.string.cover_image)).into(binding.coverPhoto)
    }

    override fun backIndicator() {
        val activity = activity as AppCompatActivity
        activity.supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.navigate_up_back_left)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}