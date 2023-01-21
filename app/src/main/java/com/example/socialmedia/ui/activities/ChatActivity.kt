package com.example.socialmedia.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socialmedia.R
import com.example.socialmedia.data.Chat
import com.example.socialmedia.databinding.ActivityChatBinding
import com.example.socialmedia.ui.adapters.ChatAdapter
import com.example.socialmedia.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private var _binding: ActivityChatBinding? = null
    private val binding get() = _binding!!
    private val chatAdapter = ChatAdapter()
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userDatabaseReference: DatabaseReference
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var permissions: Array<String>
    private lateinit var hisImage: String
    private lateinit var hisUid: String
    private lateinit var seenListener: ValueEventListener
    private lateinit var userRefForSeen: DatabaseReference
    private lateinit var chatList: ArrayList<Chat?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chatList = ArrayList()
        initFirebase()
        initChatRecyclerview()
        initPermissions()
        initNotificationPermission()
        initUserInfo()
        initActionBar()
        initSendMessage()
    }

    private fun initActionBar() {
        val toolbar = binding.chatToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHomeAsUpIndicator(R.drawable.navigate_up_back_left)
        }
    }

    private fun initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
        userDatabaseReference = firebaseDatabase.getReference("Users")
    }

    private fun initUserInfo() {
        hisUid = intent.getStringExtra("hisUid").toString()
        val userQuery = userDatabaseReference.orderByChild("uid").equalTo(hisUid)
        userQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { ds ->
                    val name = "" + ds.child("name").value
                    val email = "" + ds.child("email").value
                    hisImage = "" + ds.child("avatar").value
                    binding.apply {
                        Glide.with(this@ChatActivity)
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
            put("receiver", hisUid)
            put("message", message)
            put("timestamp", timeStamp)
            put("isSeen", false)
            databaseRef.child("Chats").push().setValue(values)
        }
        val soundEffect = MediaPlayer.create(this, R.raw.message_sent)
        soundEffect.start()
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
            val linearLayout = LinearLayoutManager(this@ChatActivity)
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
        ActivityCompat.requestPermissions(this, permissions, Constants.REQUEST_CODE)
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == (PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}