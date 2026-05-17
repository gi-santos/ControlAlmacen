package com.example.appalmacen.data.repository

import com.example.appalmacen.data.dao.InteraccionDAO
import com.example.appalmacen.data.dao.ProductoDAO
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.entities.UsuarioProductoInteraccion
import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val productoDAO: ProductoDAO,
    private val interaccionDAO: InteraccionDAO
) {
    fun getHabilitados(): Flow<List<Producto>> = productoDAO.getHabilitados()

    fun buscar(query: String): Flow<List<Producto>> = productoDAO.buscar(query)



    suspend fun actualizarCantidad(
        usuarioId: Int,
        producto: Producto,
        nuevaCantidad: Int
    ) {
        val timestamp = System.currentTimeMillis()

        // 1. Actualizar cantidad y fecha en tabla productos
        productoDAO.actualizarCantidad(
            id = producto.id,
            nuevaCantidad = nuevaCantidad,
            timestamp = timestamp
        )

        // 2. Registrar interacción
        interaccionDAO.insert(
            UsuarioProductoInteraccion(
                usuarioId = usuarioId,
                productoId = producto.id,
                cantidadAnterior = producto.cantidad,
                cantidadNueva = nuevaCantidad,
                tipoAccion = if (nuevaCantidad > producto.cantidad) "SUMA" else "RESTA",
                timestamp = timestamp
            )
        )
    }


    suspend fun insertar(producto: Producto) {
        productoDAO.insert(producto)
    }

    fun getProductosRecientesPorUsuario(usuarioId: Int): Flow<List<Producto>> {

        return productoDAO.getProductosRecientesPorUsuario(usuarioId)
    }
}