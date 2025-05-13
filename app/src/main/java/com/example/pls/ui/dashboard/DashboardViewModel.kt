package com.example.pls.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pls.model.CommandResponse
import com.example.pls.network.Repository
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val repository = Repository()

    private val _commandResult = MutableLiveData<String>()
    val commandResult: LiveData<String> = _commandResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ส่งคำสั่งควบคุมรถวีลแชร์
    fun controlWheelchair(wheelchairId: String, command: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.controlWheelchair(wheelchairId, command)
                result.onSuccess { response ->
                    _commandResult.value = response.message
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