package com.example.pls.model

import java.util.Date

data class NotificationItem(
    val id: String,
    val title: String,
    val content: String,
    val type: String,
    val timestamp: String,
    val isRead: Boolean,
    val relatedEntityId: String? = null,
    val relatedEntityType: String? = null,
    val tag: String? = null
) {
    // เมธอดสำหรับแปลง timestamp เป็นรูปแบบที่แสดงผลได้ง่ายขึ้น
    fun getFormattedTime(): String {
        // นี่เป็นตัวอย่างง่ายๆ ควรมีการแปลงเวลาที่ซับซ้อนกว่านี้ในโปรดักชั่น
        return try {
            val date = Date(timestamp.toLong())
            // ตรวจสอบว่าเป็นวันนี้ เมื่อวาน หรือวันอื่นๆ และแสดงผลตามเงื่อนไข
            // นี่เป็นเพียงตัวอย่าง ควรใช้ DateFormatter จริงๆ
            "เมื่อ " + when {
                // ภายใน 1 ชั่วโมง
                System.currentTimeMillis() - date.time < 60 * 60 * 1000 -> {
                    val minutes = (System.currentTimeMillis() - date.time) / (60 * 1000)
                    "$minutes นาทีที่แล้ว"
                }
                // ภายในวันนี้
                System.currentTimeMillis() - date.time < 24 * 60 * 60 * 1000 -> {
                    val hours = (System.currentTimeMillis() - date.time) / (60 * 60 * 1000)
                    "$hours ชั่วโมงที่แล้ว"
                }
                // เมื่อวาน
                System.currentTimeMillis() - date.time < 48 * 60 * 60 * 1000 -> {
                    "เมื่อวาน"
                }
                // วันอื่นๆ
                else -> {
                    val days = (System.currentTimeMillis() - date.time) / (24 * 60 * 60 * 1000)
                    "$days วันที่แล้ว"
                }
            }
        } catch (e: Exception) {
            "ไม่ทราบเวลา"
        }
    }

    // เมธอดสำหรับดึงวันที่ในรูปแบบที่อ่านง่าย
    fun getFormattedDate(): String {
        // ควรใช้ DateFormatter จริงๆ แต่นี่เป็นตัวอย่างง่ายๆ
        return try {
            // แปลง timestamp เป็นวันที่รูปแบบ "dd/MM/yyyy"
            val date = Date(timestamp.toLong())
            val day = date.date
            val month = date.month + 1
            val year = date.year + 1900
            "$day/$month/$year"
        } catch (e: Exception) {
            "ไม่ทราบวันที่"
        }
    }

    // เมธอดสำหรับดึงไอคอนตามประเภทการแจ้งเตือน
    fun getNotificationIcon(): Int {
        return when (type) {
            "wheelchair-status" -> com.example.pls.R.drawable.ic_wheelchair
            "pairing-added" -> com.example.pls.R.drawable.ic_link
            "pairing-deleted" -> com.example.pls.R.drawable.ic_link
            "wheelchair-added" -> com.example.pls.R.drawable.ic_add
            "wheelchair-deleted" -> com.example.pls.R.drawable.ic_clear_all
            "payment" -> com.example.pls.R.drawable.ic_payment
            else -> com.example.pls.R.drawable.ic_notifications
        }
    }

    // เมธอดสำหรับตรวจสอบว่าควรแสดงแท็กหรือไม่
    fun shouldShowTag(): Boolean {
        return !tag.isNullOrEmpty()
    }
}

data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<NotificationItem>,
    val error: String? = null
)