package com.example.appalmacen.viewmodel.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appalmacen.data.database.DatabaseHelper
import com.example.appalmacen.data.repository.AdminRepository
import com.example.appalmacen.data.repository.AdminRepositoryImpl
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.entities.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseHelper.Companion.getInstance(application)
    private val repository: AdminRepository = AdminRepositoryImpl(db.productoDAO(), db.usuarioDAO())

    private val _queryProducto = MutableStateFlow("")
    private val _queryUsuario  = MutableStateFlow("")

    // ── Productos ─────────────────────────────────────────────────────────
    val productosFiltrados: StateFlow<List<Producto>> =
        combine(
            repository.getTodosLosProductos(), // ← Ahora llama al repositorio
            _queryProducto
        ) { productos, query ->
            if (query.isBlank()) productos
            else productos.filter { it.nombre.contains(query, ignoreCase = true) }
        }.stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.Companion.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun filtrarProductos(query: String) { _queryProducto.value = query }

    fun cambiarEstadoProducto(producto: Producto, nuevoEstado: Boolean) {
        viewModelScope.launch {
            repository.actualizarEstadoProducto(producto.id, nuevoEstado) // ← Al repositorio
        }
    }

    // ── Usuarios ──────────────────────────────────────────────────────────
    val usuariosFiltrados: StateFlow<List<Usuario>> =
        combine(
            repository.getTodosLosUsuarios(), // ← Al repositorio
            _queryUsuario
        ) { usuarios, query ->
            if (query.isBlank()) usuarios
            else usuarios.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            }
        }.stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.Companion.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val totalUsuarios: StateFlow<Int> =
        repository.contarUsuarios() // ← Al repositorio
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000), 0)

    fun setQueryUsuario(query: String) { _queryUsuario.value = query }

    fun cambiarEstadoUsuario(usuario: Usuario, nuevoEstado: Boolean) {
        viewModelScope.launch {
            repository.actualizarEstadoUsuario(usuario.id, nuevoEstado) // ← Al repositorio (¡Ya no dará error!)
        }
    }

    fun cerrarSesion() { /* limpiar SharedPreferences / DataStore */ }
}