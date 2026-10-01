package com.ahmeddhibi.caisse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmeddhibi.caisse.domain.model.Register
import com.ahmeddhibi.caisse.domain.repository.RegisterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    registerRepository: RegisterRepository,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = registerRepository.register
        .map { register -> if (register == null) MainUiState.NotEnrolled else MainUiState.Ready(register) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data object NotEnrolled : MainUiState
    data class Ready(val register: Register) : MainUiState
}
