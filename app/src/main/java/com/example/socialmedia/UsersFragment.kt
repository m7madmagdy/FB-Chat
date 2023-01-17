package com.example.socialmedia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.socialmedia.data.User
import com.example.socialmedia.databinding.FragmentUsersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class UsersFragment : BaseFragment() {
    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private val usersAdapter = UsersAdapter { v, position -> onUserItemClick(v, position) }
    private var firebaseUser: FirebaseUser? = null
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var usersList: ArrayList<User?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(layoutInflater)
        initFirebase()
        initRecyclerView()
        getAllUsers()
        return binding.root
    }

    private fun initFirebase() {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("Users")
    }

    private fun initRecyclerView() {
        binding.usersRecyclerview.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = usersAdapter
        }
    }

    private fun getAllUsers() {
        usersList = ArrayList()

        try {
            firebaseDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val modelUser = it.getValue(User::class.java)
                        if (!modelUser?.uid.equals(firebaseUser?.uid)) {
                            usersList.add(modelUser)
                        }
                        usersAdapter.setUsers(usersList)
                        binding.shimmerLayout.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: NullPointerException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onUserItemClick(v: View, position: Int) {
        val user = usersAdapter.getUser(position)
        when (v.id) {
            R.id.user_layout -> Toast.makeText(requireContext(), user?.name, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}