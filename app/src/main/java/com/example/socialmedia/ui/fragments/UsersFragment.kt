package com.example.socialmedia.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialmedia.R
import com.example.socialmedia.data.User
import com.example.socialmedia.databinding.FragmentUsersBinding
import com.example.socialmedia.ui.activities.ChatActivity
import com.example.socialmedia.ui.adapters.UsersAdapter
import com.example.socialmedia.ui.main.BaseFragment
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
        usersList = ArrayList()
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
                            usersAdapter.setUsers(usersList)
                            binding.shimmerLayout.isVisible = false
                        }
                    }

                    if (usersList.isEmpty()){
                        binding.shimmerLayout.isVisible = false
                        Toast.makeText(requireContext(), "Not found users yet", Toast.LENGTH_SHORT).show()
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
                snapshot.children.forEach { ds ->
                    val modelUser = ds.getValue(User::class.java)
                    val queryName = modelUser?.name?.lowercase(Locale.ROOT)!!
                        .contains(query!!.lowercase(Locale.ROOT))
                    val queryEmail = modelUser.email?.lowercase(Locale.ROOT)!!.contains(query)
                    if (!modelUser.uid.equals(firebaseUser.uid)) {
                        if (queryName || queryEmail) {
                            usersList.add(modelUser)
                            usersAdapter.setUsers(usersList)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun onUserItemClick(v: View, position: Int) {
        val user = usersAdapter.getUser(position)
        when (v.id) {
            R.id.user_layout -> {
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("hisUid", user?.uid)
                startActivity(intent)
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
}