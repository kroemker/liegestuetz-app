package com.liegestuetz.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Top-level Hilt module for app-scoped bindings.
 *
 * Repository and data-source bindings live in:
 *   - :core:core-data  (DataModule)
 *
 * Firebase SDK bindings (FirebaseAuth, FirebaseFirestore, FirebaseMessaging)
 * are provided in :core:core-data's FirebaseModule.
 *
 * This module is intentionally lean — add only truly app-scoped singletons here.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
