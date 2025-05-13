package com.example.pls.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pls.MainActivity
import com.example.pls.R
import com.example.pls.utils.UserSessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    // URL ของเซิร์ฟเวอร์ Node.js
    private val SERVER_URL = "http://124.121.176.140:3000" // สำหรับ Emulator
    // หากใช้อุปกรณ์จริง ให้เปลี่ยนเป็น "http://192.168.x.x:3000" (IP ของเครื่องที่รันเซิร์ฟเวอร์)

    /**
     * เมื่อ Service ถูกสร้าง ให้ subscribe ไปยัง topics ที่ต้องการ
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "===== FCM Service created =====")
        // Subscribe ไปยัง topics ที่เกี่ยวข้อง
        subscribeToTopics()
    }

    /**
     * เรียกเมื่อได้รับ FCM token ใหม่
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "===== Refreshed FCM token: $token =====")

        // Subscribe ไปยัง topics ทันทีเมื่อได้รับ token ใหม่
        subscribeToTopics()

        // ถ้าผู้ใช้ล็อกอินอยู่ ให้ส่ง token ไปยังเซิร์ฟเวอร์
        val userSessionManager = UserSessionManager(applicationContext)
        if (userSessionManager.isLoggedIn()) {
            // บันทึก token ลงในเครื่อง
            userSessionManager.saveFcmToken(token)

            // ส่ง token ไปยังเซิร์ฟเวอร์
            sendRegistrationToServer(token)
        }
    }

    /**
     * เรียกเมื่อได้รับข้อความจาก FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "===== FCM Message Received From: ${remoteMessage.from} =====")

        // แสดง log เพิ่มเติมเพื่อตรวจสอบการรับข้อความ
        Log.d(TAG, "FCM Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "FCM Message Type: ${remoteMessage.messageType}")
        Log.d(TAG, "FCM Message TTL: ${remoteMessage.ttl}")

        // ตรวจสอบว่ามีข้อมูลหรือไม่
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // ถ้าเป็นการแจ้งเตือนสถานะรถวีลแชร์
            if (remoteMessage.data["type"] == "wheelchair-status") {
                val wheelchairId = remoteMessage.data["wheelchairId"]
                val status = remoteMessage.data["status"]
                Log.d(TAG, "Wheelchair status update - ID: $wheelchairId, Status: $status")

                // สร้างการแจ้งเตือนจากข้อมูลที่ได้รับ
                val title = "สถานะรถวีลแชร์"
                val message = "รถวีลแชร์ $wheelchairId: ${if (status == "active") "ออนไลน์" else "ออฟไลน์"}"
                sendNotification(title, message)
            } else {
                // ถ้าเป็นประเภทอื่น ให้แสดงข้อมูลทั้งหมด
                val title = remoteMessage.data["title"] ?: "มีการแจ้งเตือนใหม่"
                val body = remoteMessage.data["body"] ?: "คุณมีการแจ้งเตือนใหม่จากระบบ"
                sendNotification(title, body)
            }
        }

        // ตรวจสอบว่ามีการแจ้งเตือนหรือไม่
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Title: ${it.title}")
            Log.d(TAG, "Message Notification Body: ${it.body}")

            // สร้างและแสดงการแจ้งเตือน
            it.body?.let { body ->
                sendNotification(it.title, body)
            }
        }
    }

    /**
     * Subscribe ไปยัง topics ที่ต้องการ
     */
    private fun subscribeToTopics() {
        Log.d(TAG, "===== Subscribing to FCM topics =====")

        // Subscribe ไปยัง topic ทั่วไป
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Successfully subscribed to topic: all")
                } else {
                    Log.e(TAG, "Failed to subscribe to topic: all", task.exception)
                }
            }

        // ถ้าผู้ใช้ลงชื่อเข้าใช้แล้ว ให้ subscribe ไปยัง topic เฉพาะของผู้ใช้นี้
        val userSessionManager = UserSessionManager(applicationContext)
        if (userSessionManager.isLoggedIn()) {
            val userId = userSessionManager.getUserIdFromToken()
            if (userId != null) {
                FirebaseMessaging.getInstance().subscribeToTopic("user_$userId")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Successfully subscribed to topic: user_$userId")
                        } else {
                            Log.e(TAG, "Failed to subscribe to topic: user_$userId", task.exception)
                        }
                    }
            }
        }

        // เพิ่ม log เพื่อเช็คสถานะ
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "===== FCM TOKEN: ${task.result} =====")
            } else {
                Log.e(TAG, "===== Failed to get FCM token =====", task.exception)
            }
        }
    }

    /**
     * ส่ง token ไปยังเซิร์ฟเวอร์
     */
    private fun sendRegistrationToServer(token: String) {
        val userSessionManager = UserSessionManager(applicationContext)
        val userId = userSessionManager.getUserIdFromToken() ?: "unknown_user"

        Log.d(TAG, "Sending FCM token to server: $token")

        // ใช้ OkHttp ส่ง token ไปยังเซิร์ฟเวอร์
        val client = OkHttpClient()

        // สร้าง JSON request body
        val jsonBody = """
            {
                "token": "$token",
                "userId": "$userId"
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$SERVER_URL/register-token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to send FCM token to server", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Server response: $responseBody")

                if (response.isSuccessful) {
                    Log.d(TAG, "FCM token sent to server successfully!")
                } else {
                    Log.e(TAG, "Failed to send FCM token: ${response.code}")
                }
            }
        })

        // เก็บไว้ถ้าต้องการใช้ coroutine เดิมในอนาคต
        /*
        // ใช้ coroutine เพื่อเรียก API ในเธรดพื้นหลัง
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ใช้ Repository ที่มีอยู่แล้ว
                val repository = com.example.pls.network.Repository()
                val authToken = userSessionManager.getToken()

                // ถ้า auth token และ userId ไม่เป็น null ให้ส่ง FCM token ไปยังเซิร์ฟเวอร์
                if (authToken != null && userId != null) {
                    // เราต้องเพิ่ม API endpoint นี้ใน ApiService และ Repository
                    // repository.registerFcmToken(authToken, userId, token)
                    Log.d(TAG, "FCM token sent to server successfully")
                } else {
                    Log.d(TAG, "Couldn't send FCM token: auth token or userId is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send FCM token to server", e)
            }
        }
        */
    }

    /**
     * สร้างและแสดงการแจ้งเตือน
     */
    private fun sendNotification(title: String?, messageBody: String) {
        Log.d(TAG, "Creating notification - Title: $title, Body: $messageBody")

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )

        val channelId = "wheelchair_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(title ?: "การแจ้งเตือน")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // เริ่มต้น notification channel สำหรับ Android O และใหม่กว่า
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "การแจ้งเตือนรถวีลแชร์",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "การแจ้งเตือนจากแอปรถวีลแชร์"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // ใช้ timestamp เป็น notification ID เพื่อให้แสดงการแจ้งเตือนหลายรายการได้
        val notificationId = System.currentTimeMillis().toInt()

        // แสดงการแจ้งเตือน
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notification displayed with ID: $notificationId")
    }

    /**
     * สำหรับทดสอบการแจ้งเตือนภายนอกคลาส
     */
    fun sendNotificationTest(context: Context, title: String, message: String) {
        Log.d(TAG, "Test notification - Title: $title, Body: $message")

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )

        val channelId = "wheelchair_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // เริ่มต้น notification channel สำหรับ Android O และใหม่กว่า
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "การแจ้งเตือนรถวีลแชร์",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "การแจ้งเตือนจากแอปรถวีลแชร์"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // แสดงการแจ้งเตือน
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Test notification displayed with ID: $notificationId")
    }
}