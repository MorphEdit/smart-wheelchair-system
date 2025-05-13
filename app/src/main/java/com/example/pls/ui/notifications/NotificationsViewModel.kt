package com.example.pls.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pls.model.NotificationItem
import com.example.pls.network.Repository
import com.example.pls.utils.UserSessionManager
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository()
    private val userSessionManager = UserSessionManager(application)
    private val TAG = "NotificationsViewModel"

    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // เริ่มดึงข้อมูลเมื่อเปิดหน้า
    init {
        loadNotifications()
    }

    // ดึงข้อมูลการแจ้งเตือนจากเซิร์ฟเวอร์
    fun loadNotifications() {
        val token = userSessionManager.getToken() ?: return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = repository.getNotifications(token)
                result.onSuccess { notificationsList ->
                    _notifications.value = notificationsList
                }.onFailure { exception ->
                    _error.value = exception.message ?: "ไม่สามารถโหลดการแจ้งเตือนได้"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "เกิดข้อผิดพลาดในการโหลดการแจ้งเตือน"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ทำเครื่องหมายว่าการแจ้งเตือนได้อ่านแล้ว
    fun markAsRead(notificationId: String) {
        val token = userSessionManager.getToken() ?: return

        viewModelScope.launch {
            try {
                val result = repository.markNotificationAsRead(token, notificationId)
                result.onSuccess {
                    // อัปเดตรายการการแจ้งเตือนในแอป
                    val currentList = _notifications.value?.toMutableList() ?: mutableListOf()
                    val index = currentList.indexOfFirst { it.id == notificationId }

                    if (index != -1) {
                        // สร้างอ็อบเจกต์ใหม่ที่ทำเครื่องหมายว่าอ่านแล้ว
                        val updatedItem = currentList[index].copy(isRead = true)
                        currentList[index] = updatedItem
                        _notifications.value = currentList
                    }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "ไม่สามารถทำเครื่องหมายว่าอ่านแล้ว"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "เกิดข้อผิดพลาด"
            }
        }
    }

    // ลบการแจ้งเตือน
    fun deleteNotification(notificationId: String) {
        val token = userSessionManager.getToken() ?: return

        viewModelScope.launch {
            try {
                val result = repository.deleteNotification(token, notificationId)
                result.onSuccess {
                    // ลบออกจากรายการแจ้งเตือนในแอป
                    val currentList = _notifications.value?.toMutableList() ?: mutableListOf()
                    val filteredList = currentList.filter { it.id != notificationId }
                    _notifications.value = filteredList
                }.onFailure { exception ->
                    _error.value = exception.message ?: "ไม่สามารถลบการแจ้งเตือน"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "เกิดข้อผิดพลาด"
            }
        }
    }

    // ล้างการแจ้งเตือนทั้งหมด (อัปเดตเฉพาะ UI)
    fun clearAllNotifications() {
        _notifications.value = emptyList()
        // หมายเหตุ: ในระบบจริงควรมีการเรียก API ไปยังเซิร์ฟเวอร์เพื่อทำเครื่องหมายหรือลบการแจ้งเตือนทั้งหมด
    }

    // ล้างข้อความแจ้งข้อผิดพลาด
    fun clearError() {
        _error.value = null
    }
}