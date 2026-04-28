package com.example.appalmacen.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.appalmacen.databinding.ActivityNewProductBinding
import com.example.appalmacen.model.AlmacenApp
import com.example.appalmacen.viewmodel.producto.ProductoViewModel
import com.example.appalmacen.viewmodel.producto.ProductoViewModelFactory
import kotlinx.coroutines.launch

class NewProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewProductBinding

    // NewProduct no necesita usuarioId porque solo inserta, no registra interacción
    private val viewModel: ProductoViewModel by viewModels {
        val app = application as AlmacenApp
        ProductoViewModelFactory(app.productoRepository, usuarioId = -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdownMenu()
        setupListeners()
        observarEventos()

    }

    private fun observarEventos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventos.collect {
                    mostrarCargando(false)
                    Toast.makeText(this@NewProductActivity, "Producto guardado", Toast.LENGTH_SHORT).show()
                    finish() // Esto cierra la pantalla y vuelve a SelectProductActivity
                }
            }
        }
    }

    private fun setupDropdownMenu() {
        val opcionesEstado = arrayOf("Activo", "Inactivo", "Agotado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesEstado)
        binding.actvEstado.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.flImagePreview.setOnClickListener { abrirCamara() }
        binding.btnAbrirCamara.setOnClickListener { abrirCamara() }

        binding.btnGuardar.setOnClickListener {
            validarYGuardar()
        }

        binding.btnCancelarProducto.setOnClickListener {
            val intent = Intent(this, SelectProductActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun abrirCamara() {
        Toast.makeText(this, "Abriendo cámara...", Toast.LENGTH_SHORT).show()
    }

    private fun validarYGuardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val cantidadStr = binding.etCantidad.text.toString().trim()
        val cantidadMinStr = binding.etCantidadMinima.text.toString().trim()

        var hayError = false

        if (nombre.isEmpty()) {
            binding.tilNombre.error = "El nombre es obligatorio"
            hayError = true
        } else {
            binding.tilNombre.error = null
        }

        if (cantidadStr.isEmpty()) {
            binding.tilCantidad.error = "Requerido"
            hayError = true
        } else {
            binding.tilCantidad.error = null
        }

        if (hayError) return

        val cantidad = cantidadStr.toIntOrNull() ?: 0
        val cantMin = cantidadMinStr.toIntOrNull() ?: 0

        mostrarCargando(true)

        viewModel.insertarProducto(nombre, cantidad, cantMin, null)

    }

    private fun mostrarCargando(estaCargando: Boolean) {
        binding.progressBar.isVisible = estaCargando
        binding.btnGuardar.isEnabled = !estaCargando
        binding.root.alpha = if (estaCargando) 0.6f else 1.0f
    }
}