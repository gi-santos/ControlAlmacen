package com.example.appalmacen.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appalmacen.R
import com.example.appalmacen.model.entities.Producto
import com.google.android.material.switchmaterial.SwitchMaterial



class AdminProductosAdapter(
    private val onEstadoCambiado: (producto: Producto, nuevoEstado: Boolean) -> Unit
) : ListAdapter<Producto, AdminProductosAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProducto)
        val tvStock: TextView = itemView.findViewById(R.id.tvStockProducto)
        val tvEstadoBadge: TextView = itemView.findViewById(R.id.tvEstadoBadge)
        val switchEstado: SwitchMaterial = itemView.findViewById(R.id.switchEstadoProducto)
        val viewDivider: View = itemView.findViewById(R.id.viewDivider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_producto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = getItem(position)

        // Ocultar el divider en el primer ítem
        holder.viewDivider.visibility = if (position == 0) View.GONE else View.VISIBLE

        holder.tvNombre.text = producto.nombre
        holder.tvStock.text = "Stock: ${producto.cantidad}"

        // Badge de estado
        if (producto.habilitado) {
            holder.tvEstadoBadge.text = "HABILITADO"
            holder.tvEstadoBadge.backgroundTintList =
                ContextCompat.getColorStateList(holder.itemView.context, R.color.estado_activo)
        } else {
            holder.tvEstadoBadge.text = "DESHABILITADO"
            holder.tvEstadoBadge.backgroundTintList =
                ContextCompat.getColorStateList(holder.itemView.context, R.color.estado_inactivo)
        }

        // Aplicar opacidad reducida a productos deshabilitados
        holder.itemView.alpha = if (producto.habilitado) 1.0f else 0.55f

        // Evitar disparar el listener al hacer bind
        holder.switchEstado.setOnCheckedChangeListener(null)
        holder.switchEstado.isChecked = producto.habilitado

        holder.switchEstado.setOnCheckedChangeListener { _, isChecked ->
            onEstadoCambiado(producto, isChecked)
        }
    }

    // ── DiffCallback para actualizaciones eficientes ──────────────────────
    class DiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem == newItem
    }
}