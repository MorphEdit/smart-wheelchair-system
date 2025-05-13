package com.example.pls.model

data class UserResponse(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?
)

// เพิ่มคลาสสำหรับการลงทะเบียน
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String?,
    val password: String
)

// เพิ่มคลาสสำหรับการเข้าสู่ระบบ
data class LoginRequest(
    val email: String,
    val password: String
)

// เพิ่มคลาสสำหรับการตอบกลับการลงทะเบียน/เข้าสู่ระบบ
data class AuthResponse(
    val user: UserResponse,
    val token: String
)

// คลาสสำหรับจัดเก็บข้อมูลผู้ใช้ในแอป
data class UserSession(
    val user: UserResponse,
    val token: String,
    val isLoggedIn: Boolean = true
)