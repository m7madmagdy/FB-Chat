package com.example.socialmedia.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socialmedia.R
import com.example.socialmedia.data.Chat
import com.example.socialmedia.data.User
import com.example.socialmedia.databinding.ActivityChatBinding
import com.example.socialmedia.firebase.*
import com.example.socialmedia.firebase.Client.getRetrofit
import com.example.socialmedia.ui.adapters.ChatAdapter
import com.example.socialmedia.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback

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
    private lateinit var apiService: APIService
    private var notify: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chatList = ArrayList()
        initFirebase()
        initApiService()
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
                    binding.sendMessageBtn.setOnClickListener {
                        notify = true
                        sendMessage(s.toString())
                    }
                }
            }
        })
        readMessages()
    }

    private fun initApiService() {
        apiService = getRetrofit("https://fcm.googleapis.com/")!!.create(APIService::class.java)
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

        val msg = message
        val db = FirebaseDatabase.getInstance().getReference("Users").child(myUid)
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                if (notify) {
                    sendNotification(hisUid, user!!.name, message)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendNotification(hisUid: String, name: String?, message: String) {
        val myUid = firebaseUser.uid
        val allTokens = FirebaseDatabase.getInstance().getReference("Tokens")
        val query = allTokens.orderByKey().equalTo(hisUid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { ds ->
                    val token = ds.getValue(Token::class.java)
                    val data = Data(
                        myUid,
                        "$name:$message",
                        "New Message",
                        hisUid,
                        R.drawable.firebase_icon
                    )
                    val sender = Sender(data, token?.token)
                    apiService.sendNotification(sender)
                        .enqueue(object : Callback<Response> {
                            override fun onResponse(
                                call: Call<Response>,
                                response: retrofit2.Response<Response>
                            ) {
                                if (response.isSuccessful){
                                    Toast.makeText(this@ChatActivity, response.message(), Toast.LENGTH_SHORT).show()
                                    Log.d("TAG", "onSuccess: ${response.message()} ${response.body()}")
                                }
                            }

                            override fun onFailure(call: Call<Response>, t: Throwable) {
                                Toast.makeText(this@ChatActivity, t.message, Toast.LENGTH_SHORT).show()
                                Log.d("TAG", "onFailure: ${t.message}")
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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