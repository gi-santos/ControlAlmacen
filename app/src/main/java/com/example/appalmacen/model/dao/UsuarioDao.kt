package com.example.appalmacen.model.dao

import androidx.room.*
import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.model.database.Contract
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDAO {

    @Query("SELECT * FROM ${Contract.TABLE_USUARIOS} WHERE ${Contract.UsuarioColumns.HABILITADO} = 1")
    fun getHabilitados(): Flow<List<Usuario>>

    @Query("SELECT * FROM ${Contract.TABLE_USUARIOS}")
    fun getAll(): Flow<List<Usuario>>

    @Query("SELECT * FROM ${Contract.TABLE_USUARIOS} WHERE ${Contract.UsuarioColumns.ID} = :id")
    suspend fun getById(id: Int): Usuario?

    @Insert
    suspend fun insert(usuario: Usuario): Long

    @Update
    suspend fun update(usuario: Usuario)

    @Delete
    suspend fun delete(usuario: Usuario)

    @Query("UPDATE ${Contract.TABLE_USUARIOS} SET ${Contract.UsuarioColumns.HABILITADO} = :habilitado WHERE ${Contract.UsuarioColumns.ID} = :id")
    suspend fun setHabilitado(id: Int, habilitado: Boolean)

    @Query("SELECT * FROM ${Contract.TABLE_USUARIOS} WHERE ${Contract.UsuarioColumns.EMAIL} = :email AND ${Contract.UsuarioColumns.PASSWORD} = :password AND ${Contract.UsuarioColumns.HABILITADO} = 1 LIMIT 1")
    suspend fun login(email: String, password: String): Usuario?
}