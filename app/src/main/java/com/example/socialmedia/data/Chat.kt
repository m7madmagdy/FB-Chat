package com.example.socialmedia.data

data class Chat(
    var message: String? = null,
    var receiver: String? = null,
    var sender: String? = null,
    var timestamp: Long? = null,
    var isSeen: Boolean? = null,
)
