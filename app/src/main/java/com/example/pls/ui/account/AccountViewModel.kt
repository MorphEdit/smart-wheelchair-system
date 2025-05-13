package com.example.pls.ui.account

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pls.model.UserResponse
import com.example.pls.model.UserSession
import com.example.pls.network.Repository
import com.example.pls.utils.UserSessionManager
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository()
    private val userSessionManager = UserSessionManager(application)
    private val TAG = "AccountViewModel"

    private val _name = MutableLiveData<String>().apply {
        value = userSessionManager.getUser()?.name ?: "ชื่อผู้ใช้"
    }
    val name: LiveData<String> = _name

    private val _email = MutableLiveData<String>().apply {
        value = userSessionManager.getUser()?.email ?: "ไม่ระบุอีเมล"
    }
    val email: LiveData<String> = _email

    private val _phone = MutableLiveData<String>().apply {
        // แก้ไขตรงนี้: ตรวจสอบเบอร์โทรศัพท์และกำหนดค่าที่เหมาะสม
        value = if (!userSessionManager.getUser()?.phone.isNullOrEmpty()) {
            userSessionManager.getUser()?.phone
        } else {
            "กรุณาเพิ่มเบอร์โทรศัพท์"
        }
    }
    val phone: LiveData<String> = _phone

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadUserData()
    }

    /**
     * โหลดข้อมูลผู้ใช้จาก UserSessionManager และ API
     */
    fun loadUserData() {
        // โหลดข้อมูลจาก local storage ก่อน
        val user = userSessionManager.getUser()
        user?.let {
            _name.value = it.name
            _email.value = it.email ?: "ไม่ระบุอีเมล"
            _phone.value = if (!it.phone.isNullOrEmpty()) it.phone else "กรุณาเพิ่มเบอร์โทรศัพท์"
        }

        // จากนั้นโหลดข้อมูลล่าสุดจาก API
        refreshUserData()
    }

    /**
     * ดึงข้อมูลผู้ใช้ล่าสุดจาก API
     */
    fun refreshUserData() {
        // ใช้ข้อมูลจาก local storage เท่านั้น (ข้ามการเรียก API)
        val user = userSessionManager.getUser()
        user?.let {
            _name.value = it.name
            _email.value = it.email ?: "ไม่ระบุอีเมล"
            _phone.value = if (!it.phone.isNullOrEmpty()) it.phone else "กรุณาเพิ่มเบอร์โทรศัพท์"
        }

        // โค้ดเดิม (ปิดการใช้งานชั่วคราว)
        /*
        val token = userSessionManager.getToken()
        if (token != null) {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    Log.d(TAG, "กำลังดึงข้อมูลผู้ใช้ด้วย token: $token")
                    val result = repository.getUserProfile(token)

                    if (result.isSuccess) {
                        val userData = result.getOrNull()
                        userData?.let {
                            Log.d(TAG, "ดึงข้อมูลผู้ใช้สำเร็จ: ${it.name}")
                            updateUserData(it)
                        }
                    } else {
                        val error = result.exceptionOrNull()
                        Log.e(TAG, "ไม่สามารถดึงข้อมูลผู้ใช้: ${error?.message}")
                        _errorMessage.value = "ไม่สามารถโหลดข้อมูลผู้ใช้: ${error?.message ?: "ไม่ทราบสาเหตุ"}"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "เกิดข้อผิดพลาดในการดึงข้อมูลผู้ใช้", e)
                    _errorMessage.value = "เกิดข้อผิดพลาด: ${e.message ?: "ไม่ทราบสาเหตุ"}"
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            Log.e(TAG, "ไม่พบ token สำหรับการเข้าถึงข้อมูล")
            _errorMessage.value = "ไม่พบ token สำหรับการเข้าถึงข้อมูล กรุณาเข้าสู่ระบบใหม่"
        }
        */
    }

    /**
     * อัปเดตข้อมูลผู้ใช้ใน ViewModel
     */
    private fun updateUserData(user: UserResponse) {
        _name.value = user.name
        _email.value = user.email ?: "ไม่ระบุอีเมล"
        _phone.value = if (!user.phone.isNullOrEmpty()) user.phone else "กรุณาเพิ่มเบอร์โทรศัพท์"
    }

    /**
     * อัปเดต UserSession ในเครื่อง
     */
    private fun updateLocalUserSession(user: UserResponse, token: String) {
        val userSession = UserSession(user, token)
        userSessionManager.saveUserSession(userSession)
    }

    /**
     * ล้างข้อความแจ้งเตือนข้อผิดพลาด
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * อัปเดตโปรไฟล์ผู้ใช้
     */
    fun updateProfile(name: String, email: String, phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val token = userSessionManager.getToken()

        if (token == null) {
            onError("ไม่พบข้อมูลผู้ใช้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // เรียกใช้ API อัปเดตโปรไฟล์
                val result = repository.updateProfile(token, name, email, phone)

                if (result.isSuccess) {
                    // อัปเดตข้อมูลสำเร็จ
                    val userData = result.getOrNull()
                    if (userData != null) {
                        Log.d(TAG, "อัปเดตโปรไฟล์สำเร็จ: ${userData.name}")

                        // อัปเดตข้อมูลใน ViewModel
                        updateUserData(userData)

                        // อัปเดตข้อมูลใน local storage
                        updateLocalUserSession(userData, token)

                        // เรียก callback แจ้งว่าสำเร็จ
                        onSuccess()
                    } else {
                        Log.e(TAG, "อัปเดตโปรไฟล์สำเร็จแต่ไม่ได้รับข้อมูลผู้ใช้กลับมา")
                        onError("อัปเดตโปรไฟล์สำเร็จแต่ไม่ได้รับข้อมูลผู้ใช้กลับมา")
                    }
                } else {
                    // อัปเดตข้อมูลไม่สำเร็จ
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "ไม่สามารถอัปเดตโปรไฟล์: ${error?.message}")
                    onError("ไม่สามารถอัปเดตโปรไฟล์: ${error?.message ?: "ไม่ทราบสาเหตุ"}")
                }
            } catch (e: Exception) {
                // เกิดข้อผิดพลาด
                Log.e(TAG, "เกิดข้อผิดพลาดในการอัปเดตโปรไฟล์", e)
                onError("เกิดข้อผิดพลาดในการอัปเดตโปรไฟล์: ${e.message ?: "ไม่ทราบสาเหตุ"}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}