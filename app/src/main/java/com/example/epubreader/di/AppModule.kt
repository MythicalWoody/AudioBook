package com.example.epubreader.di

import android.content.Context
import com.example.epubreader.presentation.ui.reader.components.TextToSpeechController
import com.example.epubreader.presentation.ui.reader.components.TextToSpeechControllerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTextToSpeechController(
        @ApplicationContext context: Context
    ): TextToSpeechController {
        return TextToSpeechControllerImpl(context)
    }
}