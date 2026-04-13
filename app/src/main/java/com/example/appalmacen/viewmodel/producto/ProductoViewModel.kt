package com.example.appalmacen.viewmodel.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.repository.ProductoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ProductoViewModel(
    private val repository: ProductoRepository,
    private val usuarioId: Int
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val productos: StateFlow<List<Producto>> = _query
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getHabilitados()
            else repository.buscar(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    fun sumarCantidad(producto: Producto) {
        viewModelScope.launch {
            repository.actualizarCantidad(
                usuarioId = usuarioId,
                producto = producto,
                nuevaCantidad = producto.cantidad + 1
            )
        }
    }

    fun restarCantidad(producto: Producto) {
        if (producto.cantidad <= 0) return
        viewModelScope.launch {
            repository.actualizarCantidad(
                usuarioId = usuarioId,
                producto = producto,
                nuevaCantidad = producto.cantidad - 1
            )
        }
    }

    // Mantener función existente para insertar productos nuevos
    fun insertarProducto(nombre: String, cantidad: Int, cantidadMinima: Int, imagen: String?) {
        val nuevoProducto = Producto(
            nombre = nombre,
            imagen = imagen,
            cantidad = cantidad,
            cantidadMinima = cantidadMinima,
            fechaUltimaInteraccion = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.getHabilitados() // acceso al dao a través del repo
        }
    }

    class Factory(
        private val repository: ProductoRepository,
        private val usuarioId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductoViewModel::class.java)) {
                return ProductoViewModel(repository, usuarioId) as T
            }
            throw IllegalArgumentException("ViewModel desconocido")
        }
    }
}