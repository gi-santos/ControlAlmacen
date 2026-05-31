package com.example.appalmacen


import android.app.Application
import android.content.Intent
import com.example.appalmacen.data.database.DatabaseHelper
import com.example.appalmacen.data.repository.InteraccionRepository
import com.example.appalmacen.data.repository.ProductoRepository
import com.example.appalmacen.ui.activities.SelectUserActivity

class AlmacenApp : Application() {

    val database: DatabaseHelper by lazy {
        DatabaseHelper.getInstance(this)
    }

    val productoRepository: ProductoRepository by lazy {

        ProductoRepository(
            database.productoDAO(),
            database.interaccionDAO(),
            database.albaranDAO()
        )
    }

    val interaccionRepository: InteraccionRepository by lazy {
        InteraccionRepository(database.interaccionDAO())
    }

    override fun onCreate() {
        super.onCreate()

        InactivityManager.init {
            val intent = Intent(this, SelectUserActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
    // ─────────────────────────────────────────────────────────────────────────
}