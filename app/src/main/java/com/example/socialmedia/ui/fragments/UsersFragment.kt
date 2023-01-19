package com.example.socialmedia.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialmedia.R
import com.example.socialmedia.data.User
import com.example.socialmedia.databinding.FragmentUsersBinding
import com.example.socialmedia.ui.adapters.UsersAdapter
import com.example.socialmedia.ui.main.BaseFragment
import com.example.socialmedia.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.*
import java.util.*


class UsersFragment : BaseFragment() {
    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private val usersAdapter = UsersAdapter { v, position -> onUserItemClick(v, position) }
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var usersList: ArrayList<User?>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(layoutInflater)
        usersList = ArrayList()
        initFirebase()
        initRecyclerView()
        getAllUsers()
        searchUsers()
        return binding.root
    }

    private fun initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("Users")
    }

    private fun initRecyclerView() {
        binding.usersRecyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = usersAdapter
        }
    }

    private fun getAllUsers() {

        try {
            firebaseDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersList.clear()
                    snapshot.children.forEach {
                        val modelUser = it.getValue(User::class.java)
                        if (!modelUser?.uid.equals(firebaseUser.uid)) {
                            usersList.add(modelUser)
                        }
                        usersAdapter.setUsers(usersList)
                        binding.apply {
                            shimmerLayout.isVisible = false
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: NullPointerException) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun performSearch(query: String?) {

        firebaseDatabase.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                snapshot.children.forEach {
                    val modelUser = it.getValue(User::class.java)
                    if (!modelUser?.uid.equals(firebaseUser.uid)) {
                        if (modelUser?.name?.lowercase(Locale.ROOT)!!
                                .contains(query!!.lowercase(Locale.ROOT)) || modelUser.email?.lowercase(
                                Locale.ROOT
                            )!!.contains(query)
                        ) {
                            usersList.add(modelUser)
                        }
                    }
                    usersAdapter.setUsers(usersList)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun onUserItemClick(v: View, position: Int) {
        val user = usersAdapter.getUser(position)
        when (v.id) {
            R.id.user_layout -> {
                val action =
                    UsersFragmentDirections.actionUsersFragmentToChatFragment(user?.uid.toString())
                findNavController().navigate(action)
            }
        }
    }

    private fun searchUsers() {
        binding.search.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!TextUtils.isEmpty(query)) {
                    performSearch(query)
                } else {
                    getAllUsers()
                }
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (!TextUtils.isEmpty(query)) {
                    performSearch(query)
                } else {
                    getAllUsers()
                }
                return false
            }
        })
    }

    private fun checkUserStatus() {
        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user == null) {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            activity?.finish()
        }
    }
}