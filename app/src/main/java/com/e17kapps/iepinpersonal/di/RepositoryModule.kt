package com.e17kapps.iepinpersonal.di

import com.e17kapps.iepinpersonal.data.repository.AuthRepositoryImpl
import com.e17kapps.iepinpersonal.data.repository.EmployeeRepositoryImpl
import com.e17kapps.iepinpersonal.data.repository.PaymentRepositoryImpl
import com.e17kapps.iepinpersonal.data.repository.StatisticsRepositoryImpl
import com.e17kapps.iepinpersonal.domain.repository.AuthRepository
import com.e17kapps.iepinpersonal.domain.repository.EmployeeRepository
import com.e17kapps.iepinpersonal.domain.repository.PaymentRepository
import com.e17kapps.iepinpersonal.domain.repository.StatisticsRepository
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
    abstract fun bindEmployeeRepository(
        employeeRepositoryImpl: EmployeeRepositoryImpl
    ): EmployeeRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(
        statisticsRepositoryImpl: StatisticsRepositoryImpl
    ): StatisticsRepository
}