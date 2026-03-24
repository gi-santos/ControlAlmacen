package com.example.appalmacen.model.repository

import com.example.appalmacen.model.dao.UsuarioDAO
import com.example.appalmacen.model.entities.Usuario
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val usuarioDAO: UsuarioDAO) {

    // Lista de todos los usuarios
    val allUsuarios: Flow<List<Usuario>> = usuarioDAO.getAll()

    // Lista de usuarios habilitados
    val habilitados: Flow<List<Usuario>> = usuarioDAO.getHabilitados()

    // Buscar por ID
    suspend fun getById(id: Int): Usuario? {
        return usuarioDAO.getById(id)
    }

    // Insertar un nuevo usuario
    suspend fun insert(usuario: Usuario): Long {
        return usuarioDAO.insert(usuario)
    }

    // Actualizar datos del usuario
    suspend fun update(usuario: Usuario) {
        usuarioDAO.update(usuario)
    }

    // Borrar (Eliminación física)
    suspend fun delete(usuario: Usuario) {
        usuarioDAO.delete(usuario)
    }

    // Deshabilitar (Borrado lógico)
    suspend fun setHabilitado(id: Int, habilitado: Boolean) {
        usuarioDAO.setHabilitado(id, habilitado)
    }

    // Función de Login
    suspend fun login(email: String, password: String): Usuario? {
        return usuarioDAO.login(email, password)
    }
}
