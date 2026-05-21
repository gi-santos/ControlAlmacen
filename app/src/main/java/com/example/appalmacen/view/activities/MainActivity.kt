package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivityMainBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.repository.ProductoRepository
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager
import com.example.appalmacen.view.adapters.ProductoAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.appalmacen.R
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sesionController: SesionController
    private lateinit var productoRepo: ProductoRepository
    private lateinit var adapter: ProductoAdapter
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseHelper.getInstance(this)
        val usuarioRepo = UsuarioRepository(db.usuarioDAO())
        productoRepo = ProductoRepository(db.productoDAO(), db.interaccionDAO())
        
        val prefManager = PreferencesManager(this)
        sesionController = SesionController(usuarioRepo, prefManager)

        setupRecyclerView()
        setupSearch()
        setupNavigationDrawer()

        // Saludo personalizado y carga inicial
        SesionController.usuarioActivo?.let { usuario ->
            binding.tvWelcome.text = "¡Hola, ${usuario.nombre}!"
            binding.fabAddProducto.visibility = View.VISIBLE
            cargarInteraccionesRecientes(usuario.id)
            actualizarUIHeader(usuario)
        }

        binding.fabAddProducto.setOnClickListener {
            startActivity(Intent(this, ProductoGestionActivity::class.java))
        }
    }

    private fun setupNavigationDrawer() {
        setSupportActionBar(binding.toolbar)
        
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = binding.navView
        val menu = navView.menu

        // Control de visibilidad según perfil
        val esAdmin = SesionController.usuarioActivo?.esAdmin ?: false
        menu.findItem(R.id.nav_gestion_usuarios).isVisible = esAdmin
        menu.findItem(R.id.nav_configuracion).isVisible = esAdmin

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inventario -> {
                    startActivity(Intent(this, InventarioActivity::class.java))
                }
                R.id.nav_albaranes -> {
                    startActivity(Intent(this, AlbaranesActivity::class.java))
                }
                R.id.nav_gestion_usuarios -> {
                    startActivity(Intent(this, UsuarioGestionActivity::class.java))
                }
                R.id.nav_configuracion -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                realizarLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun actualizarUIHeader(usuario: com.example.appalmacen.model.entities.Usuario) {
        val headerView = binding.navView.getHeaderView(0)
        val tvNombre = headerView.findViewById<android.widget.TextView>(com.example.appalmacen.R.id.tvHeaderNombre)
        val tvEmail = headerView.findViewById<android.widget.TextView>(com.example.appalmacen.R.id.tvHeaderEmail)
        val ivFoto = headerView.findViewById<android.widget.ImageView>(com.example.appalmacen.R.id.ivHeaderFoto)

        tvNombre.text = usuario.nombre
        tvEmail.text = usuario.email
        
        usuario.foto?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(ivFoto)
        }
    }

    private fun realizarLogout() {
        sesionController.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupRecyclerView() {
        adapter = ProductoAdapter(
            productos = emptyList(),
            onUpdateStock = { producto, cambio ->
                lifecycleScope.launch {
                    val nuevaCantidad = (producto.cantidad + cambio).coerceAtLeast(0)
                    val productoActualizado = producto.copy(cantidad = nuevaCantidad)
                    productoRepo.update(productoActualizado)
                    
                    // Al actualizar stock, también lo registramos como interacción
                    SesionController.usuarioActivo?.let { usuario ->
                        productoRepo.registrarInteraccion(usuario.id, producto.id)
                    }
                }
            },
            onClick = { producto ->
                SesionController.usuarioActivo?.let { usuario ->
                    lifecycleScope.launch {
                        productoRepo.registrarInteraccion(usuario.id, producto.id)
                        if (binding.etSearch.text.isEmpty()) {
                            cargarInteraccionesRecientes(usuario.id)
                        }
                    }
                }
            },
            onLongClick = { producto ->
                if (SesionController.usuarioActivo?.esAdmin == true) {
                    mostrarOpcionesProducto(producto)
                }
            }
        )
        binding.rvRecientes.layoutManager = LinearLayoutManager(this)
        binding.rvRecientes.adapter = adapter
    }

    private fun mostrarOpcionesProducto(producto: Producto) {
        val opciones = arrayOf(
            if (producto.habilitado) "Deshabilitar Producto" else "Habilitar Producto",
            "Eliminar Producto"
        )

        AlertDialog.Builder(this)
            .setTitle(producto.nombre)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> toggleHabilitado(producto)
                    1 -> confirmarBorrado(producto)
                }
            }
            .show()
    }

    private fun toggleHabilitado(producto: Producto) {
        lifecycleScope.launch {
            val nuevoEstado = !producto.habilitado
            productoRepo.setHabilitado(producto.id, nuevoEstado)
            val mensaje = if (nuevoEstado) "Producto habilitado" else "Producto deshabilitado"
            Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarBorrado(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que deseas eliminar permanentemente '${producto.nombre}'?")
            .setPositiveButton("ELIMINAR") { _, _ ->
                lifecycleScope.launch {
                    productoRepo.delete(producto)
                    Toast.makeText(this@MainActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce para no saturar la DB
                    if (query.isEmpty()) {
                        binding.tvInteraccionesTitulo.text = "Tus últimas interacciones"
                        SesionController.usuarioActivo?.let { cargarInteraccionesRecientes(it.id) }
                    } else {
                        binding.tvInteraccionesTitulo.text = "Resultados de búsqueda"
                        productoRepo.searchProductos(query).collectLatest { productos ->
                            adapter.updateList(productos)
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarInteraccionesRecientes(usuarioId: Int) {
        lifecycleScope.launch {
            productoRepo.getUltimasInteracciones(usuarioId).collectLatest { productos ->
                // Solo actualizamos si el buscador está vacío para no pisar resultados
                if (binding.etSearch.text.isEmpty()) {
                    adapter.updateList(productos)
                    binding.tvInteraccionesTitulo.text = if (productos.isEmpty()) 
                        "Aún no tienes interacciones" else "Tus últimas interacciones"
                }
            }
        }
    }
}
