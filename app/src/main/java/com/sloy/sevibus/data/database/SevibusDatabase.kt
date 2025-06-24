package com.sloy.sevibus.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [LineEntity::class, LineFtsEntity::class, StopEntity::class, StopFtsEntity::class, RouteEntity::class, PathEntity::class, FavoriteStopEntity::class, CardInfoEntity::class],
    version = 7,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
    ]
)
@TypeConverters(LocalDateTimeConverter::class, StringListConverter::class, IntListConverter::class, PositionListConverter::class)
abstract class SevibusDatabase : RoomDatabase() {
    abstract fun tussamDao(): TussamDao
    abstract fun sevibusDao(): SevibusDao
}
