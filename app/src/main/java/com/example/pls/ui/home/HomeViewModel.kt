package com.example.pls.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pls.model.WheelchairResponse
import com.example.pls.network.Repository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = Repository()

    private val _wheelchairs = MutableLiveData<List<WheelchairResponse>>()
    val wheelchairs: LiveData<List<WheelchairResponse>> = _wheelchairs

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // โหลดข้อมูลรถวีลแชร์ทั้งหมด
    fun loadWheelchairs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.getWheelchairs()
                result.onSuccess {
                    _wheelchairs.value = it
                }.onFailure {
                    _error.value = it.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}