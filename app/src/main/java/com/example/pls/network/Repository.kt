package com.example.pls.network

import android.util.Log
import com.example.pls.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository {
    private val apiService = RetrofitClient.apiService

    // ดึงข้อมูลรถวีลแชร์ทั้งหมด
    suspend fun getWheelchairs(): Result<List<WheelchairResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getWheelchairs()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to fetch wheelchairs: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ดึงข้อมูลผู้ใช้ทั้งหมด
    suspend fun getUsers(): Result<List<UserResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUsers()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to fetch users: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ดึงข้อมูลการจับคู่ทั้งหมด
    suspend fun getPairings(): Result<List<PairingResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPairings()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to fetch pairings: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // จับคู่ด้วยรหัส
    suspend fun pairWithCode(userId: String, pairingCode: String): Result<PairWithCodeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = PairWithCodeRequest(userId, pairingCode)
                val response = apiService.pairWithCode(request)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: PairWithCodeResponse(false, null, "ไม่พบข้อมูลตอบกลับ"))
                } else {
                    Result.failure(Exception("Failed to pair with code: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ส่งคำสั่งควบคุมรถวีลแชร์
    suspend fun controlWheelchair(wheelchairId: String, command: String): Result<CommandResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.controlWheelchair(WheelchairCommand(wheelchairId, command))
                if (response.isSuccessful) {
                    Result.success(response.body() ?: CommandResponse(false, "No response body"))
                } else {
                    Result.failure(Exception("Failed to control wheelchair: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ยกเลิกการจับคู่
    suspend fun unpairWheelchair(pairingId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deletePairing(pairingId)
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to unpair wheelchair: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ลงทะเบียนผู้ใช้ใหม่
    suspend fun register(name: String, email: String, phone: String?, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val registerRequest = RegisterRequest(name, email, phone, password)
                val response = apiService.register(registerRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Registration failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // เข้าสู่ระบบ
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = apiService.login(loginRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Login failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ดึงข้อมูลผู้ใช้ปัจจุบัน
    suspend fun getUserProfile(token: String): Result<UserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val authToken = "Bearer $token"
                val response = apiService.getCurrentUser(authToken)

                if (response.isSuccessful && response.body()?.success == true) {
                    val userData = response.body()?.user
                    if (userData != null) {
                        Result.success(userData)
                    } else {
                        Result.failure(Exception("ไม่พบข้อมูลผู้ใช้"))
                    }
                } else {
                    val errorMessage = response.body()?.error ?: "ไม่สามารถดึงข้อมูลผู้ใช้ได้: ${response.code()}"
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // อัปเดตโปรไฟล์ผู้ใช้
    suspend fun updateProfile(token: String, name: String, email: String, phone: String?): Result<UserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("Repository", "Updating profile - token: $token, name: $name, email: $email, phone: $phone")
                val authToken = "Bearer $token"
                val request = UpdateProfileRequest(name, email, phone)
                Log.d("Repository", "Sending request to API: $request")

                val response = apiService.updateProfile(authToken, request)
                Log.d("Repository", "API response: ${response.isSuccessful}, code: ${response.code()}, body: ${response.body()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val userData = response.body()?.user
                    if (userData != null) {
                        Result.success(userData)
                    } else {
                        Result.failure(Exception("ไม่พบข้อมูลผู้ใช้ที่อัปเดต"))
                    }
                } else {
                    val errorMessage = response.body()?.error ?: "ไม่สามารถอัปเดตข้อมูลผู้ใช้ได้: ${response.code()}"
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e("Repository", "Exception in updateProfile", e)
                Result.failure(e)
            }
        }
    }

    // ลงทะเบียน FCM token
    suspend fun registerFcmToken(userId: String, fcmToken: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterTokenRequest(userId, fcmToken)
                val response = apiService.registerDeviceToken(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("ไม่สามารถลงทะเบียน FCM token ได้: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("Repository", "Failed to register FCM token", e)
                Result.failure(e)
            }
        }
    }
}