package com.example.appalmacen.view.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.appalmacen.controller.SesionController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appalmacen.databinding.ActivityProductoGestionBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.repository.ProductoRepository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductoGestionActivity : BaseActivity() {

    private lateinit var binding: ActivityProductoGestionBinding
    private lateinit var productoRepo: ProductoRepository
    private var fotoUri: Uri? = null
    private var productoId: Int = -1
    private var currentPhotoPath: String? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            fotoUri?.let { uri ->
                Glide.with(this).load(uri).centerCrop().into(binding.ivProdFoto)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            fotoUri = photoURI
            takePhotoLauncher.launch(photoURI)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoGestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseHelper.getInstance(this)
        productoRepo = ProductoRepository(db.productoDAO(), db.interaccionDAO())

        productoId = intent.getIntExtra("PRODUCTO_ID", -1)
        val esAdmin = SesionController.usuarioActivo?.esAdmin ?: false

        if (productoId != -1 && esAdmin) {
            cargarDatosProducto()
            binding.btnDeleteProd.visibility = View.VISIBLE
        } else if (productoId != -1) {
            cargarDatosProducto()
        }

        binding.btnPickProdImage.text = "HACER FOTO CON CÁMARA"
        binding.btnPickProdImage.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.btnSaveProd.setOnClickListener {
            guardarProducto()
        }

        binding.btnDeleteProd.setOnClickListener {
            eliminarProducto()
        }
    }

    private fun cargarDatosProducto() {
        lifecycleScope.launch {
            val producto = productoRepo.getById(productoId)
            producto?.let {
                binding.etProdNombre.setText(it.nombre)
                binding.etProdStock.setText(it.cantidad.toString())
                binding.etProdStockMin.setText(it.cantidadMinima.toString())
                binding.swProdHabilitado.isChecked = it.habilitado
                it.imagen?.let { uriStr ->
                    fotoUri = Uri.parse(uriStr)
                    Glide.with(this@ProductoGestionActivity).load(fotoUri).centerCrop().into(binding.ivProdFoto)
                }
            }
        }
    }

    private fun guardarProducto() {
        val nombre = binding.etProdNombre.text.toString()
        val stock = binding.etProdStock.text.toString().toIntOrNull() ?: 0
        val stockMin = binding.etProdStockMin.text.toString().toIntOrNull() ?: 0
        val habilitado = binding.swProdHabilitado.isChecked

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (productoId == -1) {
                // Verificar si ya existe un producto con el mismo nombre
                val productoExistente = productoRepo.getByNombre(nombre)
                if (productoExistente != null) {
                    Toast.makeText(this@ProductoGestionActivity, "Error: Ya existe un producto con el nombre '$nombre'", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val nuevo = Producto(
                    nombre = nombre,
                    imagen = fotoUri?.toString(),
                    cantidad = stock,
                    cantidadMinima = stockMin,
                    habilitado = habilitado,
                    fechaUltimaInteraccion = System.currentTimeMillis()
                )
                productoRepo.insert(nuevo)
                Toast.makeText(this@ProductoGestionActivity, "Producto creado", Toast.LENGTH_SHORT).show()
            } else {
                val existente = productoRepo.getById(productoId)
                existente?.let {
                    val actualizado = it.copy(
                        nombre = nombre,
                        imagen = fotoUri?.toString(),
                        cantidad = stock,
                        cantidadMinima = stockMin,
                        habilitado = habilitado
                    )
                    productoRepo.update(actualizado)
                    Toast.makeText(this@ProductoGestionActivity, "Producto actualizado", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
    }

    private fun eliminarProducto() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que deseas eliminar permanentemente este producto y todo su historial?")
            .setPositiveButton("ELIMINAR") { _, _ ->
                lifecycleScope.launch {
                    val producto = productoRepo.getById(productoId)
                    producto?.let {
                        productoRepo.delete(it)
                        Toast.makeText(this@ProductoGestionActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }
}
