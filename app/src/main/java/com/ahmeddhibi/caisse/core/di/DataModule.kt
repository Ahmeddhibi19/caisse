package com.ahmeddhibi.caisse.core.di

import com.ahmeddhibi.caisse.data.cart.InMemoryCartRepository
import com.ahmeddhibi.caisse.data.catalog.HardcodedProductRepository
import com.ahmeddhibi.caisse.data.remote.FirebaseRegisterDataSource
import com.ahmeddhibi.caisse.data.remote.RegisterRemoteDataSource
import com.ahmeddhibi.caisse.data.repository.DefaultRegisterRepository
import com.ahmeddhibi.caisse.data.repository.OfflineFirstSaleRepository
import com.ahmeddhibi.caisse.domain.repository.CartRepository
import com.ahmeddhibi.caisse.domain.repository.ProductRepository
import com.ahmeddhibi.caisse.domain.repository.RegisterRepository
import com.ahmeddhibi.caisse.domain.repository.SaleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindProductRepository(impl: HardcodedProductRepository): ProductRepository

    @Binds
    abstract fun bindCartRepository(impl: InMemoryCartRepository): CartRepository

    @Binds
    abstract fun bindRegisterRepository(impl: DefaultRegisterRepository): RegisterRepository

    @Binds
    abstract fun bindSaleRepository(impl: OfflineFirstSaleRepository): SaleRepository

    @Binds
    abstract fun bindRegisterRemoteDataSource(impl: FirebaseRegisterDataSource): RegisterRemoteDataSource
}
