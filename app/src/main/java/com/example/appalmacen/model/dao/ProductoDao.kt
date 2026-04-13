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

    @Query("""
    UPDATE ${Contract.TABLE_PRODUCTOS} 
    SET ${Contract.ProductoColumns.CANTIDAD} = :nuevaCantidad,
        ${Contract.ProductoColumns.FECHA_ULTIMA_INTERACCION} = :timestamp
    WHERE ${Contract.ProductoColumns.ID} = :id
""")
    suspend fun actualizarCantidad(id: Int, nuevaCantidad: Int, timestamp: Long)

    @Query("""
    SELECT * FROM ${Contract.TABLE_PRODUCTOS} 
    WHERE ${Contract.ProductoColumns.HABILITADO} = 1 
    AND (
        ${Contract.ProductoColumns.NOMBRE} LIKE '%' || :query || '%'
    )
    ORDER BY ${Contract.ProductoColumns.NOMBRE} ASC
""")
    fun buscar(query: String): Flow<List<Producto>>
}