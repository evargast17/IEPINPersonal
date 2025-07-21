package com.e17kapps.iepinpersonal.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    // Los ViewModels con @HiltViewModel se inyectan automáticamente
    // Este módulo está listo para futuras dependencias específicas de ViewModels
}