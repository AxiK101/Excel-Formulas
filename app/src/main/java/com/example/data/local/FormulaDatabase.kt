package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FormulaEntity::class],
    version = 5,
    exportSchema = false
)
abstract class FormulaDatabase : RoomDatabase() {
    abstract fun formulaDao(): FormulaDao

    companion object {
        @Volatile
        private var INSTANCE: FormulaDatabase? = null

        fun getInstance(context: Context): FormulaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FormulaDatabase::class.java,
                    "formulagrid_cache.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
