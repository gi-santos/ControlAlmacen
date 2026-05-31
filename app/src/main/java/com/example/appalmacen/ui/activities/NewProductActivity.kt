package com.example.appalmacen.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.appalmacen.BaseActivity
import com.example.appalmacen.databinding.ActivityNewProductBinding
import com.example.appalmacen.databinding.BottomSheetCamaraBinding
import com.example.appalmacen.AlmacenApp
import com.example.appalmacen.viewmodel.producto.ProductoViewModel
import com.example.appalmacen.viewmodel.producto.ProductoViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class NewProductActivity : BaseActivity() {

    private lateinit var binding: ActivityNewProductBinding
    private var bottomSheetDialog: BottomSheetDialog? = null



    private val viewModel: ProductoViewModel by viewModels {
        val app = application as AlmacenApp
        ProductoViewModelFactory(app.productoRepository, usuarioId = -1)
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) abrirBottomSheetCamara()
            else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
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
                    finish()
                }
            }
        }


        viewModel.fotoPath.observe(this) { path ->
            if (path != null) {
                val bitmap = BitmapFactory.decodeFile(path)
                binding.ivProductoImagen.setImageBitmap(bitmap)
                binding.ivProductoImagen.isVisible = true
                binding.llPlaceholder.isVisible = false
                binding.btnAbrirCamara.text = "Cambiar Foto"
            } else {
                binding.ivProductoImagen.isVisible = false
                binding.llPlaceholder.isVisible = true
                binding.btnAbrirCamara.text = "Abrir Cámara"
            }
        }
    }

    private fun setupListeners() {
        binding.flImagePreview.setOnClickListener { solicitarPermisoCamara() }
        binding.btnAbrirCamara.setOnClickListener { solicitarPermisoCamara() }

        binding.btnGuardar.setOnClickListener { validarYGuardar() }

        binding.btnCancelarProducto.setOnClickListener {
            finish()
        }
    }


    private fun solicitarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            abrirBottomSheetCamara()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirBottomSheetCamara() {
        if (bottomSheetDialog?.isShowing == true) return

        val sheetBinding = BottomSheetCamaraBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(this).apply {
            setContentView(sheetBinding.root)
            setCancelable(false)
        }

        viewModel.iniciarCamara(this, this, sheetBinding.previewView)

        sheetBinding.btnCapturar.setOnClickListener {
            viewModel.capturarFoto(this) {
                bottomSheetDialog?.dismiss()
            }
        }

        sheetBinding.btnCancelarCamara.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }

        bottomSheetDialog?.show()
    }



    private fun setupDropdownMenu() {
        val opcionesEstado = arrayOf("Activo", "Inactivo", "Agotado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesEstado)
        binding.actvEstado.setAdapter(adapter)
    }

    private fun validarYGuardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val cantidadStr = binding.etCantidad.text.toString().trim()
        val cantidadMinStr = binding.etCantidadMinima.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.tilNombre.error = "El nombre es obligatorio"
            return
        }

        val cantidad = cantidadStr.toIntOrNull() ?: 0
        val cantMin = cantidadMinStr.toIntOrNull() ?: 0
        val fotoPath = viewModel.fotoPath.value // Obtenemos el path de la foto tomada

        mostrarCargando(true)
        viewModel.insertarProducto(nombre, cantidad, cantMin, fotoPath)
    }

    private fun mostrarCargando(estaCargando: Boolean) {
        binding.progressBar.isVisible = estaCargando
        binding.btnGuardar.isEnabled = !estaCargando
        binding.root.alpha = if (estaCargando) 0.6f else 1.0f
    }
}