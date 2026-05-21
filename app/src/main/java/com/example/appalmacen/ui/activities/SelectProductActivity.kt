package com.example.appalmacen.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
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
import com.example.appalmacen.ui.adapters.ProductoAdapter
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
        configurarExpandible() // <-- Nueva función añadida
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
            layoutManager = LinearLayoutManager(this@SelectProductActivity, LinearLayoutManager.VERTICAL, false)
            adapter = recientesAdapter
        }
    }

    private fun configurarBuscador() {
        binding.etBuscarProducto.addTextChangedListener { texto ->
            viewModel.onQueryChanged(texto.toString())
        }
    }


    private fun configurarExpandible() {

        binding.layoutCabeceraProductos.setOnClickListener {
            val estaOculto = binding.cardListaProductos.visibility == View.GONE

            if (estaOculto) {
                binding.cardListaProductos.visibility = View.VISIBLE
                binding.ivFlechaExpandir.animate().rotation(180f).setDuration(200).start()
            } else {
                binding.cardListaProductos.visibility = View.GONE
                binding.ivFlechaExpandir.animate().rotation(0f).setDuration(200).start()
            }
        }
    }

    private fun observarProductos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.productos.collect { lista ->
                        adapter.submitList(lista)
                    }
                }
                launch {
                    viewModel.productosRecientes.collect { listaRecientes ->
                        val listaFiltradaYLimitada = listaRecientes
                            .filter { producto -> producto.habilitado }
                            .take(5)
                        recientesAdapter.submitList(listaFiltradaYLimitada)
                        binding.rvRecientes.isVisible = listaFiltradaYLimitada.isNotEmpty()
                    }
                }
            }
        }
    }

    private fun configurarCerrarSesion() {
        binding.btnCerrarSesion.setOnClickListener {
            val intent = Intent(this, SelectUserActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
}