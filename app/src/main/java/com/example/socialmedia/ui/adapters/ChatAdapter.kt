package com.example.socialmedia.ui.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmedia.R
import com.example.socialmedia.data.Chat
import com.example.socialmedia.utils.Constants.MSG_TYPE_LEFT
import com.example.socialmedia.utils.Constants.MSG_TYPE_RIGHT
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private lateinit var fUser: FirebaseUser
    private lateinit var messageTv: TextView
    private lateinit var timeTv: TextView
    private lateinit var isSeenTv: TextView
    private var chatList: ArrayList<Chat?> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, view: Int): ChatViewHolder {
        return if (view == MSG_TYPE_RIGHT) {
            val rowRightChat =
                LayoutInflater.from(parent.context).inflate(R.layout.row_chat_right, parent, false)
            ChatViewHolder(rowRightChat)
        } else {
            val rowLeftChat =
                LayoutInflater.from(parent.context).inflate(R.layout.row_chat_left, parent, false)
            ChatViewHolder(rowLeftChat)
        }
    }

    override fun getItemCount(): Int = chatList.size

    override fun getItemViewType(position: Int): Int {
        fUser = FirebaseAuth.getInstance().currentUser!!
        return if (chatList[position]?.sender.equals(fUser.uid)) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = chatList[position]?.message
        val timeStamp = chatList[position]?.timestamp
        val date = Date(timeStamp!!)
        val formatter = SimpleDateFormat("EEEE hh:mm aa", Locale.getDefault())
        val result = formatter.format(date)
        timeTv.text = result
        messageTv.text = message

        if (position == chatList.size - 1) {
            isSeenTv.setText("Delivered")

//            if (chatList[position]?.isSeen == true){
//                isSeenTv.setText("Seen")
//            }else{
//                isSeenTv.setText("Delivered")
//            }
        }else{
            isSeenTv.isVisible = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setChats(chats: ArrayList<Chat?>) {
        chatList = chats
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            messageTv = itemView.findViewById(R.id.message_tv)
            timeTv = itemView.findViewById(R.id.time_tv)
            isSeenTv = itemView.findViewById(R.id.is_sent_tv)
        }
    }
}