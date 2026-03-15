package com.example.appalmacen.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.appalmacen.model.database.Contract

@Entity(tableName = Contract.TABLE_ALBARANES)
data class Albaran(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Contract.AlbaranColumns.ID)
    val id: Int = 0,

    @ColumnInfo(name = Contract.AlbaranColumns.CIF)
    val cif: String,

    @ColumnInfo(name = Contract.AlbaranColumns.NOMBRE_PROVEEDOR)
    val nombreProveedor: String,

    @ColumnInfo(name = Contract.AlbaranColumns.IMPORTE)
    val importe: Double,

    @ColumnInfo(name = Contract.AlbaranColumns.PAGADO)
    val pagado: Boolean = false,

    @ColumnInfo(name = Contract.AlbaranColumns.FECHA_PAGO)
    val fechaPago: Long?,

    @ColumnInfo(name = Contract.AlbaranColumns.FECHA)
    val fecha: Long,

    @ColumnInfo(name = Contract.AlbaranColumns.IMAGEN_PATH)
    val imagenPath: String?
)