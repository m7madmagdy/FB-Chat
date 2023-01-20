package com.example.socialmedia.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socialmedia.R
import com.example.socialmedia.data.Chat
import com.example.socialmedia.databinding.FragmentChatBinding
import com.example.socialmedia.ui.adapters.ChatAdapter
import com.example.socialmedia.ui.main.BaseFragment
import com.example.socialmedia.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChatFragment : BaseFragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val arguments: ChatFragmentArgs by navArgs()
    private val hisUid by lazy { arguments.user.uid }
    private val chatAdapter = ChatAdapter()
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userDatabaseReference: DatabaseReference
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var permissions: Array<String>
    private lateinit var hisImage: String
    private lateinit var seenListener: ValueEventListener
    private lateinit var userRefForSeen: DatabaseReference
    private lateinit var chatList: ArrayList<Chat?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        chatList = ArrayList()
        initFirebase()
        initChatRecyclerview()
        initPermissions()
        initNotificationPermission()
        initSendMessage()
        initUserInfo()
        initNavigateUp()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavigation()
        hideToolbar()
    }

    private fun initNavigateUp() {
        binding.backBtn.setOnClickListener { findNavController().popBackStack() }
    }

    private fun initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
        userDatabaseReference = firebaseDatabase.getReference("Users")
    }

    private fun initUserInfo() {
        val args = arguments.user
        Toast.makeText(requireContext(), "Chating with ${args.name}", Toast.LENGTH_SHORT)
            .show()

        val userQuery = userDatabaseReference.orderByChild("uid").equalTo(hisUid)
        userQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { ds ->
                    val name = "" + ds.child("name").value
                    val email = "" + ds.child("email").value
                    hisImage = "" + ds.child("avatar").value
                    binding.apply {
                        Glide.with(requireContext())
                            .load(hisImage)
                            .into(avatar)
                        userName.text = name
                        if (email != root.context.getString(R.string.admin_email)) {
                            userName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                userName.tooltipText = "Verified"
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun initSendMessage() {
        binding.messageEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val messageTxt = binding.messageEdt.text.toString().trim()

                if (TextUtils.isEmpty(messageTxt)) {
                    binding.sendMessageBtn.isEnabled = false
                } else {
                    binding.sendMessageBtn.isEnabled = true
                    binding.sendMessageBtn.setOnClickListener { sendMessage(s.toString()) }
                }
            }
        })
        readMessages()
    }

    private fun readMessages() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Chats")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                snapshot.children.forEach { ds ->
                    val chat = ds.getValue(Chat::class.java)
                    if (chat?.receiver.equals(firebaseUser.uid) && chat?.sender.equals(hisUid) || chat?.receiver.equals(
                            hisUid
                        ) && chat?.sender.equals(firebaseUser.uid)
                    ) {
                        chatList.add(chat)
                        chatAdapter.setChats(chatList)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendMessage(message: String) {
        val myUid = firebaseUser.uid
        val timeStamp = System.currentTimeMillis()
        val values = hashMapOf<String, Any>()
        values.apply {
            put("sender", myUid)
            put("receiver", hisUid!!)
            put("message", message)
            put("timestamp", timeStamp)
            put("isSeen", false)
            databaseRef.child("Chats").push().setValue(values)
        }
        val soundEffect = MediaPlayer.create(requireContext(), R.raw.message_sent)
        soundEffect.start()
        Toast.makeText(requireContext(), "Message sent", Toast.LENGTH_SHORT).show()
        binding.messageEdt.text.clear()
    }

    private fun seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats")
        seenListener = userRefForSeen.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { ds ->
                    val chat = ds.getValue(Chat::class.java)
                    if (chat?.receiver.equals(firebaseUser.uid) && chat?.sender.equals(hisUid)) {
                        val values = hashMapOf<String, Any>()
                        values.put("isSeen", true)
                        ds.ref.updateChildren(values)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    private fun initChatRecyclerview() {
        binding.chatRecyclerview.apply {
            setHasFixedSize(true)
            val linearLayout = LinearLayoutManager(requireContext())
            linearLayout.stackFromEnd = true
            layoutManager = linearLayout
            adapter = chatAdapter
        }
    }

    private fun initPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun initNotificationPermission() {
        if (!checkNotificationPermission()) {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), permissions, Constants.REQUEST_CODE)
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == (PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }

    private fun alertNotificationPermission() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.enable_notification))

        builder.setPositiveButton(getString(R.string.enable)) { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavigation()
        showToolbar()
        _binding = null
    }
}