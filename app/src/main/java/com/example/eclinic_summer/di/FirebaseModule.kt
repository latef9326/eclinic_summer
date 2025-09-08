package com.example.eclinic_summer.di

import com.example.eclinic_summer.data.model.repository.AuthRepositoryImpl
import com.example.eclinic_summer.data.model.repository.UserRepositoryImpl
import com.example.eclinic_summer.domain.domainrepository.AuthRepository
import com.example.eclinic_summer.domain.domainrepository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides Firebase services and repository implementations as dependencies.
 *
 * This module sets up [FirebaseAuth], [FirebaseFirestore], [FirebaseStorage],
 * and binds them to repository interfaces using Hilt dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /** Provides a singleton instance of [FirebaseAuth]. */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /** Provides a singleton instance of [FirebaseFirestore]. */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    /** Provides a singleton instance of [FirebaseStorage]. */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    /** Provides an [AuthRepository] implementation backed by Firebase. */
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(auth, firestore)

    /** Provides a [UserRepository] implementation backed by Firestore. */
    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore
    ): UserRepository = UserRepositoryImpl(firestore)
}
