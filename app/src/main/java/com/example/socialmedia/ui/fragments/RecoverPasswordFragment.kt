package com.example.socialmedia.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.socialmedia.R
import com.example.socialmedia.databinding.FragmentRecoverPasswordBinding
import com.example.socialmedia.utils.ProgressDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth

class RecoverPasswordFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentRecoverPasswordBinding? = null
    private val binding get() = _binding!!
    private var behavior: BottomSheetBehavior<*>? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverPasswordBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.emailEdt.requestFocus().apply { showKeyboard() }
        initProductSheet()
    }

    private fun initProductSheet() {
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        behavior = BottomSheetBehavior.from(bottomSheet!!).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
        }

        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams

        binding.recoverBtn.setOnClickListener {
            val email = binding.emailEdt.text.toString().trim()
            if (email.isEmpty()) {
                binding.emailLayout.error = getString(R.string.type_your_email_first)
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailLayout.error = getString(R.string.invalid_email)
            } else {
                beginRecovery(email)
            }
        }

        binding.apply {
            cancelBtn.setOnClickListener {
                behavior?.state = BottomSheetBehavior.STATE_HIDDEN
            }
            closeSheetBtn.setOnClickListener {
                behavior?.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun beginRecovery(email: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.show(getString(R.string.sending_email))

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.hideDialog()
                    behavior?.state = BottomSheetBehavior.STATE_HIDDEN
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_your_mail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            .addOnFailureListener {
                progressDialog.hideDialog()
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun showKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.emailEdt, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

