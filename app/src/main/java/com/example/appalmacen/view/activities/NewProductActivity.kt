package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels // IMPORTANTE: Para usar 'by viewModels'
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.appalmacen.databinding.ActivityNewProductBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.viewmodel.producto.ProductoViewModel
import com.example.appalmacen.viewmodel.producto.ProductoViewModelFactory


class NewProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewProductBinding

    private val productoViewModel: ProductoViewModel by viewModels {

        val database = DatabaseHelper.getInstance(this)
        ProductoViewModelFactory(database.productoDAO())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdownMenu()
        setupListeners()
    }

    private fun setupDropdownMenu() {
        val opcionesEstado = arrayOf("Activo", "Inactivo", "Agotado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesEstado)
        binding.actvEstado.setAdapter(adapter)
    }

    private fun setupListeners() {
        // Listeners para la imagen (ambos abren la cámara)
        binding.flImagePreview.setOnClickListener { abrirCamara() }
        binding.btnAbrirCamara.setOnClickListener { abrirCamara() }

        // Botón guardar
        binding.btnGuardar.setOnClickListener {
            validarYGuardar()
        }

        binding.btnCancelarProducto.setOnClickListener {
            val intent = Intent(this, SelectProductActivity::class.java)
            // Añadimos flags para evitar crear múltiples instancias si ya existe
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Cerramos la pantalla actual
        }
    }

    private fun abrirCamara() {
        // Aquí puedes añadir el código de CameraX o Intent de imagen más adelante
        Toast.makeText(this, "Abriendo cámara...", Toast.LENGTH_SHORT).show()
    }

    private fun validarYGuardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val cantidadStr = binding.etCantidad.text.toString().trim()
        val cantidadMinStr = binding.etCantidadMinima.text.toString().trim()

        // --- VALIDACIONES DE UI ---
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

        if (hayError) return // Detiene la ejecución si hay errores visuales

        // --- PROCESO DE GUARDADO ---
        val cantidad = cantidadStr.toIntOrNull() ?: 0
        val cantMin = cantidadMinStr.toIntOrNull() ?: 0

        mostrarCargando(true)

        // Llamada al ViewModel
        productoViewModel.insertarProducto(nombre, cantidad, cantMin, null)

        // Pequeño delay para que el usuario vea el feedback de "guardando"
        binding.root.postDelayed({
            mostrarCargando(false)
            Toast.makeText(this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
            finish()
        }, 600)
    }

    private fun mostrarCargando(estaCargando: Boolean) {
        binding.progressBar.isVisible = estaCargando
        binding.btnGuardar.isEnabled = !estaCargando
        // Opcional: bajar la opacidad del formulario mientras carga
        binding.root.alpha = if (estaCargando) 0.6f else 1.0f
    }
}