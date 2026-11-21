package com.dev.fxwatch.core.domain.di

import javax.inject.Qualifier

/** Dispatcher for blocking I/O such as network and disk. */
@Qualifier
annotation class IoDispatcher

/** Dispatcher for CPU-bound work. */
@Qualifier
annotation class DefaultDispatcher
