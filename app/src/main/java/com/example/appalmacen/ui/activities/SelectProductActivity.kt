package com.example.appalmacen.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appalmacen.BaseActivity
import com.example.appalmacen.databinding.ActivitySelectProductBinding
import com.example.appalmacen.data.database.DatabaseHelper
import com.example.appalmacen.data.repository.ProductoRepository
import com.example.appalmacen.ui.adapters.ProductoAdapter
import com.example.appalmacen.viewmodel.producto.ProductoViewModel
import com.example.appalmacen.viewmodel.producto.ProductoViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectProductActivity : BaseActivity() {

    private lateinit var binding: ActivitySelectProductBinding
    private lateinit var adapter: ProductoAdapter
    private lateinit var recientesAdapter: ProductoAdapter

    // Archivo temporal para la captura de la cámara
    private var fotoTemporalFile: File? = null

    private val usuarioId: Int by lazy {
        intent.getIntExtra("user_id", -1)
    }

    private val viewModel: ProductoViewModel by viewModels {
        val db = DatabaseHelper.getInstance(this)
        val repository = ProductoRepository(db.productoDAO(), db.interaccionDAO(), db.albaranDAO())
        ProductoViewModelFactory(repository, usuarioId)
    }


    private val solicitarPermisoCamaraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { esConcedido ->
        if (esConcedido) {
            abrirCamaraNativa()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Registro para procesar el resultado de la cámara
    private val tomarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            fotoTemporalFile?.let { archivoFoto ->
                Toast.makeText(this, "Generando PDF del Albarán...", Toast.LENGTH_SHORT).show()

                // Llamamos a la lógica del ViewModel compartiéndole el contexto de la app
                viewModel.guardarFotoComoPdf(
                    context = applicationContext,
                    fotoFile = archivoFoto,
                    onSuccess = {
                        Toast.makeText(this, "Albarán guardado en archivos y registrado con éxito", Toast.LENGTH_LONG).show()
                    },
                    onError = { error ->
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        } else {
            Toast.makeText(this, "Captura cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarAdapter()
        configurarRecyclerView()
        configurarBuscador()
        configurarCerrarSesion()
        configurarExpandible()
        configurarBotonAlbaran() // <-- Inicializamos el botón
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

    private fun configurarBotonAlbaran() {
        binding.btnAnadirAlbaran.setOnClickListener {
            comprobarPermisosYAbrirCamara()
        }
    }

    private fun comprobarPermisosYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamaraNativa()
        } else {
            solicitarPermisoCamaraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamaraNativa() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val directorioCache = externalCacheDir
        fotoTemporalFile = File.createTempFile("TEMP_ALBARAN_$timeStamp", ".jpg", directorioCache)

        fotoTemporalFile?.let { file ->
            val fotoUri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
            }
            tomarFotoLauncher.launch(intent)
        }
    }

    private fun configurarExpandible() {
        binding.layoutCabeceraProductos.setOnClickListener {
            val estaOculto = binding.cardListaProductos.visibility == View.GONE

            if (estaOculto) {
                binding.cardListaProductos.visibility = View.VISIBLE
                binding.ivFlechaExpandir.animate().rotation(180f).setDuration(200).start()
                binding.rvProductos.post {          // ✅ espera al siguiente frame
                    binding.rvProductos.scrollToPosition(0)
                }
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
                        android.util.Log.d("DEBUG", "Lista llegó: ${lista.size}, card visible: ${binding.cardListaProductos.visibility}")
                        adapter.submitList(lista.filter { it.habilitado })
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