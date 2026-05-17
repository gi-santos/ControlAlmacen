package com.example.appalmacen.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appalmacen.databinding.ActivitySelectProductBinding
import com.example.appalmacen.data.database.DatabaseHelper
import com.example.appalmacen.data.repository.ProductoRepository
import com.example.appalmacen.utils.PreferencesManager
import com.example.appalmacen.ui.adapters.ProductoAdapter
import com.example.appalmacen.ui.adapters.RecientesAdapter
import com.example.appalmacen.viewmodel.producto.ProductoViewModel
import com.example.appalmacen.viewmodel.producto.ProductoViewModelFactory
import kotlinx.coroutines.launch

class SelectProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectProductBinding
    private lateinit var adapter: ProductoAdapter

    private lateinit var recientesAdapter: ProductoAdapter

    private val usuarioId: Int by lazy {
        intent.getIntExtra("user_id", -1)
    }

    private val viewModel: ProductoViewModel by viewModels {
        val db = DatabaseHelper.getInstance(this)
        val repository = ProductoRepository(db.productoDAO(), db.interaccionDAO())
        ProductoViewModelFactory(repository, usuarioId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarAdapter()
        configurarRecyclerView()
        configurarBuscador()
        configurarCerrarSesion()
        observarProductos()
    }

    private fun configurarAdapter() {
        adapter = ProductoAdapter(
            onSumar = { producto -> viewModel.sumarCantidad(producto) },
            onRestar = { producto -> viewModel.restarCantidad(producto) }
        )
        recientesAdapter = ProductoAdapter(
            onSumar = { producto -> viewModel.sumarCantidad(producto) },
            onRestar = { producto -> viewModel.restarCantidad(producto) }
        )
    }

    private fun configurarRecyclerView() {
        binding.rvProductos.apply {
            layoutManager = LinearLayoutManager(this@SelectProductActivity)
            adapter = this@SelectProductActivity.adapter
            clipToPadding = false
            clipChildren = false
            isNestedScrollingEnabled = false
        }
        binding.rvRecientes.apply {
            layoutManager = LinearLayoutManager(this@SelectProductActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = recientesAdapter
            // Esto quita el error "No adapter attached"
        }
    }

    private fun configurarBuscador() {
        binding.etBuscarProducto.addTextChangedListener { texto ->
            viewModel.onQueryChanged(texto.toString())
        }
    }

    private fun observarProductos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Flujo de la lista completa
                launch {
                    viewModel.productos.collect { lista ->
                        adapter.submitList(lista)
                    }
                }


                launch {
                    viewModel.productosRecientes.collect { listaRecientes ->
                        recientesAdapter.submitList(listaRecientes)
                        // Mostrar/Ocultar el título de "Vistos recientemente" si la lista está vacía
                        binding.rvRecientes.isVisible = listaRecientes.isNotEmpty()
                    }
                }
            }
        }
    }

    private fun configurarCerrarSesion() {
        binding.btnCerrarSesion.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Quieres terminar la jornada?")
                .setPositiveButton("Salir") { _, _ ->
                    // Usamos tu clase en lugar de SharedPreferences manual
                    val prefManager = PreferencesManager(this)
                    prefManager.clearSession()

                    val intent = Intent(this, SelectUserActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}