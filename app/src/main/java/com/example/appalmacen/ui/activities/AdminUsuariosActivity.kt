package com.example.appalmacen.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appalmacen.BaseActivity
import com.example.appalmacen.R
import com.example.appalmacen.ui.adapters.AdminUsuariosAdapter
import com.example.appalmacen.viewmodel.admin.AdminViewModel
import kotlinx.coroutines.launch

class AdminUsuariosActivity : BaseActivity() {

    private val viewModel: AdminViewModel by viewModels()

    private lateinit var rvUsuarios: RecyclerView
    private lateinit var etBuscar: EditText
    private lateinit var tvTotalUsuarios: TextView

    private lateinit var btnCerrarSesion: LinearLayout
    private lateinit var btnNuevoUsuario: com.google.android.material.button.MaterialButton
    private lateinit var layoutVacioUsuarios: LinearLayout

    private val adapter = AdminUsuariosAdapter { usuario, nuevoEstado ->
        viewModel.cambiarEstadoUsuario(usuario, nuevoEstado)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_usuarios)


        initViews()
        collectFlows()
        setupRecyclerView()
        setupBuscador()
        setupBotones()
    }

    private fun initViews() {
        rvUsuarios          = findViewById(R.id.rvUsuarios)
        etBuscar            = findViewById(R.id.etBuscarUsuario)
        tvTotalUsuarios     = findViewById(R.id.tvTotalUsuarios)
        btnCerrarSesion     = findViewById(R.id.btnCerrarSesion)
        btnNuevoUsuario     = findViewById(R.id.btnNuevoUsuario)
        layoutVacioUsuarios = findViewById(R.id.layoutVacioUsuarios)
    }

    private fun setupRecyclerView() {
        rvUsuarios.layoutManager = LinearLayoutManager(this)
        rvUsuarios.adapter = adapter
    }

    private fun setupBuscador() {
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setQueryUsuario(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBotones() {
        btnCerrarSesion.setOnClickListener {
            viewModel.cerrarSesion()
            val intent = Intent(this, AdminProductosActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        btnNuevoUsuario.setOnClickListener {
            val intent = Intent(this, NewUserActivity::class.java)
            startActivity(intent)
        }
    }

    private fun collectFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.usuariosFiltrados.collect { usuarios ->
                    adapter.submitList(usuarios)
                    val hayResultados = usuarios.isNotEmpty()
                    rvUsuarios.isVisible          = hayResultados
                    layoutVacioUsuarios.isVisible = !hayResultados
                    tvTotalUsuarios.text          = "${usuarios.size} usuarios"
                }
            }
        }
    }
}