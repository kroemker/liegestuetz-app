package com.liegestuetz.data.di

import com.liegestuetz.data.repository.ChallengeRepositoryImpl
import com.liegestuetz.data.repository.CompletionRepositoryImpl
import com.liegestuetz.data.repository.UserRepositoryImpl
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.repository.ChallengeRepository
import com.liegestuetz.domain.repository.CompletionRepository
import com.liegestuetz.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    // UserRepositoryImpl implements both AuthRepository and UserRepository
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: UserRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindChallengeRepository(impl: ChallengeRepositoryImpl): ChallengeRepository

    @Binds @Singleton
    abstract fun bindCompletionRepository(impl: CompletionRepositoryImpl): CompletionRepository
}
