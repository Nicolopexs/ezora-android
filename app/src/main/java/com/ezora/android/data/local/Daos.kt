package com.ezora.android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MedicoDao {
    @Query("SELECT * FROM medicos ORDER BY apellido, nombre")
    abstract fun observeAll(): Flow<List<MedicoEntity>>

    @Query("SELECT COUNT(*) FROM medicos")
    abstract fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<MedicoEntity>)

    @Query("DELETE FROM medicos")
    abstract suspend fun clear()

    @Transaction
    open suspend fun replaceAll(items: List<MedicoEntity>) {
        clear()
        insertAll(items)
    }
}

@Dao
abstract class FarmaciaDao {
    @Query("SELECT * FROM farmacias ORDER BY nombre")
    abstract fun observeAll(): Flow<List<FarmaciaEntity>>

    @Query("SELECT COUNT(*) FROM farmacias")
    abstract fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<FarmaciaEntity>)

    @Query("DELETE FROM farmacias")
    abstract suspend fun clear()

    @Transaction
    open suspend fun replaceAll(items: List<FarmaciaEntity>) {
        clear()
        insertAll(items)
    }
}

@Dao
abstract class StockDao {
    @Query("SELECT * FROM stock ORDER BY nombre, lote")
    abstract fun observeAll(): Flow<List<StockEntity>>

    @Query("SELECT COUNT(*) FROM stock")
    abstract fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<StockEntity>)

    @Query("DELETE FROM stock")
    abstract suspend fun clear()

    @Transaction
    open suspend fun replaceAll(items: List<StockEntity>) {
        clear()
        insertAll(items)
    }
}

@Dao
abstract class KvDao {
    @Query("SELECT v FROM kv WHERE k = :key LIMIT 1")
    abstract suspend fun get(key: String): String?

    @Query("SELECT v FROM kv WHERE k = :key LIMIT 1")
    abstract fun observe(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun put(entity: KvEntity)

    @Query("DELETE FROM kv WHERE k = :key")
    abstract suspend fun delete(key: String)
}
