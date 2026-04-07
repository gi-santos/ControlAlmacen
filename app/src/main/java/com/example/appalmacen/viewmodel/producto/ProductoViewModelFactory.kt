package com.example.appalmacen.viewmodel.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appalmacen.model.dao.ProductoDAO

class ProductoViewModelFactory(private val dao: ProductoDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductoViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}