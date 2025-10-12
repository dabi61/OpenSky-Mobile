package com.dabi.opensky.core.data.di

import com.dabi.opensky.core.data.repository.billing.BillingRepository
import com.dabi.opensky.core.data.repository.billing.BillingRepositoryImpl
import com.dabi.opensky.core.data.repository.booking.BookingRepository
import com.dabi.opensky.core.data.repository.booking.BookingRepositoryImpl
import com.dabi.opensky.core.data.repository.login.AuthRepository
import com.dabi.opensky.core.data.repository.login.AuthRepositoryImpl
import com.dabi.opensky.core.data.repository.room.RoomRepository
import com.dabi.opensky.core.data.repository.room.RoomRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindHotelRepository(
        roomRepositoryImpl: RoomRepositoryImpl
    ): RoomRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepository: BookingRepositoryImpl
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        billingRepository: BillingRepositoryImpl
    ): BillingRepository


}
