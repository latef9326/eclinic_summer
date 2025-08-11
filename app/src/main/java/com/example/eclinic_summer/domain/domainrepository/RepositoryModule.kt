package com.example.eclinic_summer.domain.domainrepository

import com.example.eclinic_summer.data.model.repository.AppointmentRepositoryImpl
import com.example.eclinic_summer.domain.domainrepository.AppointmentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAppointmentRepository(
        impl: AppointmentRepositoryImpl
    ): AppointmentRepository
}