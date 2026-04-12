package com.example.appalmacen.controller

import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager

class SesionController(
    private val usuarioRepository: UsuarioRepository,
    private val preferencesManager: PreferencesManager
) {

    // Variable estática para acceder al usuario actual en toda la App
    companion object {
        var usuarioActivo: Usuario? = null
    }

    // Función principal de Login
    suspend fun login(email: String, password: String): Boolean {
        val usuario = usuarioRepository.login(email, password)
        return if (usuario != null) {
            usuarioActivo = usuario
            preferencesManager.saveUserSession(usuario.id)
            true
        } else {
            false
        }
    }

    // Cerrar sesión
    fun logout() {
        usuarioActivo = null
        preferencesManager.clearSession()
    }

    // Comprobar si hay una sesión guardada al abrir la App
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

    fun esAdmin(): Boolean = usuarioActivo?.esAdmin ?: false
}
