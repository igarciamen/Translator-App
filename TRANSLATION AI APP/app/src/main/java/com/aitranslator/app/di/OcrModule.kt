package com.aitranslator.app.di

import com.aitranslator.app.data.ocr.CameraCaptureUriProvider
import com.aitranslator.app.data.ocr.DefaultCameraCaptureUriProvider
import com.aitranslator.app.data.ocr.MlKitTextRecognizer
import com.aitranslator.app.data.ocr.OcrRepositoryImpl
import com.aitranslator.app.domain.ocr.OcrRepository
import com.aitranslator.app.domain.ocr.TextRecognizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OcrModule {

    @Binds
    abstract fun bindTextRecognizer(impl: MlKitTextRecognizer): TextRecognizer

    @Binds
    abstract fun bindOcrRepository(impl: OcrRepositoryImpl): OcrRepository

    @Binds
    abstract fun bindCameraCaptureUriProvider(
        impl: DefaultCameraCaptureUriProvider
    ): CameraCaptureUriProvider
}