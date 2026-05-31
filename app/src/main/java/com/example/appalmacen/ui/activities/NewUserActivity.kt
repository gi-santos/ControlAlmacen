package com.example.appalmacen.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.appalmacen.BaseActivity
import com.example.appalmacen.databinding.ActivityNewUserBinding
import com.example.appalmacen.databinding.BottomSheetCamaraBinding
import com.example.appalmacen.data.database.DatabaseHelper
import com.example.appalmacen.data.repository.UsuarioRepository
import com.example.appalmacen.viewmodel.UsuarioViewModel
import com.example.appalmacen.viewmodel.usuario.RegisterViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog

class NewUserActivity : BaseActivity() {

    private lateinit var binding: ActivityNewUserBinding
    private lateinit var viewModel: UsuarioViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) abrirBottomSheetCamara()
            else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewUserBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val db         = DatabaseHelper.getInstance(this)
        val repository = UsuarioRepository(db.usuarioDAO())
        val factory    = RegisterViewModelFactory(repository)
        viewModel      = ViewModelProvider(this, factory)[UsuarioViewModel::class.java]

        observarViewModel()
        configurarListeners()
    }

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

        viewModel.fotoPath.observe(this) { path ->
            if (path != null) {
                val bitmap = BitmapFactory.decodeFile(path)
                binding.ivUsuarioFoto.setImageBitmap(bitmap)
                binding.ivUsuarioFoto.visibility      = View.VISIBLE
                binding.llPlaceholderUsuario.visibility = View.GONE
                binding.btnAbrirCamaraUsuario.text = "Cambiar Foto"
            } else {
                binding.ivUsuarioFoto.visibility        = View.GONE
                binding.llPlaceholderUsuario.visibility = View.VISIBLE
                binding.btnAbrirCamaraUsuario.text      = "Subir Foto"
            }
        }
    }

    private fun configurarListeners() {

        binding.btnCancelarUsuario.setOnClickListener { finish() }


        binding.flUsuarioPreview.setOnClickListener { solicitarPermisoCamara() }
        binding.btnAbrirCamaraUsuario.setOnClickListener { solicitarPermisoCamara() }


        binding.btnGuardarUsuario.setOnClickListener {
            val nombre = binding.etNombreUsuario.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()

            if (validarCampos(nombre, email)) {
                viewModel.registrarUsuario(
                    nombre = nombre,
                    email = email,
                    password = null,
                    esAdmin = false,
                    habilitado = true
                )
            }
        }
    }

    private fun validarCampos(nombre: String, email: String): Boolean {
        return when {
            nombre.isEmpty() -> {
                Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
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
            viewModel.capturarFoto(this) { bottomSheetDialog?.dismiss() }
        }

        sheetBinding.btnCancelarCamara.setOnClickListener { bottomSheetDialog?.dismiss() }
        bottomSheetDialog?.show()
    }
}