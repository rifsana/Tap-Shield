package com.tapshield.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tapshield.app.data.local.dao.ContactDao
import com.tapshield.app.data.local.dao.SosEventDao
import com.tapshield.app.data.local.dao.SosSessionDao
import com.tapshield.app.data.local.entity.ContactEntity
import com.tapshield.app.data.local.entity.SosEventEntity
import com.tapshield.app.data.local.entity.SosSessionEntity

@Database(
    entities = [
        ContactEntity::class,
        SosSessionEntity::class,
        SosEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TapShieldDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun sosSessionDao(): SosSessionDao
    abstract fun sosEventDao(): SosEventDao

    companion object {
        @Volatile
        private var INSTANCE: TapShieldDatabase? = null

        fun getInstance(context: Context): TapShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TapShieldDatabase::class.java,
                    "tapshield_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
