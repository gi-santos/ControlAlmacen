package com.example.appalmacen.data.repository

import com.example.appalmacen.data.dao.ProductoDAO
import com.example.appalmacen.data.dao.UsuarioDAO
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.entities.Usuario
import kotlinx.coroutines.flow.Flow

class AdminRepositoryImpl(
    private val productoDao: ProductoDAO,
    private val usuarioDao: UsuarioDAO
) : AdminRepository {

    // ── Lógica de Productos ───────────────────────────────────────────────

    override fun getTodosLosProductos(): Flow<List<Producto>> {
        return productoDao.getTodosLosProductos()
    }

    override suspend fun actualizarEstadoProducto(productoId: Int, nuevoEstado: Boolean) {
        productoDao.actualizarEstado(productoId, nuevoEstado)
    }

    // ── Lógica de Usuarios ────────────────────────────────────────────────

    override fun getTodosLosUsuarios(): Flow<List<Usuario>> {
        return usuarioDao.getTodosLosUsuarios()
    }

    override fun contarUsuarios(): Flow<Int> {

        return usuarioDao.contarUsuarios()
    }

    override suspend fun actualizarEstadoUsuario(usuarioId: Int, nuevoEstado: Boolean) {
        usuarioDao.setHabilitado(usuarioId, nuevoEstado)
    }
}