package com.example.socialmedia.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.socialmedia.ui.activities.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseNotification : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val sp = getSharedPreferences("SP_USER", 0)
        val savedCurrentUser = sp.getString("Current_UserID", "None")

        val sent = message.data["sent"]
        val user = message.data["user"]

        val fUser = FirebaseAuth.getInstance().currentUser
        if (fUser != null && sent.equals(fUser.uid)) {
            if (!savedCurrentUser.equals(user)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOAndAboveNotification(message)
                } else {
                    sendNormalNotification(message)
                }
            }
        }
    }


    private fun sendNormalNotification(message: RemoteMessage) {
        val user = message.data["user"]
        val icon = message.data["icon"]
        val title = message.data["title"]
        val body = message.data["body"]

        val remoteMessage = message.notification
        val i = user?.replace("\\D".toRegex(), "")?.toInt()
        val intent = Intent(this, ChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("hisUid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(this, i!!, intent, PendingIntent.FLAG_IMMUTABLE)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon!!.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var j = 0
        if (i > 0) {
            j = i
        }
        notificationManager.notify(j, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendOAndAboveNotification(message: RemoteMessage) {
        val user = message.data["user"]
        val icon = message.data["icon"]
        val title = message.data["title"]
        val body = message.data["body"]
        message.notification
        val i = user?.replace("\\D".toRegex(), "")?.toInt()
        val intent = Intent(this, ChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("hisUid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(this, i!!, intent, PendingIntent.FLAG_IMMUTABLE)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val oreoNotification = OreoAndAboveNotification(this)
        val builder = oreoNotification.getNotifications(title, body, pendingIntent, soundUri, icon)
        var j = 0
        if (i > 0) {
            j = i
        }
        oreoNotification.manager.notify(j, builder.build())
    }

}