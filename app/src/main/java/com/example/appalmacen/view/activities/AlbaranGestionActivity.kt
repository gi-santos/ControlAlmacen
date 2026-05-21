package com.example.appalmacen.view.activities

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivityAlbaranGestionBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Albaran
import com.example.appalmacen.model.repository.AlbaranRepository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlbaranGestionActivity : BaseActivity() {

    private lateinit var binding: ActivityAlbaranGestionBinding
    private lateinit var albaranRepo: AlbaranRepository
    private var fotoUri: Uri? = null
    private var albaranId: Int = -1
    private var currentPhotoPath: String? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            fotoUri?.let { uri ->
                Glide.with(this).load(uri).centerInside().into(binding.ivAlbaranFoto)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("ALBARAN_${timeStamp}_", ".jpg", storageDir).apply {
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
        binding = ActivityAlbaranGestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseHelper.getInstance(this)
        albaranRepo = AlbaranRepository(db.albaranDAO())

        albaranId = intent.getIntExtra("ALBARAN_ID", -1)
        val esAdmin = SesionController.usuarioActivo?.esAdmin ?: false

        if (albaranId == -1) {
            binding.tvTituloAlbaran.text = "Nuevo Albarán"
        } else {
            binding.tvTituloAlbaran.text = "Editar Albarán"
            cargarDatosAlbaran()
            if (esAdmin) {
                binding.btnDeleteAlbaran.visibility = View.VISIBLE
            }
        }

        binding.btnPickAlbaranImage.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.btnSaveAlbaran.setOnClickListener {
            guardarAlbaran()
        }

        binding.btnDeleteAlbaran.setOnClickListener {
            eliminarAlbaran()
        }
    }

    private fun cargarDatosAlbaran() {
        lifecycleScope.launch {
            val albaran = albaranRepo.getById(albaranId)
            albaran?.let {
                binding.etAlbaranCif.setText(it.cif)
                binding.etAlbaranProveedor.setText(it.nombreProveedor)
                binding.etAlbaranImporte.setText(it.importe.toString())
                binding.swAlbaranPagado.isChecked = it.pagado
                it.imagenPath?.let { uriStr ->
                    fotoUri = Uri.parse(uriStr)
                    Glide.with(this@AlbaranGestionActivity).load(fotoUri).centerInside().into(binding.ivAlbaranFoto)
                }
            }
        }
    }

    private fun guardarAlbaran() {
        val cif = binding.etAlbaranCif.text.toString()
        val proveedor = binding.etAlbaranProveedor.text.toString()
        val importe = binding.etAlbaranImporte.text.toString().toDoubleOrNull() ?: 0.0
        val pagado = binding.swAlbaranPagado.isChecked

        if (cif.isEmpty() || proveedor.isEmpty()) {
            Toast.makeText(this, "CIF y Proveedor son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (albaranId == -1) {
                val nuevo = Albaran(
                    cif = cif,
                    nombreProveedor = proveedor,
                    importe = importe,
                    pagado = pagado,
                    fechaPago = if (pagado) System.currentTimeMillis() else null,
                    fecha = System.currentTimeMillis(),
                    imagenPath = fotoUri?.toString()
                )
                albaranRepo.insert(nuevo)
                Toast.makeText(this@AlbaranGestionActivity, "Albarán creado", Toast.LENGTH_SHORT).show()
            } else {
                val existente = albaranRepo.getById(albaranId)
                existente?.let {
                    val actualizado = it.copy(
                        cif = cif,
                        nombreProveedor = proveedor,
                        importe = importe,
                        pagado = pagado,
                        fechaPago = if (pagado && !it.pagado) System.currentTimeMillis() else if (!pagado) null else it.fechaPago,
                        imagenPath = fotoUri?.toString()
                    )
                    albaranRepo.update(actualizado)
                    Toast.makeText(this@AlbaranGestionActivity, "Albarán actualizado", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
    }

    private fun eliminarAlbaran() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Albarán")
            .setMessage("¿Estás seguro de que deseas eliminar este albarán?")
            .setPositiveButton("ELIMINAR") { _, _ ->
                lifecycleScope.launch {
                    val albaran = albaranRepo.getById(albaranId)
                    albaran?.let {
                        albaranRepo.delete(it)
                        Toast.makeText(this@AlbaranGestionActivity, "Albarán eliminado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }
}
