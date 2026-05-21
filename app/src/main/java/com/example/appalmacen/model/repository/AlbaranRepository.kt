package com.example.appalmacen.model.repository

import com.example.appalmacen.model.dao.AlbaranDAO
import com.example.appalmacen.model.entities.Albaran
import kotlinx.coroutines.flow.Flow

class AlbaranRepository(private val albaranDAO: AlbaranDAO) {
    val allAlbaranes: Flow<List<Albaran>> = albaranDAO.getAll()

    suspend fun getById(id: Int): Albaran? = albaranDAO.getById(id)

    suspend fun insert(albaran: Albaran): Long = albaranDAO.insert(albaran)

    suspend fun update(albaran: Albaran) = albaranDAO.update(albaran)

    suspend fun delete(albaran: Albaran) = albaranDAO.delete(albaran)

    fun getByEstadoPago(pagado: Boolean): Flow<List<Albaran>> = albaranDAO.getByEstadoPago(pagado)
}
