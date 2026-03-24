package com.example.appalmacen.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import com.example.appalmacen.model.database.Contract

@Entity(
    tableName = Contract.TABLE_USUARIOS,
    foreignKeys = [
        ForeignKey(
            entity = Perfil::class,
            parentColumns = [Contract.PerfilColumns.ID],
            childColumns = ["perfil_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Contract.UsuarioColumns.ID)
    val id: Int = 0,

    @ColumnInfo(name = "perfil_id", index = true)
    val perfilId: Int? = null,

    @ColumnInfo(name = Contract.UsuarioColumns.NOMBRE)
    val nombre: String,

    @ColumnInfo(name = Contract.UsuarioColumns.FOTO)
    val foto: String?,

    @ColumnInfo(name = Contract.UsuarioColumns.EMAIL)
    val email: String,

    @ColumnInfo(name = Contract.UsuarioColumns.PASSWORD)
    val password: String,

    @ColumnInfo(name = Contract.UsuarioColumns.ES_ADMIN)
    val esAdmin: Boolean = false,

    @ColumnInfo(name = Contract.UsuarioColumns.HABILITADO)
    val habilitado: Boolean = true
)