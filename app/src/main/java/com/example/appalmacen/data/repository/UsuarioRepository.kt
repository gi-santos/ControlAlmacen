package com.example.appalmacen.data.repository

import com.example.appalmacen.data.dao.UsuarioDAO
import com.example.appalmacen.model.entities.Usuario
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val usuarioDAO: UsuarioDAO) {

    // Lista de todos los usuarios (Flow es ideal para MVVM y Room)
    val allUsuarios: Flow<List<Usuario>> = usuarioDAO.getAll()

    val habilitados: Flow<List<Usuario>> = usuarioDAO.getHabilitados()

    suspend fun getById(id: Int): Usuario? {
        return usuarioDAO.getById(id)
    }

    suspend fun insert(usuario: Usuario): Long {
        return usuarioDAO.insert(usuario)
    }

    suspend fun update(usuario: Usuario) {
        usuarioDAO.update(usuario)
    }

    suspend fun delete(usuario: Usuario) {
        usuarioDAO.delete(usuario)
    }

    suspend fun setHabilitado(id: Int, habilitado: Boolean) {
        usuarioDAO.setHabilitado(id, habilitado)
    }

    // Esta función permanece, pero solo devuelve el resultado de la DB.
    // No guarda nada en variables estáticas.
    suspend fun login(email: String, password: String): Usuario? {
        return usuarioDAO.login(email, password)
    }

    suspend fun getAdmins(): List<Usuario> {
        return usuarioDAO.getAdmins()
    }
}