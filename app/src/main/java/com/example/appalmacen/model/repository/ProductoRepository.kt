package com.example.appalmacen.model.repository

import com.example.appalmacen.model.dao.ProductoDAO
import com.example.appalmacen.model.entities.Producto
import kotlinx.coroutines.flow.Flow

class ProductoRepository(private val productoDAO: ProductoDAO) {

    val productosHabilitados: Flow<List<Producto>> = productoDAO.getHabilitados()
    val todosLosProductos: Flow<List<Producto>> = productoDAO.getAll()

    suspend fun insertar(producto: Producto): Long {
        return productoDAO.insert(producto)
    }

    suspend fun actualizar(producto: Producto) {
        productoDAO.update(producto)
    }

    suspend fun eliminar(producto: Producto) {
        productoDAO.delete(producto)
    }

    suspend fun obtenerPorId(id: Int): Producto? {
        return productoDAO.getById(id)
    }

    suspend fun setHabilitado(id: Int, habilitado: Boolean) {
        productoDAO.setHabilitado(id, habilitado)
    }
}