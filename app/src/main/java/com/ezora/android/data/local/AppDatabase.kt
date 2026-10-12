package com.ezora.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MedicoEntity::class, FarmaciaEntity::class, StockEntity::class, KvEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicoDao(): MedicoDao
    abstract fun farmaciaDao(): FarmaciaDao
    abstract fun stockDao(): StockDao
    abstract fun kvDao(): KvDao
}
