package com.example.appalmacen.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.appalmacen.model.database.Contract

@Entity(tableName = Contract.TABLE_PERFILES)
data class Perfil(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Contract.PerfilColumns.ID)
    val id: Int = 0,

    @ColumnInfo(name = Contract.PerfilColumns.NOMBRE)
    val nombre: String,

    @ColumnInfo(name = Contract.PerfilColumns.DESCRIPCION)
    val descripcion: String?
)