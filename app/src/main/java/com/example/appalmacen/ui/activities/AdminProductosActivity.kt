package com.example.appalmacen.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appalmacen.BaseActivity
import com.example.appalmacen.R
import com.example.appalmacen.ui.adapters.AdminProductosAdapter
import com.example.appalmacen.viewmodel.admin.AdminViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminProductosActivity : BaseActivity() {

    private val viewModel: AdminViewModel by viewModels()

    private lateinit var rvProductos: RecyclerView
    private lateinit var etBuscar: EditText
    private lateinit var btnVerUsuarios: LinearLayout
    private lateinit var btnCerrarSesion: LinearLayout
    private lateinit var btnNuevoProducto: com.google.android.material.button.MaterialButton
    private lateinit var layoutVacio: LinearLayout


    private val adapter = AdminProductosAdapter { producto, nuevoEstado ->
        viewModel.cambiarEstadoProducto(producto, nuevoEstado)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_productos)


        initViews()
        setupRecyclerView()
        setupBuscador()
        setupBotones()
        observeViewModel()
    }

    private fun initViews() {
        rvProductos      = findViewById(R.id.rvProductos)
        etBuscar         = findViewById(R.id.etBuscarProducto)
        btnVerUsuarios   = findViewById(R.id.btnVerUsuarios)
        btnCerrarSesion  = findViewById(R.id.btnCerrarSesion)
        btnNuevoProducto = findViewById(R.id.btnNuevoProducto)
        layoutVacio      = findViewById(R.id.layoutVacio)

    }

    private fun setupRecyclerView() {
        rvProductos.layoutManager = LinearLayoutManager(this)
        rvProductos.adapter = adapter
    }

    private fun setupBuscador() {
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrarProductos(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBotones() {
        btnVerUsuarios.setOnClickListener {
            startActivity(Intent(this, AdminUsuariosActivity::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            viewModel.cerrarSesion()
            startActivity(Intent(this, SelectUserActivity::class.java))
            finishAffinity()
        }

        btnNuevoProducto.setOnClickListener {
            val intent = Intent(this, NewProductActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        // Observa la lista total de productos (filtrados únicamente por el buscador de texto)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productosFiltrados.collectLatest { productos ->
                    adapter.submitList(productos)

                    val hayResultados = !productos.isNullOrEmpty()
                    rvProductos.visibility = if (hayResultados) View.VISIBLE else View.GONE
                    layoutVacio.visibility = if (hayResultados) View.GONE else View.VISIBLE
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalUsuarios.collectLatest { total ->
                    findViewById<TextView>(R.id.tvContadorUsuarios).text =
                        "$total usuarios registrados en el sistema"
                }
            }
        }
    }
}