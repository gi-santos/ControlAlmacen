package com.example.appalmacen.model.dao

import androidx.room.*
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.database.Contract
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDAO {

    @Query("SELECT * FROM ${Contract.TABLE_PRODUCTOS} WHERE ${Contract.ProductoColumns.HABILITADO} = 1")
    fun getHabilitados(): Flow<List<Producto>>

    @Query("SELECT * FROM ${Contract.TABLE_PRODUCTOS}")
    fun getAll(): Flow<List<Producto>>

    @Query("SELECT * FROM ${Contract.TABLE_PRODUCTOS} WHERE ${Contract.ProductoColumns.ID} = :id")
    suspend fun getById(id: Int): Producto?

    @Insert
    suspend fun insert(producto: Producto): Long

    @Update
    suspend fun update(producto: Producto)

    @Delete
    suspend fun delete(producto: Producto)

    @Query("UPDATE ${Contract.TABLE_PRODUCTOS} SET ${Contract.ProductoColumns.HABILITADO} = :habilitado WHERE ${Contract.ProductoColumns.ID} = :id")
    suspend fun setHabilitado(id: Int, habilitado: Boolean)
}