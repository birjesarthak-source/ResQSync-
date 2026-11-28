package com.resqsync.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.resqsync.app.database.dao.EmergencyReportDao
import com.resqsync.app.database.dao.NetworkMessageDao
import com.resqsync.app.database.entity.EmergencyReportEntity
import com.resqsync.app.database.entity.NetworkMessageEntity

@Database(
    entities = [
        EmergencyReportEntity::class,
        NetworkMessageEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun emergencyReportDao(): EmergencyReportDao

    abstract fun networkMessageDao(): NetworkMessageDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN isSyncedToCloud INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN aiSummary TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN locationSource TEXT NOT NULL DEFAULT 'UNAVAILABLE'")
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN locationAccuracyMeters REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN locationAltitudeMeters REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN locationTimestamp INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN locationAddress TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN locationLandmark TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN locationSourceDeviceId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN voiceBase64 TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE emergency_reports ADD COLUMN photoPaths TEXT NOT NULL DEFAULT '[]'")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "resqsync_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
