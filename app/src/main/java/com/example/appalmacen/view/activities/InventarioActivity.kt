package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivityInventarioBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.repository.ProductoRepository
import com.example.appalmacen.view.adapters.InventarioAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventarioBinding
    private lateinit var productoRepo: ProductoRepository
    private lateinit var adapter: InventarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarInventario)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarInventario.setNavigationOnClickListener { finish() }

        val db = DatabaseHelper.getInstance(this)
        productoRepo = ProductoRepository(db.productoDAO(), db.interaccionDAO())

        setupRecyclerView()
        setupFilters()
        setupSearch()
        observeProductos()
    }

    private fun setupSearch() {
        binding.etSearchInventario.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                observeProductos()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedChangeListener { _, _ ->
            observeProductos()
        }
    }

    private fun setupRecyclerView() {
        adapter = InventarioAdapter(
            productos = emptyList(),
            onItemClick = { producto ->
                // Al hacer clic, abrimos la gestión del producto
                val intent = Intent(this, ProductoGestionActivity::class.java)
                intent.putExtra("PRODUCTO_ID", producto.id)
                startActivity(intent)
            },
            onItemLongClick = { producto ->
                // Al hacer clic largo, si es admin, permitir borrar o deshabilitar
                val esAdmin = SesionController.usuarioActivo?.esAdmin ?: false
                if (esAdmin) {
                    mostrarOpcionesProducto(producto)
                }
            }
        )
        binding.rvInventario.layoutManager = LinearLayoutManager(this)
        binding.rvInventario.adapter = adapter
    }

    private fun mostrarOpcionesProducto(producto: Producto) {
        val opciones = arrayOf(
            if (producto.habilitado) "Deshabilitar Producto" else "Habilitar Producto",
            "Eliminar Producto"
        )

        AlertDialog.Builder(this)
            .setTitle(producto.nombre)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> toggleHabilitado(producto)
                    1 -> confirmarBorrado(producto)
                }
            }
            .show()
    }

    private fun toggleHabilitado(producto: Producto) {
        lifecycleScope.launch {
            val nuevoEstado = !producto.habilitado
            productoRepo.setHabilitado(producto.id, nuevoEstado)
            val mensaje = if (nuevoEstado) "Producto habilitado" else "Producto deshabilitado"
            Toast.makeText(this@InventarioActivity, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarBorrado(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que deseas eliminar permanentemente '${producto.nombre}'?")
            .setPositiveButton("ELIMINAR") { _, _ ->
                lifecycleScope.launch {
                    productoRepo.delete(producto)
                    Toast.makeText(this@InventarioActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private var observeJob: kotlinx.coroutines.Job? = null

    private fun observeProductos() {
        observeJob?.cancel()
        observeJob = lifecycleScope.launch {
            productoRepo.allProductos.collectLatest { lista ->
                val query = binding.etSearchInventario.text.toString().trim().lowercase()
                
                // 1. Filtrar por búsqueda si hay texto
                var filtrada = if (query.isNotEmpty()) {
                    lista.filter { it.nombre.lowercase().contains(query) }
                } else {
                    lista
                }

                // 2. Filtrar por chip seleccionado
                filtrada = when (binding.chipGroupFilters.checkedChipId) {
                    com.example.appalmacen.R.id.chipLowStock -> {
                        filtrada.filter { it.cantidad <= it.cantidadMinima }
                    }
                    com.example.appalmacen.R.id.chipDisabled -> {
                        filtrada.filter { !it.habilitado }
                    }
                    else -> filtrada // chipAll
                }

                adapter.updateList(filtrada)
                
                // Actualizar título con conteo
                supportActionBar?.title = "Inventario (${filtrada.size})"
            }
        }
    }
}
