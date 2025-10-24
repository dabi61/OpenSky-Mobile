package com.dabi.opensky.core.data.di

import com.dabi.opensky.core.data.repository.billing.BillingRepository
import com.dabi.opensky.core.data.repository.billing.BillingRepositoryImpl
import com.dabi.opensky.core.data.repository.booking.BookingRepository
import com.dabi.opensky.core.data.repository.booking.BookingRepositoryImpl
import com.dabi.opensky.core.data.repository.login.AuthRepository
import com.dabi.opensky.core.data.repository.login.AuthRepositoryImpl
import com.dabi.opensky.core.data.repository.room.RoomRepository
import com.dabi.opensky.core.data.repository.room.RoomRepositoryImpl
import com.dabi.opensky.core.data.repository.schedule.ScheduleRepository
import com.dabi.opensky.core.data.repository.schedule.ScheduleRepositoryImpl
import com.dabi.opensky.core.data.repository.tour.TourRepository
import com.dabi.opensky.core.data.repository.tour.TourRepositoryImpl
import com.dabi.opensky.core.data.repository.tourguide.TourGuideRepository
import com.dabi.opensky.core.data.repository.tourguide.TourGuideRepositoryImpl
import com.dabi.opensky.core.data.repository.voucher.VoucherRepository
import com.dabi.opensky.core.data.repository.voucher.VoucherRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindVoucherRepository(
        voucherRepository: VoucherRepositoryImpl
    ): VoucherRepository

    @Binds
    @Singleton
    abstract fun bindTourRepository(
        tourRepository: TourRepositoryImpl
    ): TourRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        scheduleRepository: ScheduleRepositoryImpl
    ): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindTourGuideRepository(
        tourGuideRepository: TourGuideRepositoryImpl
    ): TourGuideRepository
}
