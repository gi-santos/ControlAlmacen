package com.example.appalmacen.model.dao

import androidx.room.*
import com.example.appalmacen.model.entities.Perfil
import com.example.appalmacen.model.database.Contract
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDAO {

    @Query("SELECT * FROM ${Contract.TABLE_PERFILES}")
    fun getAll(): Flow<List<Perfil>>

    @Query("SELECT * FROM ${Contract.TABLE_PERFILES} WHERE ${Contract.PerfilColumns.ID} = :id")
    suspend fun getById(id: Int): Perfil?

    @Insert
    suspend fun insert(perfil: Perfil): Long

    @Update
    suspend fun update(perfil: Perfil)

    @Delete
    suspend fun delete(perfil: Perfil)
}