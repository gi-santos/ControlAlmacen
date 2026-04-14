package com.example.appalmacen.model.repository


import com.example.appalmacen.model.dao.InteraccionDAO
import com.example.appalmacen.model.entities.UsuarioProductoInteraccion
import kotlinx.coroutines.flow.Flow

class InteraccionRepository(
    private val interaccionDAO: InteraccionDAO
) {
    fun getByUsuario(usuarioId: Int): Flow<List<UsuarioProductoInteraccion>> =
        interaccionDAO.getByUsuario(usuarioId)

    fun getByProducto(productoId: Int): Flow<List<UsuarioProductoInteraccion>> =
        interaccionDAO.getByProducto(productoId)

    suspend fun registrar(interaccion: UsuarioProductoInteraccion) =
        interaccionDAO.insert(interaccion)
}