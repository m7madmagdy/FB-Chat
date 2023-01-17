package com.example.socialmedia

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.example.socialmedia.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        menuProviders()
        return binding.root
    }

    private fun menuProviders() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)

                val searchItem = menu.findItem(R.id.search)
                searchItem.isVisible = false
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return when (menuItem.itemId) {
                    R.id.log_out -> {
                        alertUserSignOut()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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