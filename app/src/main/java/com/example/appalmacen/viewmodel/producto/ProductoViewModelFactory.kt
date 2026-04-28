package com.example.appalmacen.viewmodel.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appalmacen.data.repository.ProductoRepository

class ProductoViewModelFactory(
    private val repository: ProductoRepository,
    private val usuarioId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductoViewModel(repository, usuarioId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}