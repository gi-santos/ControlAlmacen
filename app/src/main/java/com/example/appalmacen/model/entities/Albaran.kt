package com.example.appalmacen.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.appalmacen.data.database.Contract

@Entity(tableName = Contract.TABLE_ALBARANES)
data class Albaran(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Contract.AlbaranColumns.ID)
    val id: Int = 0,

    @ColumnInfo(name = Contract.AlbaranColumns.IMAGEN_PATH)
    val rutaPdf: String,

    @ColumnInfo(name = Contract.AlbaranColumns.FECHA_GUARDADO)
    val fechaGuardado: String


)