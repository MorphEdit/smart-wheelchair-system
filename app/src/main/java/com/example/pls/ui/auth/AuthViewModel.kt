package com.example.pls.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pls.model.UserSession
import com.example.pls.network.Repository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = Repository()

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _registerResult = MutableLiveData<AuthResult>()
    val registerResult: LiveData<AuthResult> = _registerResult

    // เข้าสู่ระบบ
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = AuthResult.Loading

            try {
                val result = repository.login(email, password)
                result.onSuccess { authResponse ->
                    // สร้าง UserSession จากข้อมูลที่ได้รับ
                    val userSession = UserSession(
                        user = authResponse.user,
                        token = authResponse.token
                    )
                    _loginResult.value = AuthResult.Success(userSession)
                }.onFailure { exception ->
                    _loginResult.value = AuthResult.Error(exception.message ?: "เข้าสู่ระบบไม่สำเร็จ")
                }
            } catch (e: Exception) {
                _loginResult.value = AuthResult.Error(e.message ?: "เกิดข้อผิดพลาดที่ไม่ทราบสาเหตุ")
            }
        }
    }

    // ลงทะเบียน
    fun register(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            _registerResult.value = AuthResult.Loading

            try {
                val result = repository.register(name, email, phone, password)
                result.onSuccess { authResponse ->
                    // สร้าง UserSession จากข้อมูลที่ได้รับ
                    val userSession = UserSession(
                        user = authResponse.user,
                        token = authResponse.token
                    )
                    _registerResult.value = AuthResult.Success(userSession)
                }.onFailure { exception ->
                    _registerResult.value = AuthResult.Error(exception.message ?: "ลงทะเบียนไม่สำเร็จ")
                }
            } catch (e: Exception) {
                _registerResult.value = AuthResult.Error(e.message ?: "เกิดข้อผิดพลาดที่ไม่ทราบสาเหตุ")
            }
        }
    }

    // รีเซ็ตผลลัพธ์
    fun resetLoginResult() {
        _loginResult.value = null
    }

    fun resetRegisterResult() {
        _registerResult.value = null
    }

    // คลาสสำหรับผลลัพธ์การยืนยันตัวตน
    sealed class AuthResult {
        data class Success(val userSession: UserSession) : AuthResult()
        data class Error(val message: String) : AuthResult()
        object Loading : AuthResult()
    }
}