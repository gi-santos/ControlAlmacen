package com.example.appalmacen

import android.app.Application
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.repository.ProductoRepository // y los demás que necesites

class AlmacenApp : Application() {

    val database: DatabaseHelper by lazy {
        DatabaseHelper.getInstance(this)
    }

    // Un repository por cada DAO que necesites
    val productoRepository: ProductoRepository by lazy {
        ProductoRepository(database.productoDAO())
    }
}