package com.example.appalmacen.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appalmacen.model.dao.*
import com.example.appalmacen.model.entities.*

@Database(
    entities = [
        Usuario::class,
        Producto::class,
        Albaran::class,
        Perfil::class,
        Proveedor::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DatabaseHelper : RoomDatabase() {

    abstract fun usuarioDAO(): UsuarioDAO
    abstract fun productoDAO(): ProductoDAO
    abstract fun albaranDAO(): AlbaranDAO
    abstract fun perfilDAO(): PerfilDAO
    abstract fun proveedorDAO(): ProveedorDAO

    companion object {
        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseHelper::class.java,
                    "app_almacen.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
