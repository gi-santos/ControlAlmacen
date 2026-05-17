package com.example.appalmacen.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appalmacen.data.database.Contract
import com.example.appalmacen.model.entities.UsuarioProductoInteraccion
import kotlinx.coroutines.flow.Flow

@Dao
interface InteraccionDAO {

    @Insert
    suspend fun insert(interaccion: UsuarioProductoInteraccion)

    @Query("""
        SELECT * FROM ${Contract.TABLE_INTERACCIONES} 
        WHERE ${Contract.InteraccionColumns.USUARIO_ID} = :usuarioId 
        ORDER BY ${Contract.InteraccionColumns.TIMESTAMP} DESC
    """)
    fun getByUsuario(usuarioId: Int): Flow<List<UsuarioProductoInteraccion>>

    @Query("""
        SELECT * FROM ${Contract.TABLE_INTERACCIONES} 
        WHERE ${Contract.InteraccionColumns.PRODUCTO_ID} = :productoId 
        ORDER BY ${Contract.InteraccionColumns.TIMESTAMP} DESC
    """)
    fun getByProducto(productoId: Int): Flow<List<UsuarioProductoInteraccion>>


}