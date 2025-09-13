package com.dabi.opensky.core.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

//    @Binds
//    fun bindsLibraryRepository(libraryRepositoryImpl: LibraryRepositoryImpl): LibraryRepository

//    @Binds
//    fun bindsLibraryRepository(detailsRepositoryImpl: DetailsRepositoryImpl): DetailsRepository
}