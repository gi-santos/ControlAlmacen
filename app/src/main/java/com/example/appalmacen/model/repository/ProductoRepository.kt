package com.example.appalmacen.model.repository

import com.example.appalmacen.model.dao.InteraccionDAO
import com.example.appalmacen.model.dao.ProductoDAO
import com.example.appalmacen.model.entities.Interaccion
import com.example.appalmacen.model.entities.Producto
import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val productoDAO: ProductoDAO,
    private val interaccionDAO: InteraccionDAO
) {
    val allProductos: Flow<List<Producto>> = productoDAO.getAll()
    val habilitados: Flow<List<Producto>> = productoDAO.getHabilitados()

    suspend fun getById(id: Int): Producto? = productoDAO.getById(id)

    suspend fun insert(producto: Producto): Long = productoDAO.insert(producto)

    suspend fun update(producto: Producto) = productoDAO.update(producto)

    suspend fun delete(producto: Producto) {
        interaccionDAO.deleteByProducto(producto.id)
        productoDAO.delete(producto)
    }

    suspend fun registrarInteraccion(usuarioId: Int, productoId: Int) {
        interaccionDAO.insert(Interaccion(usuarioId = usuarioId, productoId = productoId))
    }

    fun getUltimasInteracciones(usuarioId: Int): Flow<List<Producto>> {
        return interaccionDAO.getUltimasInteracciones(usuarioId)
    }

    fun searchProductos(query: String): Flow<List<Producto>> {
        return productoDAO.searchHabilitados(query)
    }
}
