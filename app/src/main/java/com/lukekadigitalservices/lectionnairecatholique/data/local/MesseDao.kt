package com.lukekadigitalservices.lectionnairecatholique.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MesseDao {
    @Query("SELECT * FROM messes_cache WHERE date = :date AND zone = :zone")
    suspend fun getMesse(date: String, zone: String): MesseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMesse(messe: MesseEntity)
}