package com.example.appalmacen.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appalmacen.databinding.ItemProductoBinding
import com.example.appalmacen.model.entities.Producto

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(private val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: Producto) {
            binding.tvProductoNombre.text = producto.nombre
            binding.tvProductoCantidad.text = "Stock: ${producto.cantidad}"
            
            // Indicador de stock bajo
            if (producto.cantidad <= producto.cantidadMinima) {
                binding.tvProductoCantidad.setTextColor(Color.RED)
                binding.statusIndicator.setBackgroundColor(Color.RED)
            } else {
                binding.tvProductoCantidad.setTextColor(Color.GRAY)
                binding.statusIndicator.setBackgroundColor(Color.GREEN)
            }

            Glide.with(binding.root)
                .load(producto.imagen)
                .circleCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivProductoImagen)

            binding.root.setOnClickListener { onClick(producto) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size

    fun updateList(newList: List<Producto>) {
        productos = newList
        notifyDataSetChanged()
    }
}
