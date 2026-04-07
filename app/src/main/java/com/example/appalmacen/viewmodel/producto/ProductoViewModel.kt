package com.example.appalmacen.viewmodel.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appalmacen.model.dao.ProductoDAO
import com.example.appalmacen.model.entities.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductoViewModel(private val dao: ProductoDAO) : ViewModel() {

    fun insertarProducto(nombre: String, cantidad: Int, cantidadMinima: Int, imagen: String?) {
        val nuevoProducto = Producto(
            nombre = nombre,
            imagen = imagen,
            cantidad = cantidad,
            cantidadMinima = cantidadMinima,
            fechaUltimaInteraccion = System.currentTimeMillis() // Fecha actual en milisegundos
        )

        // Usamos viewModelScope para que la corrutina muera si el ViewModel se destruye
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(nuevoProducto)
        }
    }
}