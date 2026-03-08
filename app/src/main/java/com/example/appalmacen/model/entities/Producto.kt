package com.example.appalmacen.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.appalmacen.model.database.Contract

@Entity(tableName = Contract.TABLE_PRODUCTOS)
data class Producto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Contract.ProductoColumns.ID)
    val id: Int = 0,

    @ColumnInfo(name = Contract.ProductoColumns.NOMBRE)
    val nombre: String,

    @ColumnInfo(name = Contract.ProductoColumns.IMAGEN)
    val imagen: String?,

    @ColumnInfo(name = Contract.ProductoColumns.CANTIDAD)
    val cantidad: Int,

    @ColumnInfo(name = Contract.ProductoColumns.CANTIDAD_MINIMA)
    val cantidadMinima: Int,

    @ColumnInfo(name = Contract.ProductoColumns.HABILITADO)
    val habilitado: Boolean = true,

    @ColumnInfo(name = Contract.ProductoColumns.FECHA_ULTIMA_INTERACCION)
    val fechaUltimaInteraccion: Long
)