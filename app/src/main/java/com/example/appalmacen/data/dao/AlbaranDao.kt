package com.example.appalmacen.data.dao

import androidx.room.*
import com.example.appalmacen.model.entities.Albaran
import com.example.appalmacen.data.database.Contract
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbaranDAO {
    @Query("SELECT * FROM ${Contract.TABLE_ALBARANES}")
    fun getAll(): Flow<List<Albaran>>

    // Cambiamos a Long si tu ID en base de datos es tratado como tal por SQLite
    @Query("SELECT * FROM ${Contract.TABLE_ALBARANES} WHERE ${Contract.AlbaranColumns.ID} = :id")
    suspend fun getById(id: Int): Albaran?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlbaran(albaran: Albaran): Long

    @Update
    suspend fun update(albaran: Albaran): Int

    @Delete
    suspend fun delete(albaran: Albaran): Int


}