package app.vercel.srapp_10db2.twa.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import app.vercel.srapp_10db2.twa.MainActivity
import app.vercel.srapp_10db2.twa.R
import app.vercel.srapp_10db2.twa.model.SplashViewModel

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class SprsFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }


    override fun onNewToken(token: String) {
        Log.d(TAG, "room token: $token")
        //token을 서버로 전송
        saveFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        //수신한 메시지를 처리
        //val token = FirebaseMessaging.getInstance().token.result

        //check messages

        Log.d("Firebase", "FirebaseMessagingService From: ${remoteMessage.from}")
        Log.d("Firebase", "FirebaseMessagingService title: ${remoteMessage.notification?.title}")
        Log.d("Firebase", "FirebaseMessagingService body: ${remoteMessage.notification?.body}")
        Log.d("Firebase", "FirebaseMessagingService link: ${ remoteMessage.data["link"]}")
            sendNotification(
                remoteMessage.data,
                remoteMessage.notification?.title,
                remoteMessage.notification?.body
            )


    }


    private fun getChannelId(): String {
        return ChannelIds.DEFAULT.value
    }

    private fun getSilentChannelId(): String {
        return ChannelIds.SILENT.value
    }

    private fun saveFcmToken (token: String) {
      //  sessionManager.accessToken = token
    }

    private fun sendNotification(data: Map<String, String>, title: String?, body: String?) {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if(data.isNotEmpty()) {
                putExtra("link", data["link"]);
            }
        }


        val pIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
           PendingIntent.getActivity(
                this,
                1, intent,
                PendingIntent.FLAG_MUTABLE)

        } else {
                PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        }


        //val isSilent = TextUtils.isEmpty(data.getValue("KEY_SOUND"))

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 소리

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.push)
            .setContentTitle(title) // 제목
            .setContentText(body) // 내용
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pIntent) //클릭시 해당 인텐트 실행

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SPRS 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel (channel)
        }
        notificationManager.notify(0 , notificationBuilder.build())
    }

    private enum class ChannelIds(val value: String) {
        DEFAULT("SPRS"),
        SILENT("SPRS_silent")
    }
}