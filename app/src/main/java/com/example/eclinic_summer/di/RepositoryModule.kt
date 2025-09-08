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

/**
 * Provides repository and use case bindings for dependency injection.
 *
 * This module binds concrete repository implementations to their
 * corresponding interfaces, making them available as singletons
 * throughout the application.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** Binds [AppointmentRepositoryImpl] as the implementation for [AppointmentRepository]. */
    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(
        impl: AppointmentRepositoryImpl
    ): AppointmentRepository

    /** Binds [ChatRepositoryImpl] as the implementation for [ChatRepository]. */
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    /** Binds [DocumentRepositoryImpl] as the implementation for [DocumentRepository]. */
    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        impl: DocumentRepositoryImpl
    ): DocumentRepository

    /** Binds [BookAppointmentUseCaseImpl] as the implementation for [BookAppointmentUseCase]. */
    @Binds
    @Singleton
    abstract fun bindBookAppointmentUseCase(
        impl: BookAppointmentUseCaseImpl
    ): BookAppointmentUseCase
}
