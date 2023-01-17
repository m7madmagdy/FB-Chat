package com.example.socialmedia

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.socialmedia.data.User
import com.example.socialmedia.databinding.FragmentUsersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.lang.reflect.Field
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
            layoutManager = GridLayoutManager(requireContext(), 2)
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
                        binding.shimmerLayout.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: NullPointerException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
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
            R.id.user_layout -> Toast.makeText(requireContext(), user?.name, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun searchUsers(){
        binding.search.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!TextUtils.isEmpty(query)){
                    performSearch(query)
                }else{
                    getAllUsers()
                }
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (!TextUtils.isEmpty(query)){
                    performSearch(query)
                }else{
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}