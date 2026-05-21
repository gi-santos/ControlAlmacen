package com.example.appalmacen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.appalmacen.R
import com.example.appalmacen.databinding.ItemProductoBinding
import com.example.appalmacen.model.entities.Producto
import java.io.File

class ProductoAdapter(
    private val onSumar: (Producto) -> Unit,
    private val onRestar: (Producto) -> Unit
) : ListAdapter<Producto, ProductoAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = getItem(position)

        with(holder.binding) {
            tvNombreProducto.text = producto.nombre
            tvCantidad.text = producto.cantidad.toString()
            tvSkuProducto.text = "Mínimo: ${producto.cantidadMinima}"

            // --- IMAGEN ---
            if (!producto.imagen.isNullOrEmpty()) {
                val file = File(producto.imagen!!)
                if (file.exists()) {
                    ivProducto.load(file) {
                        crossfade(true)
                        placeholder(R.drawable.ic_warehouse)
                        error(R.drawable.ic_warehouse)
                        transformations(RoundedCornersTransformation(12f))
                    }
                } else {
                    ivProducto.setImageResource(R.drawable.ic_warehouse)
                }
            } else {
                ivProducto.setImageResource(R.drawable.ic_warehouse)
            }

            // --- ESTADO VISUAL (ACTIVO/INACTIVO) ---
            val backgroundRes = if (producto.habilitado) {
                R.drawable.bg_item_producto_activo
            } else {
                R.drawable.bg_item_producto
            }
            layoutItem.setBackgroundResource(backgroundRes)

            // --- ALERTA DE CANTIDAD MÍNIMA ---
            val esCritico = producto.cantidad <= producto.cantidadMinima

            tvCantidad.setTextColor(
                ContextCompat.getColor(
                    root.context,
                    if (esCritico) R.color.cantidad_critica else R.color.cantidad_normal
                )
            )
            tvCantidad.setBackgroundResource(
                if (esCritico) R.drawable.bg_cantidad_critica else 0
            )

            // --- LISTENERS ---
            btnMas.setOnClickListener { onSumar(producto) }
            btnMenos.setOnClickListener { onRestar(producto) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(old: Producto, new: Producto) = old.id == new.id
        override fun areContentsTheSame(old: Producto, new: Producto) = old == new
    }
}