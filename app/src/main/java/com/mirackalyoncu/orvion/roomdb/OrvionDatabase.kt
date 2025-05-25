package com.mirackalyoncu.orvion.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mirackalyoncu.orvion.model.Gorev
import com.mirackalyoncu.orvion.model.GorevLog
import com.mirackalyoncu.orvion.model.Kullanici
import com.mirackalyoncu.orvion.model.Mail
import com.mirackalyoncu.orvion.model.Surec
import com.mirackalyoncu.orvion.model.SurecKatilim

@Database(
    entities = [Kullanici::class, Surec::class, Gorev::class, GorevLog::class, SurecKatilim::class, Mail::class],
    version = 7
)

abstract class OrvionDatabase : RoomDatabase() {
    abstract fun kullaniciDao(): KullaniciDao
    abstract fun surecDao(): SurecDao
    abstract fun gorevDao(): GorevDao
    abstract fun gorevLogDao(): GorevLogDao
    abstract fun surecKatilimDao(): SurecKatilimDao
    abstract fun mailDao(): MailDao

    companion object {
        @Volatile
        private var INSTANCE: OrvionDatabase? = null

        fun getInstance(context: Context): OrvionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OrvionDatabase::class.java,
                    "orvion_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}