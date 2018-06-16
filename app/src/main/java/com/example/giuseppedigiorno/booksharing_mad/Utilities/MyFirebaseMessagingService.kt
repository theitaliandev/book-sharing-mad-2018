package com.example.giuseppedigiorno.booksharing_mad.Utilities

import android.app.NotificationManager
import android.content.Context
import android.support.v4.app.NotificationCompat
import com.example.giuseppedigiorno.booksharing_mad.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val CHANNEL_ID = "BookSharing"

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("New Book Request")
                .setContentText("You've received a new book request")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.logo)

        var notificationId = System.currentTimeMillis().toInt()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

    }
}