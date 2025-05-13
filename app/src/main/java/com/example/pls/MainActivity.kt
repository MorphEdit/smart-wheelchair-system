package com.example.pls

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.pls.databinding.ActivityMainBinding
import com.example.pls.utils.UserSessionManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userSessionManager: UserSessionManager
    private val TAG = "MainActivity"

    // URL ของเซิร์ฟเวอร์ Node.js
    private val SERVER_URL = "http://124.121.176.140:3000" // สำหรับ Emulator
    // หากใช้อุปกรณ์จริง ให้เปลี่ยนเป็น "http://192.168.x.x:3000" (IP ของเครื่องที่รันเซิร์ฟเวอร์)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // สร้าง UserSessionManager
        userSessionManager = UserSessionManager(this)

        // เช็คการเชื่อมต่อกับ Firebase
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase Apps: ${FirebaseApp.getApps(this).size}")
        Log.d(TAG, "Firebase instance: ${FirebaseApp.getInstance().name}")

        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // ตรวจสอบว่าผู้ใช้เข้าสู่ระบบหรือยัง
        if (!userSessionManager.isLoggedIn()) {
            navController.navigate(R.id.loginFragment)
        }

        // ขอสิทธิ์การแจ้งเตือนสำหรับ Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // ดึง FCM token เมื่อแอปเริ่มทำงาน
        retrieveAndStoreFcmToken()

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_account
            )
        )

        navView.setupWithNavController(navController)

        // ซ่อน/แสดง Bottom Navigation ตามหน้าที่อยู่
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    navView.visibility = View.GONE
                }
                else -> {
                    navView.visibility = View.VISIBLE
                }
            }
        }

        // ทดสอบการแจ้งเตือนบนอุปกรณ์เมื่อเปิดแอพ
        Handler(Looper.getMainLooper()).postDelayed({
            testLocalNotification()
        }, 3000) // ทดสอบหลังจากเปิดแอพ 3 วินาที
    }

    // ทดสอบการแจ้งเตือนภายในเครื่อง
    private fun testLocalNotification() {
        Log.d(TAG, "Testing local notification")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // สร้าง channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "test_channel"
            val channel = NotificationChannel(
                channelId,
                "ทดสอบการแจ้งเตือน",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ทดสอบการแจ้งเตือนจากแอป"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // สร้าง intent
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

        // สร้าง notification
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "test_channel" else ""
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("ทดสอบการแจ้งเตือน")
            .setContentText("หากคุณเห็นข้อความนี้ แสดงว่าการแจ้งเตือนทำงานได้")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // แสดง notification
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, builder.build())
        Log.d(TAG, "Local notification sent with ID: $notificationId")
    }

    // ขอสิทธิ์การแจ้งเตือน
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    // จัดการผลลัพธ์จากการขอสิทธิ์
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted")
                // ลงทะเบียน token อีกครั้งหลังได้รับอนุญาต
                retrieveAndStoreFcmToken()
            } else {
                Log.d(TAG, "Notification permission denied")
                // อาจแสดงข้อความบอกผู้ใช้ว่าไม่สามารถแสดงการแจ้งเตือนได้
            }
        }
    }

    // ดึงและเก็บ FCM token
    private fun retrieveAndStoreFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // รับ token
            val token = task.result

            // บันทึก token ลงในเครื่อง
            userSessionManager.saveFcmToken(token)

            // ส่ง token ไปยังเซิร์ฟเวอร์ หากผู้ใช้ล็อกอินอยู่
            sendTokenToServer(token)

            Log.d(TAG, "FCM Token: $token")
        }
    }

    // ส่ง token ไปยังเซิร์ฟเวอร์
    private fun sendTokenToServer(token: String) {
        // ดึง user ID จาก session (ถ้ามี)
        val userId = userSessionManager.getUserIdFromToken() ?: "unknown_user"

        Log.d(TAG, "Sending FCM token to server: $token")

        // ใช้ OkHttp ส่ง token ไปยังเซิร์ฟเวอร์
        val client = OkHttpClient()

        // สร้าง JSON request body
        val json = """
            {
                "token": "$token",
                "userId": "$userId"
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$SERVER_URL/register-token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to send FCM token to server", e)

                // ลองส่งอีกครั้งใน 5 วินาที
                Handler(Looper.getMainLooper()).postDelayed({
                    sendTokenToServer(token)
                }, 5000)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Server response: $responseBody")

                if (response.isSuccessful) {
                    Log.d(TAG, "FCM token sent to server successfully!")
                } else {
                    Log.e(TAG, "Failed to send FCM token: ${response.code}")

                    // ลองส่งอีกครั้งใน 5 วินาที
                    Handler(Looper.getMainLooper()).postDelayed({
                        sendTokenToServer(token)
                    }, 5000)
                }
            }
        })
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 123
    }
}