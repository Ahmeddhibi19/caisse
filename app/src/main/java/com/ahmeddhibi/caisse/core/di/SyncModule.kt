package com.ahmeddhibi.caisse.core.di

import com.ahmeddhibi.caisse.data.remote.FirebaseSalesDataSource
import com.ahmeddhibi.caisse.data.remote.RemoteSalesDataSource
import com.ahmeddhibi.caisse.data.sync.WorkManagerSyncScheduler
import com.ahmeddhibi.caisse.domain.sync.SyncScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    abstract fun bindSyncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler

    @Binds
    abstract fun bindRemoteSalesDataSource(impl: FirebaseSalesDataSource): RemoteSalesDataSource
}
