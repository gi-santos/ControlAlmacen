package com.example.appalmacen.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appalmacen.data.dao.AlbaranDAO
import com.example.appalmacen.data.dao.InteraccionDAO
import com.example.appalmacen.data.dao.PerfilDAO
import com.example.appalmacen.data.dao.ProductoDAO
import com.example.appalmacen.data.dao.UsuarioDAO
import com.example.appalmacen.model.entities.*

@Database(
    entities = [
        Usuario::class,
        Producto::class,
        Albaran::class,
        Perfil::class,
        UsuarioProductoInteraccion::class
    ],
    version = 4,
    exportSchema = false
)
abstract class DatabaseHelper : RoomDatabase() {

    abstract fun usuarioDAO(): UsuarioDAO
    abstract fun productoDAO(): ProductoDAO
    abstract fun albaranDAO(): AlbaranDAO
    abstract fun perfilDAO(): PerfilDAO
    abstract fun interaccionDAO(): InteraccionDAO



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
