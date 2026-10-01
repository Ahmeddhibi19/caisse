package com.ahmeddhibi.caisse.ui.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmeddhibi.caisse.core.network.NetworkMonitor
import com.ahmeddhibi.caisse.domain.repository.RegisterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
    private val registerRepository: RegisterRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EnrollmentUiState>(EnrollmentUiState.WaitingForNetwork)
    val uiState: StateFlow<EnrollmentUiState> = _uiState.asStateFlow()

    private var isOnline = false
    private var enrollJob: Job? = null

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                isOnline = online
                when {
                    online && _uiState.value == EnrollmentUiState.WaitingForNetwork -> enroll()
                    !online && _uiState.value == EnrollmentUiState.Failed ->
                        _uiState.value = EnrollmentUiState.WaitingForNetwork
                }
            }
        }
    }

    fun onRetry() = enroll()

    private fun enroll() {
        if (enrollJob?.isActive == true) return
        enrollJob = viewModelScope.launch {
            _uiState.value = EnrollmentUiState.Enrolling
            try {
                registerRepository.enroll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = if (isOnline) EnrollmentUiState.Failed else EnrollmentUiState.WaitingForNetwork
            }
        }
    }
}

sealed interface EnrollmentUiState {
    data object WaitingForNetwork : EnrollmentUiState
    data object Enrolling : EnrollmentUiState
    data object Failed : EnrollmentUiState
}
