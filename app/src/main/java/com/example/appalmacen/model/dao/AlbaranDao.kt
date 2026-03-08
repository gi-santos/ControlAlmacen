package com.example.appalmacen.model.dao

import androidx.room.*
import com.example.appalmacen.model.entities.Albaran
import com.example.appalmacen.model.database.Contract
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbaranDAO {

    @Query("SELECT * FROM ${Contract.TABLE_ALBARANES}")
    fun getAll(): Flow<List<Albaran>>

    @Query("SELECT * FROM ${Contract.TABLE_ALBARANES} WHERE ${Contract.AlbaranColumns.ID} = :id")
    suspend fun getById(id: Int): Albaran?

    @Insert
    suspend fun insert(albaran: Albaran): Long

    @Update
    suspend fun update(albaran: Albaran)

    @Delete
    suspend fun delete(albaran: Albaran)

    @Query("SELECT * FROM ${Contract.TABLE_ALBARANES} WHERE ${Contract.AlbaranColumns.PAGADO} = :pagado")
    fun getByEstadoPago(pagado: Boolean): Flow<List<Albaran>>
}