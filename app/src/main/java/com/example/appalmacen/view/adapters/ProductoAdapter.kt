package com.example.appalmacen.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appalmacen.R
import com.example.appalmacen.databinding.ItemProductoBinding
import com.example.appalmacen.model.entities.Producto

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
            // Vinculación con los datos reales de tu Entidad
            tvNombreProducto.text = producto.nombre
            tvCantidad.text = producto.cantidad.toString()

            // Usamos 'cantidadMinima' para mostrar información extra si lo deseas
            tvSkuProducto.text = "Mínimo requerido: ${producto.cantidadMinima}"

            // Lógica visual basada en el campo 'habilitado'
            val backgroundRes = if (producto.habilitado) {
                R.drawable.bg_item_producto_activo
            } else {
                R.drawable.bg_item_producto // Estado deshabilitado
            }
            layoutItem.setBackgroundResource(backgroundRes)

            // Listeners
            btnMas.setOnClickListener { onSumar(producto) }
            btnMenos.setOnClickListener { onRestar(producto) }

            // Nota: Si usas Glide o Coil para la imagen, aquí usarías producto.imagen
            // Ejemplo: Glide.with(root.context).load(producto.imagen).into(ivProducto)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Producto>() {
        // Comparamos por el ID único definido en tu @PrimaryKey
        override fun areItemsTheSame(old: Producto, new: Producto) = old.id == new.id

        // Al ser una Data Class, la comparación de contenido es automática
        override fun areContentsTheSame(old: Producto, new: Producto) = old == new
    }
}