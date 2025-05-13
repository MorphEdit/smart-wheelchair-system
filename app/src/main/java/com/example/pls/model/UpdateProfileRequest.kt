package com.example.pls.model

/**
 * คลาสสำหรับการอัปเดตข้อมูลโปรไฟล์ผู้ใช้
 */
data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val phone: String?
)