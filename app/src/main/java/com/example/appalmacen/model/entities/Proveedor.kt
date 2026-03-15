package com.example.appalmacen.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.appalmacen.model.database.Contract

@Entity(tableName = Contract.TABLE_PROVEEDORES)
data class Proveedor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Contract.ProveedorColumns.ID)
    val id: Int = 0,

    @ColumnInfo(name = Contract.ProveedorColumns.NOMBRE)
    val nombre: String,

    @ColumnInfo(name = Contract.ProveedorColumns.CIF)
    val cif: String,

    @ColumnInfo(name = Contract.ProveedorColumns.TELEFONO)
    val telefono: String?,

    @ColumnInfo(name = Contract.ProveedorColumns.EMAIL)
    val email: String?
)