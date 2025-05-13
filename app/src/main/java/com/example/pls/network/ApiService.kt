package com.example.pls.network

import com.example.pls.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    companion object {
        const val BASE_URL = "http://124.121.176.140:3000/api/"
    }

    // รถวีลแชร์
    @GET("wheelchairs")
    suspend fun getWheelchairs(): Response<List<WheelchairResponse>>

    @GET("wheelchairs/{id}")
    suspend fun getWheelchair(@Path("id") id: String): Response<WheelchairResponse>

    // ผู้ใช้งาน
    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>

    // การจับคู่
    @GET("pairings")
    suspend fun getPairings(): Response<List<PairingResponse>>

    // จับคู่ด้วยรหัส
    @POST("pair-with-code")
    suspend fun pairWithCode(@Body request: PairWithCodeRequest): Response<PairWithCodeResponse>

    // ส่งคำสั่งควบคุมรถวีลแชร์
    @POST("wheelchair/control")
    suspend fun controlWheelchair(@Body command: WheelchairCommand): Response<CommandResponse>

    // ยกเลิกการจับคู่
    @DELETE("pairings/{id}")
    suspend fun deletePairing(@Path("id") id: String): Response<Map<String, Any>>

    // ระบบลงทะเบียนและเข้าสู่ระบบ
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserProfileResponse>

    // เพิ่ม API อัปเดตโปรไฟล์
    @PUT("auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserProfileResponse>

    // ลงทะเบียน FCM token
    @POST("register-device-token")
    suspend fun registerDeviceToken(@Body request: RegisterTokenRequest): Response<RegisterTokenResponse>

    // เพิ่ม API ดึงข้อมูลการแจ้งเตือน
    @GET("notifications")
    suspend fun getNotifications(@Header("Authorization") token: String): Response<NotificationsResponse>

    // เพิ่ม API อ่านการแจ้งเตือน
    @POST("notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Header("Authorization") token: String,
        @Path("id") notificationId: String
    ): Response<Map<String, Any>>

    // เพิ่ม API ลบการแจ้งเตือน
    @DELETE("notifications/{id}")
    suspend fun deleteNotification(
        @Header("Authorization") token: String,
        @Path("id") notificationId: String
    ): Response<Map<String, Any>>
}