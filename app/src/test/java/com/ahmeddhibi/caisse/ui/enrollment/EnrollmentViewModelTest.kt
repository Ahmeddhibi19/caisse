package com.ahmeddhibi.caisse.ui.enrollment

import com.ahmeddhibi.caisse.testing.FakeNetworkMonitor
import com.ahmeddhibi.caisse.testing.FakeRegisterRepository
import com.ahmeddhibi.caisse.testing.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EnrollmentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val network = FakeNetworkMonitor(initiallyOnline = false)
    private val registers = FakeRegisterRepository(initial = null)

    @Test
    fun `waits for the network before enrolling`() = runTest {
        val viewModel = EnrollmentViewModel(registers, network)

        assertThat(viewModel.uiState.value).isEqualTo(EnrollmentUiState.WaitingForNetwork)
        assertThat(registers.enrollCalls).isEqualTo(0)
    }

    @Test
    fun `enrols as soon as the network comes back`() = runTest {
        val viewModel = EnrollmentViewModel(registers, network)

        network.online.value = true

        assertThat(registers.enrollCalls).isEqualTo(1)
        assertThat(viewModel.uiState.value).isEqualTo(EnrollmentUiState.Enrolling)
    }

    @Test
    fun `a server error while online offers a retry`() = runTest {
        registers.failure = IllegalStateException("server error")
        val viewModel = EnrollmentViewModel(registers, network)

        network.online.value = true
        assertThat(viewModel.uiState.value).isEqualTo(EnrollmentUiState.Failed)

        registers.failure = null
        viewModel.onRetry()
        assertThat(registers.enrollCalls).isEqualTo(2)
    }

    @Test
    fun `losing the network after a failure waits again and retries automatically`() = runTest {
        registers.failure = IllegalStateException("server error")
        val viewModel = EnrollmentViewModel(registers, network)
        network.online.value = true

        network.online.value = false
        assertThat(viewModel.uiState.value).isEqualTo(EnrollmentUiState.WaitingForNetwork)

        registers.failure = null
        network.online.value = true
        assertThat(registers.enrollCalls).isEqualTo(2)
    }
}
