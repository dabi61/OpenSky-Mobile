package com.dabi.opensky.core.database.di


import android.app.Application
import androidx.room.Room
import com.dabi.opensky.core.database.OpenSkyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        application: Application,
//        typeResponseConverter: TypeResponseConverter,
//        statsResponseConverter: StatsResponseConverter,
    ): OpenSkyDatabase {
        return Room
            .databaseBuilder(application, OpenSkyDatabase::class.java, "OpenSky.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
//            .addTypeConverter(typeResponseConverter)
//            .addTypeConverter(statsResponseConverter)
            .build()
    }

//    @Provides
//    @Singleton
//    fun providePokemonDao(appDatabase: AppDatabase):
//
//    {
//        return appDatabase.pokemonDao()
//    }
//
//    @Provides
//    @Singleton
//    fun providePokemonInfoDao(appDatabase: PokedexDatabase): PokemonInfoDao {
//        return appDatabase.pokemonInfoDao()
//    }
//
//    @Provides
//    @Singleton
//    fun provideTypeResponseConverter(json: Json): TypeResponseConverter {
//        return TypeResponseConverter(json)
//    }
}
