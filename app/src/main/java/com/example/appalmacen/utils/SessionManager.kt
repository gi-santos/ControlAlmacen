package com.example.appalmacen.utils

import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.data.repository.UsuarioRepository

class SessionManager(
    private val preferencesManager: PreferencesManager,
    private val usuarioRepository: UsuarioRepository
) {
    // Estado en memoria para acceso rápido durante la ejecución
    var usuarioActivo: Usuario? = null
        private set

    // Comprobar si hay sesión y cargar datos del usuario
    suspend fun restaurarSesion(): Boolean {
        if (preferencesManager.isLoggedIn()) {
            val userId = preferencesManager.getUserId()
            val usuario = usuarioRepository.getById(userId)
            if (usuario != null && usuario.habilitado) {
                usuarioActivo = usuario
                return true
            }
        }
        return false
    }

    fun iniciarSesion(usuario: Usuario) {
        usuarioActivo = usuario
        preferencesManager.saveUserSession(usuario.id)
    }

    fun cerrarSesion() {
        usuarioActivo = null
        preferencesManager.clearSession()
    }

    fun esAdmin(): Boolean = usuarioActivo?.esAdmin ?: false
}