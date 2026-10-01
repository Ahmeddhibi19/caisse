package com.ahmeddhibi.caisse.ui

import com.ahmeddhibi.caisse.domain.model.Register
import com.ahmeddhibi.caisse.testing.FakeRegisterRepository
import com.ahmeddhibi.caisse.testing.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `asks for enrolment until the device is a register`() = runTest {
        val registers = FakeRegisterRepository(initial = null)
        val viewModel = MainViewModel(registers)
        backgroundScope.launch(mainDispatcherRule.dispatcher) { viewModel.uiState.collect() }

        assertThat(viewModel.uiState.value).isEqualTo(MainUiState.NotEnrolled)

        registers.enroll()

        assertThat(viewModel.uiState.value).isEqualTo(MainUiState.Ready(Register(storeId = "test-store", number = 1)))
    }
}
