package com.example.pls.ui.pairing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pls.model.PairingData
import com.example.pls.network.Repository
import kotlinx.coroutines.launch

class PairingViewModel : ViewModel() {

    private val repository = Repository()

    private val _pairingState = MutableLiveData<PairingState>()
    val pairingState: LiveData<PairingState> = _pairingState

    sealed class PairingState {
        object Loading : PairingState()
        data class Success(val pairingData: PairingData) : PairingState()
        data class Error(val message: String) : PairingState()
    }

    fun pairWithCode(userId: String, pairingCode: String) {
        _pairingState.value = PairingState.Loading

        viewModelScope.launch {
            val result = repository.pairWithCode(userId, pairingCode)

            result.fold(
                onSuccess = { response ->
                    if (response.success && response.pairing != null) {
                        _pairingState.value = PairingState.Success(response.pairing)
                    } else {
                        _pairingState.value = PairingState.Error(
                            response.error ?: "ไม่สามารถจับคู่ได้"
                        )
                    }
                },
                onFailure = { e ->
                    _pairingState.value = PairingState.Error(e.message ?: "เกิดข้อผิดพลาดที่ไม่ทราบสาเหตุ")
                }
            )
        }
    }
}