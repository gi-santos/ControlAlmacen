package com.example.appalmacen.view.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.appalmacen.R
import com.example.appalmacen.databinding.ActivityRegisterBinding
import com.example.appalmacen.databinding.ActivityUsuarioBinding
import com.example.appalmacen.databinding.BottomSheetCamaraBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.viewmodel.UsuarioViewModel
import com.example.appalmacen.viewmodel.usuario.RegisterViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsuarioBinding
    private lateinit var viewModel: UsuarioViewModel

    // BottomSheet de cámara (lo reutilizamos si ya está abierto)
    private var bottomSheetDialog: BottomSheetDialog? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) abrirBottomSheetCamara()
            else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db         = DatabaseHelper.getInstance(this)
        val repository = UsuarioRepository(db.usuarioDAO())
        val factory    = RegisterViewModelFactory(repository)
        viewModel      = ViewModelProvider(this, factory)[UsuarioViewModel::class.java]

        configurarDropdowns()
        observarViewModel()
        configurarListeners()
    }

    // ------------------------------------------------------------------
    // Dropdowns esAdmin / habilitado
    // ------------------------------------------------------------------

    private fun configurarDropdowns() {
        val tiposUsuario = listOf("Empleado", "Administrador")
        val adapterTipo  = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposUsuario)
        binding.actvEsAdmin.setAdapter(adapterTipo)
        binding.actvEsAdmin.setText("Empleado", false)

        val estados       = listOf("Habilitado", "Deshabilitado")
        val adapterEstado = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados)
        binding.actvHabilitado.setAdapter(adapterEstado)
        binding.actvHabilitado.setText("Habilitado", false)
    }

    // ------------------------------------------------------------------
    // Observers
    // ------------------------------------------------------------------

    private fun observarViewModel() {

        viewModel.registerState.observe(this) { state ->
            when (state) {
                is UsuarioViewModel.RegisterState.Loading -> {
                    binding.progressBarUsuario.visibility = View.VISIBLE
                    binding.btnGuardarUsuario.isEnabled   = false
                }
                is UsuarioViewModel.RegisterState.Success -> {
                    binding.progressBarUsuario.visibility = View.GONE
                    Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is UsuarioViewModel.RegisterState.Error -> {
                    binding.progressBarUsuario.visibility = View.GONE
                    binding.btnGuardarUsuario.isEnabled   = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressBarUsuario.visibility = View.GONE
                    binding.btnGuardarUsuario.isEnabled   = true
                }
            }
        }

        // Cuando se captura la foto → mostrar miniatura en ivUsuarioFoto
        viewModel.fotoPath.observe(this) { path ->
            if (path != null) {
                val bitmap = BitmapFactory.decodeFile(path)
                binding.ivUsuarioFoto.setImageBitmap(bitmap)
                binding.ivUsuarioFoto.visibility      = View.VISIBLE
                binding.llPlaceholderUsuario.visibility = View.GONE
                // Cambiar texto del botón
                binding.btnAbrirCamaraUsuario.text = "Cambiar Foto"
            } else {
                binding.ivUsuarioFoto.visibility        = View.GONE
                binding.llPlaceholderUsuario.visibility = View.VISIBLE
                binding.btnAbrirCamaraUsuario.text      = "Subir Foto"
            }
        }
    }

    // ------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------

    private fun configurarListeners() {

        // El FrameLayout de preview y el botón hacen lo mismo
        binding.flUsuarioPreview.setOnClickListener { solicitarPermisoCamara() }
        binding.btnAbrirCamaraUsuario.setOnClickListener { solicitarPermisoCamara() }

        binding.btnGuardarUsuario.setOnClickListener {
            val esAdmin    = binding.actvEsAdmin.text.toString() == "Administrador"
            val habilitado = binding.actvHabilitado.text.toString() == "Habilitado"

            viewModel.registrarUsuario(
                nombre     = binding.etNombreUsuario.text.toString(),
                email      = binding.etEmail.text.toString(),
                password   = binding.etPassword.text.toString(),
                esAdmin    = esAdmin,
                habilitado = habilitado
            )
        }
    }

    // ------------------------------------------------------------------
    // Cámara con BottomSheetDialog
    // ------------------------------------------------------------------

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
        // Evitar abrir dos veces
        if (bottomSheetDialog?.isShowing == true) return

        val sheetBinding = BottomSheetCamaraBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(this).apply {
            setContentView(sheetBinding.root)
            setCancelable(false)
        }

        // Iniciar cámara delantera en el PreviewView del BottomSheet
        // Usamos 'this@RegisterActivity' como LifecycleOwner → correcto
        viewModel.iniciarCamara(
            context       = this,
            lifecycleOwner = this,   // Activity es LifecycleOwner
            previewView   = sheetBinding.previewView
        )

        sheetBinding.btnCapturar.setOnClickListener {
            viewModel.capturarFoto(this) {
                // Este callback se ejecuta cuando la foto se guardó con éxito
                bottomSheetDialog?.dismiss()
            }
        }

        sheetBinding.btnCancelarCamara.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }

        bottomSheetDialog?.show()
    }
}