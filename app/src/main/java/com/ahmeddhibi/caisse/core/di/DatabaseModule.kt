package com.ahmeddhibi.caisse.core.di

import android.content.Context
import com.ahmeddhibi.caisse.data.local.CaisseDatabase
import com.ahmeddhibi.caisse.data.local.dao.RegisterDao
import com.ahmeddhibi.caisse.data.local.dao.SaleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CaisseDatabase = CaisseDatabase.create(context)

    @Provides
    fun provideSaleDao(database: CaisseDatabase): SaleDao = database.saleDao()

    @Provides
    fun provideRegisterDao(database: CaisseDatabase): RegisterDao = database.registerDao()
}
