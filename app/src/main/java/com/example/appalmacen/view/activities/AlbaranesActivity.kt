package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivityAlbaranesBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Albaran
import com.example.appalmacen.model.repository.AlbaranRepository
import com.example.appalmacen.view.adapters.AlbaranesAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlbaranesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlbaranesBinding
    private lateinit var albaranRepo: AlbaranRepository
    private lateinit var adapter: AlbaranesAdapter
    private var listaCompleta: List<Albaran> = emptyList()
    private var listaFiltrada: List<Albaran> = emptyList()
    
    private var fechaInicio: Long? = null
    private var fechaFin: Long? = null
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbaranesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAlbaranes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAlbaranes.setNavigationOnClickListener { finish() }

        val db = DatabaseHelper.getInstance(this)
        albaranRepo = AlbaranRepository(db.albaranDAO())

        setupRecyclerView()
        observeAlbaranes()

        binding.btnSelectRange.setOnClickListener {
            mostrarDatePickerRange()
        }

        binding.btnGenerarInforme.setOnClickListener {
            generarInformeDetallado()
        }

        binding.fabAddAlbaran.setOnClickListener {
            startActivity(Intent(this, AlbaranGestionActivity::class.java))
        }
    }

    private fun mostrarDatePickerRange() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Selecciona rango de fechas")
        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            fechaInicio = selection.first
            fechaFin = selection.second
            
            val inicioStr = simpleDateFormat.format(Date(fechaInicio!!))
            val finStr = simpleDateFormat.format(Date(fechaFin!!))
            binding.tvDateRange.text = "Desde $inicioStr hasta $finStr"
            
            aplicarFiltro()
        }

        picker.show(supportFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun aplicarFiltro() {
        listaFiltrada = if (fechaInicio != null && fechaFin != null) {
            // Ajustamos fechaFin al final del día (23:59:59)
            val finAjustado = fechaFin!! + 86399999L 
            listaCompleta.filter { it.fecha in fechaInicio!!..finAjustado }
        } else {
            listaCompleta
        }
        
        adapter.updateList(listaFiltrada)
        actualizarResumen(listaFiltrada)
    }

    private fun setupRecyclerView() {
        adapter = AlbaranesAdapter(
            albaranes = emptyList(),
            onClick = { albaran ->
                val intent = Intent(this, AlbaranGestionActivity::class.java)
                intent.putExtra("ALBARAN_ID", albaran.id)
                startActivity(intent)
            },
            onLongClick = { albaran ->
                if (SesionController.usuarioActivo?.esAdmin == true) {
                    mostrarOpcionesAlbaran(albaran)
                }
            }
        )
        binding.rvAlbaranes.layoutManager = LinearLayoutManager(this)
        binding.rvAlbaranes.adapter = adapter
    }

    private fun observeAlbaranes() {
        lifecycleScope.launch {
            albaranRepo.allAlbaranes.collectLatest { albaranes ->
                listaCompleta = albaranes
                aplicarFiltro()
            }
        }
    }

    private fun actualizarResumen(albaranes: List<Albaran>) {
        val total = albaranes.sumOf { it.importe }
        val pagados = albaranes.filter { it.pagado }.sumOf { it.importe }
        val sinPagar = albaranes.filter { !it.pagado }.sumOf { it.importe }

        binding.tvTotalImporte.text = String.format(Locale.getDefault(), "%.2f€", total)
        binding.tvTotalPagados.text = String.format(Locale.getDefault(), "%.2f€", pagados)
        binding.tvTotalSinPagar.text = String.format(Locale.getDefault(), "%.2f€", sinPagar)
    }

    private fun generarInformeDetallado() {
        if (listaFiltrada.isEmpty()) {
            Toast.makeText(this, "No hay albaranes para generar el informe", Toast.LENGTH_SHORT).show()
            return
        }

        val total = listaFiltrada.sumOf { it.importe }
        val pagados = listaFiltrada.filter { it.pagado }.sumOf { it.importe }
        val sinPagar = listaFiltrada.filter { !it.pagado }.sumOf { it.importe }

        val sb = StringBuilder()
        sb.append("INFORME DE ALBARANES\n")
        if (fechaInicio != null && fechaFin != null) {
            sb.append("Rango: ${simpleDateFormat.format(Date(fechaInicio!!))} - ${simpleDateFormat.format(Date(fechaFin!!))}\n")
        }
        sb.append("-----------------------------------\n")
        sb.append("Total Importe: ${String.format(Locale.getDefault(), "%.2f€", total)}\n")
        sb.append("Total Pagados: ${String.format(Locale.getDefault(), "%.2f€", pagados)}\n")
        sb.append("Total Pendientes: ${String.format(Locale.getDefault(), "%.2f€", sinPagar)}\n\n")
        sb.append("DETALLE:\n")
        
        listaFiltrada.forEach { alb ->
            val estado = if (alb.pagado) "[PAGADO]" else "[PENDIENTE]"
            sb.append("${simpleDateFormat.format(Date(alb.fecha))} | ${alb.nombreProveedor} (${alb.cif}): ${String.format(Locale.getDefault(), "%.2f€", alb.importe)} $estado\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Informe de Albaranes")
            .setMessage(sb.toString())
            .setPositiveButton("CERRAR", null)
            .show()
    }

    private fun mostrarOpcionesAlbaran(albaran: Albaran) {
        val opciones = arrayOf(
            if (albaran.pagado) "Marcar como Pendiente" else "Marcar como Pagado",
            "Eliminar Albarán"
        )

        AlertDialog.Builder(this)
            .setTitle("Albarán ${albaran.nombreProveedor}")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> togglePago(albaran)
                    1 -> confirmarBorrado(albaran)
                }
            }
            .show()
    }

    private fun togglePago(albaran: Albaran) {
        lifecycleScope.launch {
            val nuevoEstado = !albaran.pagado
            val fechaPago = if (nuevoEstado) System.currentTimeMillis() else null
            val actualizado = albaran.copy(pagado = nuevoEstado, fechaPago = fechaPago)
            albaranRepo.update(actualizado)
            Toast.makeText(this@AlbaranesActivity, "Estado de pago actualizado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarBorrado(albaran: Albaran) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Albarán")
            .setMessage("¿Estás seguro de que deseas eliminar este albarán?")
            .setPositiveButton("ELIMINAR") { _, _ ->
                lifecycleScope.launch {
                    albaranRepo.delete(albaran)
                    Toast.makeText(this@AlbaranesActivity, "Albarán eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }
}
