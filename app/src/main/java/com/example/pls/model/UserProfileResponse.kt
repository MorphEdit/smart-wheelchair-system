package com.example.pls.model

// คลาสสำหรับการตอบกลับข้อมูลผู้ใช้จาก API /api/auth/me
data class UserProfileResponse(
    val user: UserResponse?, // ข้อมูลผู้ใช้
    val success: Boolean,    // สถานะการทำงาน
    val error: String?       // ข้อความแจ้งข้อผิดพลาด (ถ้ามี)
)