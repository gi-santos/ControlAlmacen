package com.example.appalmacen.model.dao

import androidx.room.*
import com.example.appalmacen.model.entities.Proveedor
import com.example.appalmacen.model.database.Contract
import kotlinx.coroutines.flow.Flow

@Dao
interface ProveedorDAO {

    @Query("SELECT * FROM ${Contract.TABLE_PROVEEDORES}")
    fun getAll(): Flow<List<Proveedor>>

    @Query("SELECT * FROM ${Contract.TABLE_PROVEEDORES} WHERE ${Contract.ProveedorColumns.ID} = :id")
    suspend fun getById(id: Int): Proveedor?

    @Insert
    suspend fun insert(proveedor: Proveedor): Long

    @Update
    suspend fun update(proveedor: Proveedor)

    @Delete
    suspend fun delete(proveedor: Proveedor)
}