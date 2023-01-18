package com.example.socialmedia.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.socialmedia.databinding.FragmentChatBinding
import com.example.socialmedia.ui.main.BaseFragment

class ChatFragment : BaseFragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val arguments: ChatFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        handleMessage()
        backFragment()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavigation()
        hideToolbar()
        Log.d("TAG", "onViewCreated: ${arguments.uid}")
    }

    private fun backFragment() {
        binding.backBtn.setOnClickListener { findNavController().popBackStack() }
    }

    private fun handleMessage() {
        binding.messageEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.sendMessageBtn.isEnabled = s.toString().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.sendMessageBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Sending..", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavigation()
        showToolbar()
        _binding = null
    }

}