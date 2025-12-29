package com.sloy.sevibus.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [LineEntity::class, LineFtsEntity::class, StopEntity::class, StopFtsEntity::class, RouteEntity::class, PathEntity::class, FavoriteStopEntity::class, CardInfoEntity::class, DismissedAlertEntity::class],
    version = 10,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 7, to = 8, spec = PathMigration7To8::class),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10)
    ]
)
@TypeConverters(LocalDateTimeConverter::class, StringListConverter::class, IntListConverter::class)
abstract class SevibusDatabase : RoomDatabase() {
    abstract fun tussamDao(): TussamDao
    abstract fun sevibusDao(): SevibusDao
}

@RenameColumn(
    tableName = "paths",
    fromColumnName = "points",
    toColumnName = "polyline"
)
class PathMigration7To8 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        // Clear all paths since the data format changed from List<PositionDto> to Polyline
        // Paths will be refetched with the new polyline format
        db.execSQL("DELETE FROM paths")
    }
}
