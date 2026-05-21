package com.example.appalmacen.data.repository


import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.entities.Usuario
import kotlinx.coroutines.flow.Flow

interface AdminRepository {

    // ── Métodos para Productos ────────────────────────────────────────────
    fun getTodosLosProductos(): Flow<List<Producto>>

    suspend fun actualizarEstadoProducto(productoId: Int, nuevoEstado: Boolean)


    // ── Métodos para Usuarios ─────────────────────────────────────────────
    fun getTodosLosUsuarios(): Flow<List<Usuario>>
    fun contarUsuarios(): Flow<Int>
    suspend fun actualizarEstadoUsuario(usuarioId: Int, nuevoEstado: Boolean)
}