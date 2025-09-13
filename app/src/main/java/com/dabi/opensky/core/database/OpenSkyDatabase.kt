package com.dabi.opensky.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
//    entities = [MediaEntity::class],
    version = 1,
    exportSchema = false,
)
//@TypeConverters(value = [TypeResponseConverter::class, StatsResponseConverter::class])
abstract class OpenSkyDatabase : RoomDatabase() {

//    abstract fun mediaDao(): MediaDao
//    abstract fun pokemonInfoDao(): PokemonInfoDao
}
