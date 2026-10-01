package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.core.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkMonitor(initiallyOnline: Boolean = true) : NetworkMonitor {

    val online = MutableStateFlow(initiallyOnline)

    override val isOnline: Flow<Boolean> = online
}
