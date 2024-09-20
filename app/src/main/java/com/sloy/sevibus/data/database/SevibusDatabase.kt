package com.sloy.sevibus.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [LineEntity::class, StopEntity::class, RouteEntity::class, PathEntity::class],
    version = 2,
)
@TypeConverters(LocalDateTimeConverter::class, StringListConverter::class, IntListConverter::class, PositionListConverter::class)
abstract class SevibusDatabase : RoomDatabase() {
    abstract fun tussamDao(): TussamDao
}