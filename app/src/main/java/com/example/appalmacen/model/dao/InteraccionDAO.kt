package com.example.appalmacen.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appalmacen.model.entities.Interaccion
import com.example.appalmacen.model.entities.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface InteraccionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(interaccion: Interaccion)

    @Query("""
        SELECT DISTINCT p.* FROM productos p 
        INNER JOIN interacciones i ON p.id = i.producto_id 
        WHERE i.usuario_id = :usuarioId AND p.habilitado = 1
        ORDER BY i.fecha DESC 
        LIMIT 10
    """)
    fun getUltimasInteracciones(usuarioId: Int): Flow<List<Producto>>
    
    @Query("DELETE FROM interacciones WHERE producto_id = :productoId")
    suspend fun deleteByProducto(productoId: Int)
}
