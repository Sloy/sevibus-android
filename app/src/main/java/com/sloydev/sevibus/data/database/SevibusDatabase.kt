package com.sloydev.sevibus.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [LineEntity::class, StopEntity::class, RouteEntity::class],
    version = 1
)
@TypeConverters(LocalDateTimeConverter::class, StringListConverter::class, IntListConverter::class)
abstract class SevibusDatabase : RoomDatabase() {
    abstract fun tussamDao(): TussamDao
}