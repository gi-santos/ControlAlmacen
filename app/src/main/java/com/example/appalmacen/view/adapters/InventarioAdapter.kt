package com.example.appalmacen.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appalmacen.R
import com.example.appalmacen.model.entities.Producto

class InventarioAdapter(
    private var productos: List<Producto>,
    private val onItemClick: (Producto) -> Unit,
    private val onItemLongClick: (Producto) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvInvNombre)
        val tvStock: TextView = view.findViewById(R.id.tvInvStock)
        val tvStockMin: TextView = view.findViewById(R.id.tvInvStockMin)
        val tvEstado: TextView = view.findViewById(R.id.tvInvEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventario_excel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]
        holder.tvNombre.text = producto.nombre
        holder.tvStock.text = producto.cantidad.toString()
        holder.tvStockMin.text = producto.cantidadMinima.toString()
        
        holder.tvEstado.text = if (producto.habilitado) "ACTIVO" else "DESC"
        holder.tvEstado.setTextColor(if (producto.habilitado) Color.parseColor("#4CAF50") else Color.RED)

        // Estilo de fila (alternancia de colores)
        val backgroundColor = if (position % 2 == 0) Color.WHITE else Color.parseColor("#F9F9F9")
        holder.itemView.setBackgroundColor(backgroundColor)

        // Alerta visual de stock bajo (sobrescribe el fondo si hay alerta)
        if (producto.cantidad <= producto.cantidadMinima) {
            holder.tvStock.setTextColor(Color.RED)
            holder.tvStock.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF0F0"))
        } else {
            holder.tvStock.setTextColor(Color.BLACK)
            holder.tvStock.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        holder.itemView.setOnClickListener { onItemClick(producto) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(producto)
            true
        }
    }

    override fun getItemCount() = productos.size

    fun updateList(newList: List<Producto>) {
        productos = newList
        notifyDataSetChanged()
    }
}
