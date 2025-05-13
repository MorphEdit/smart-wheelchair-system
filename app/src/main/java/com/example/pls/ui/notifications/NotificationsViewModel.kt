package com.example.pls.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// ข้อมูลรายการแจ้งเตือน
data class NotificationItem(
    val title: String,
    val content: String,
    val date: String
)

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    // รายการแจ้งเตือน
    private val _notifications = MutableLiveData<List<NotificationItem>>().apply {
        value = generateDummyNotifications()
    }
    val notifications: LiveData<List<NotificationItem>> = _notifications

    // สร้างข้อมูลตัวอย่าง
    private fun generateDummyNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem(
                "การชำระเงินสำเร็จ",
                "การชำระเงินของคุณสำหรับรายการ #12345 เสร็จสมบูรณ์แล้ว",
                "10 นาทีที่แล้ว"
            ),
            NotificationItem(
                "ส่วนลดพิเศษ",
                "รับส่วนลด 20% สำหรับการสั่งซื้อครั้งต่อไปของคุณ ใช้โค้ด SPECIAL20",
                "2 ชั่วโมงที่แล้ว"
            ),
            NotificationItem(
                "คำสั่งซื้อกำลังจัดส่ง",
                "คำสั่งซื้อ #67890 ของคุณกำลังอยู่ระหว่างการจัดส่ง คาดว่าจะถึงภายในวันพรุ่งนี้",
                "1 วันที่แล้ว"
            ),
            NotificationItem(
                "แจ้งเตือนระบบ",
                "ระบบได้ทำการอัปเดตเป็นเวอร์ชั่นล่าสุดเรียบร้อยแล้ว",
                "3 วันที่แล้ว"
            )
        )
    }
}