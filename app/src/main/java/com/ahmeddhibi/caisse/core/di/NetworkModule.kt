package com.ahmeddhibi.caisse.core.di

import com.ahmeddhibi.caisse.core.network.ConnectivityManagerNetworkMonitor
import com.ahmeddhibi.caisse.core.network.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    abstract fun bindNetworkMonitor(impl: ConnectivityManagerNetworkMonitor): NetworkMonitor
}
