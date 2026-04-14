package com.example.appalmacen.view.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appalmacen.databinding.ActivityRegisterBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.model.repository.UsuarioRepository
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private var fotoUri: Uri? = null
    private var usuarioId: Int = -1

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Si no se puede obtener el permiso persistente, al menos tenemos el URI temporal
            }
            fotoUri = it
            Glide.with(this).load(it).circleCrop().into(binding.ivRegFoto)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseHelper.getInstance(this)
        usuarioRepository = UsuarioRepository(db.usuarioDAO())

        // Comprobar si estamos editando
        usuarioId = intent.getIntExtra("USUARIO_ID", -1)
        if (usuarioId != -1) {
            cargarDatosUsuario()
            binding.btnRegister.text = "GUARDAR CAMBIOS"
        }

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        binding.btnRegister.setOnClickListener {
            registrarUsuario()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            val usuario = usuarioRepository.getById(usuarioId)
            usuario?.let {
                binding.etRegNombre.setText(it.nombre)
                binding.etRegEmail.setText(it.email)
                binding.cbEsAdmin.isChecked = it.esAdmin
                it.foto?.let { uriString ->
                    fotoUri = Uri.parse(uriString)
                    Glide.with(this@RegisterActivity).load(fotoUri).circleCrop().into(binding.ivRegFoto)
                }
                // La contraseña no se carga por seguridad, se deja vacía si no se quiere cambiar
            }
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.etRegNombre.text.toString()
        val email = binding.etRegEmail.text.toString()
        val password = binding.etRegPassword.text.toString()
        val confirmPassword = binding.etRegConfirmPassword.text.toString()
        val esAdmin = binding.cbEsAdmin.isChecked

        if (nombre.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena los campos necesarios", Toast.LENGTH_SHORT).show()
            return
        }

        // Si es nuevo usuario y admin, requiere pass. Si es edición, solo si la rellena.
        if (usuarioId == -1 && esAdmin && password.isEmpty()) {
            Toast.makeText(this, "El administrador requiere contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isNotEmpty() && password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (usuarioId == -1) {
                    // Nuevo Usuario
                    val nuevoUsuario = Usuario(
                        nombre = nombre,
                        email = email,
                        password = if (esAdmin) password else "",
                        foto = fotoUri?.toString(),
                        esAdmin = esAdmin
                    )
                    usuarioRepository.insert(nuevoUsuario)
                    Toast.makeText(this@RegisterActivity, "Usuario creado con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    // Editar Usuario existente
                    val usuarioExistente = usuarioRepository.getById(usuarioId)
                    usuarioExistente?.let {
                        val usuarioActualizado = it.copy(
                            nombre = nombre,
                            email = email,
                            password = if (password.isNotEmpty()) password else it.password,
                            foto = fotoUri?.toString(),
                            esAdmin = esAdmin
                        )
                        usuarioRepository.update(usuarioActualizado)
                        Toast.makeText(this@RegisterActivity, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    }
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
