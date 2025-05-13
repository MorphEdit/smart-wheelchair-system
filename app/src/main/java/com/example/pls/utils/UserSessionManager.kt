package com.example.pls.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.pls.model.UserResponse
import com.example.pls.model.UserSession
import com.google.gson.Gson

class UserSessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_USER_SESSION = "user_session"
        private const val KEY_PAIRED_WHEELCHAIR = "paired_wheelchair"
        private const val KEY_PAIRED_WHEELCHAIR_ID = "paired_wheelchair_id"
        private const val KEY_PAIRING_ID = "pairing_id"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }

    // บันทึกข้อมูลผู้ใช้ลงในเครื่อง
    fun saveUserSession(userSession: UserSession) {
        val userJson = gson.toJson(userSession)
        editor.putString(KEY_USER_SESSION, userJson)
        editor.apply()
    }

    // ดึงข้อมูลผู้ใช้จากเครื่อง
    fun getUserSession(): UserSession? {
        val userJson = sharedPreferences.getString(KEY_USER_SESSION, null) ?: return null
        return try {
            gson.fromJson(userJson, UserSession::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ตรวจสอบว่าผู้ใช้เข้าสู่ระบบแล้วหรือไม่
    fun isLoggedIn(): Boolean {
        return getUserSession() != null
    }

    // ดึง token ของผู้ใช้
    fun getToken(): String? {
        return getUserSession()?.token
    }

    // ดึงข้อมูลผู้ใช้
    fun getUser(): UserResponse? {
        return getUserSession()?.user
    }

    // ดึง userId จาก token (สำคัญมาก! เพื่อให้ตรงกับรูปแบบที่ server ต้องการ)
    fun getUserIdFromToken(): String? {
        val token = getToken() ?: return null
        // รูปแบบของ token คือ "token_userId_timestamp" โดยที่ userId = "user_timestamp"
        val parts = token.split("_")
        // ถ้ามากกว่า 3 ส่วน มันคือ "token_user_timestamp_additionalTimestamp"
        return if (parts.size >= 3) {
            // สร้าง userId แบบถูกต้อง "user_timestamp"
            "${parts[1]}_${parts[2]}"
        } else {
            null
        }
    }

    // บันทึกข้อมูลรถวีลแชร์ที่จับคู่แล้ว
    fun savePairedWheelchair(wheelchairId: String, pairingId: String) {
        editor.putString(KEY_PAIRED_WHEELCHAIR_ID, wheelchairId)
        editor.putString(KEY_PAIRING_ID, pairingId)
        editor.putBoolean(KEY_PAIRED_WHEELCHAIR, true)
        editor.apply()
    }

    // ตรวจสอบว่ามีรถวีลแชร์ที่จับคู่แล้วหรือไม่
    fun hasPairedWheelchair(): Boolean {
        return sharedPreferences.getBoolean(KEY_PAIRED_WHEELCHAIR, false)
    }

    // ดึง ID ของรถวีลแชร์ที่จับคู่แล้ว
    fun getPairedWheelchairId(): String? {
        return sharedPreferences.getString(KEY_PAIRED_WHEELCHAIR_ID, null)
    }

    // ดึง ID ของการจับคู่
    fun getPairingId(): String? {
        return sharedPreferences.getString(KEY_PAIRING_ID, null)
    }

    // ลบข้อมูลรถวีลแชร์ที่จับคู่แล้ว
    fun clearPairedWheelchair() {
        editor.remove(KEY_PAIRED_WHEELCHAIR)
        editor.remove(KEY_PAIRED_WHEELCHAIR_ID)
        editor.remove(KEY_PAIRING_ID)
        editor.apply()
    }

    // บันทึก FCM token
    fun saveFcmToken(token: String) {
        editor.putString(KEY_FCM_TOKEN, token)
        editor.apply()
    }

    // ดึง FCM token
    fun getFcmToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }

    // ล้าง FCM token
    fun clearFcmToken() {
        editor.remove(KEY_FCM_TOKEN)
        editor.apply()
    }

    // ออกจากระบบ
    fun logout() {
        editor.remove(KEY_USER_SESSION)
        clearPairedWheelchair()
        clearFcmToken()
        editor.apply()
    }
}