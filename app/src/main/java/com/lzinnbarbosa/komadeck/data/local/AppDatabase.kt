package com.lzinnbarbosa.komadeck.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MidiaEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun midiaDao(): MidiaDao
}