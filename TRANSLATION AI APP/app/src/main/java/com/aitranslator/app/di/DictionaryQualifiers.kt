package com.aitranslator.app.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OnlineDictionary

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OfflineDictionary