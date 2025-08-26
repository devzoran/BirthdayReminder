package com.birthday.reminder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Birthday::class],
    version = 2,
    exportSchema = false
)
abstract class BirthdayDatabase : RoomDatabase() {
    
    abstract fun birthdayDao(): BirthdayDao
    
    companion object {
        @Volatile
        private var INSTANCE: BirthdayDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE birthdays ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): BirthdayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BirthdayDatabase::class.java,
                    "birthday_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
