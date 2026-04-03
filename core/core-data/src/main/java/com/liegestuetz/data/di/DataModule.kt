package com.liegestuetz.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds repository interfaces (from :core:core-domain) to their implementations.
 *
 * Populated in Phase 2–4 as each repository implementation is written:
 *
 *   Phase 2: UserRepositoryImpl, AuthRepository bindings
 *   Phase 3: ChallengeRepositoryImpl binding
 *   Phase 4: CompletionRepositoryImpl binding
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule
