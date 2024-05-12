package com.kdapps.netluxdp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kdapps.netluxdp.R
import com.kdapps.netluxdp.activities.MainActivity

class MyFirebaseMessagingService: FirebaseMessagingService() {

    lateinit var sharedPreferences: SharedPreferences
    val TAG = "FCMService"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        //check if message contains data payload
        remoteMessage.data.isNotEmpty().let{
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            if(!remoteMessage.data.isNullOrEmpty()){
                val msgBody: String = remoteMessage.data.get("body").toString()
                val msgTitle: String = remoteMessage.data.get("title").toString()
                //compose and show notification4
                if(msgBody == "null"){
                    sendNotification("", msgTitle)
                } else{
                    sendNotification(msgBody, msgTitle)
                }

            }
        }

        //check if message contains a notification payload.
        remoteMessage.notification?.let{
            sendNotification(remoteMessage.notification?.body, remoteMessage.notification?.title)
        }
    }


    private fun sendNotification(messageBody: String?, messageTitle:String?){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =  PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = "NetluxDP_main"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_launcher_logo_round)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Netlux Dealer Portal",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }


    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        sharedPreferences = this.getSharedPreferences("dealer_details", MODE_PRIVATE)
        sharedPreferences.edit().putString("FCM_tokenID", token )
        Log.e("FCM_TOKEN", token)
    }


}

