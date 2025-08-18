package com.example.eclinic_summer.di

import com.example.eclinic_summer.data.model.repository.AppointmentRepositoryImpl
import com.example.eclinic_summer.data.model.repository.BookAppointmentUseCaseImpl
import com.example.eclinic_summer.data.model.repository.ChatRepositoryImpl
import com.example.eclinic_summer.data.model.repository.DocumentRepository
import com.example.eclinic_summer.data.model.repository.DocumentRepositoryImpl
import com.example.eclinic_summer.domain.domainrepository.AppointmentRepository
import com.example.eclinic_summer.domain.domainrepository.ChatRepository
import com.example.eclinic_summer.domain.domainrepository.usecase.BookAppointmentUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // --- Repositories ---
    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(
        impl: AppointmentRepositoryImpl
    ): AppointmentRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        impl: DocumentRepositoryImpl
    ): DocumentRepository

    // --- UseCases ---
    @Binds
    @Singleton
    abstract fun bindBookAppointmentUseCase(
        impl: BookAppointmentUseCaseImpl
    ): BookAppointmentUseCase
}
