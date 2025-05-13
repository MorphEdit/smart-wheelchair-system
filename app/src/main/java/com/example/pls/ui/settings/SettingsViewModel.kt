package com.example.pls.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// ข้อมูลรายการตั้งค่า
data class SettingItem(
    val title: String,
    val description: String,
    val id: String // เพิ่ม id เพื่อระบุรายการ
)

class SettingsViewModel : ViewModel() {

    // รายการตั้งค่า
    private val _settings = MutableLiveData<List<SettingItem>>().apply {
        value = generateSettingItems()
    }
    val settings: LiveData<List<SettingItem>> = _settings

    // สร้างข้อมูลตัวอย่าง
    private fun generateSettingItems(): List<SettingItem> {
        return listOf(
            SettingItem(
                "จับคู่รถวีลแชร์",
                "จับคู่กับรถวีลแชร์ด้วยรหัส 6 หลัก",
                "pair_wheelchair"
            ),
            SettingItem(
                "การแจ้งเตือน",
                "จัดการการแจ้งเตือนแอปพลิเคชัน",
                "notifications"
            ),
            SettingItem(
                "ความเป็นส่วนตัว",
                "จัดการการตั้งค่าความเป็นส่วนตัวและการมองเห็นข้อมูล",
                "privacy"
            ),
            SettingItem(
                "ภาษา",
                "เปลี่ยนภาษาที่ใช้ในแอปพลิเคชัน",
                "language"
            ),
            SettingItem(
                "ธีม",
                "เปลี่ยนธีมและรูปลักษณ์ของแอปพลิเคชัน",
                "theme"
            )
        )
    }
}