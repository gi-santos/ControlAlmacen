package com.example.appalmacen.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.appalmacen.data.database.Contract

@Entity(
    tableName = Contract.TABLE_INTERACCIONES,
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = [Contract.UsuarioColumns.ID],
            childColumns = [Contract.InteraccionColumns.USUARIO_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = [Contract.ProductoColumns.ID],
            childColumns = [Contract.InteraccionColumns.PRODUCTO_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(Contract.InteraccionColumns.USUARIO_ID),
        Index(Contract.InteraccionColumns.PRODUCTO_ID)
    ]
)
data class UsuarioProductoInteraccion(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Contract.InteraccionColumns.ID)
    val id: Int = 0,

    @ColumnInfo(name = Contract.InteraccionColumns.USUARIO_ID)
    val usuarioId: Int,

    @ColumnInfo(name = Contract.InteraccionColumns.PRODUCTO_ID)
    val productoId: Int,

    @ColumnInfo(name = Contract.InteraccionColumns.CANTIDAD_ANTERIOR)
    val cantidadAnterior: Int,

    @ColumnInfo(name = Contract.InteraccionColumns.CANTIDAD_NUEVA)
    val cantidadNueva: Int,

    @ColumnInfo(name = Contract.InteraccionColumns.TIPO_ACCION)
    val tipoAccion: String, // "SUMA" o "RESTA"

    @ColumnInfo(name = Contract.InteraccionColumns.TIMESTAMP)
    val timestamp: Long = System.currentTimeMillis()
)