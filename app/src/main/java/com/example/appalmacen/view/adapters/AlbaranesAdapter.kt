package com.example.appalmacen.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appalmacen.R
import com.example.appalmacen.model.entities.Albaran
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlbaranesAdapter(
    private var albaranes: List<Albaran>,
    private val onClick: (Albaran) -> Unit,
    private val onLongClick: (Albaran) -> Unit
) : RecyclerView.Adapter<AlbaranesAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProveedor: TextView = view.findViewById(R.id.tvItemProveedor)
        val tvCif: TextView = view.findViewById(R.id.tvItemCif)
        val tvFecha: TextView = view.findViewById(R.id.tvItemFecha)
        val tvImporte: TextView = view.findViewById(R.id.tvItemImporte)
        val tvEstado: TextView = view.findViewById(R.id.tvItemEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_albaran, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val albaran = albaranes[position]
        holder.tvProveedor.text = albaran.nombreProveedor
        holder.tvCif.text = "CIF: ${albaran.cif}"
        holder.tvFecha.text = dateFormat.format(Date(albaran.fecha))
        holder.tvImporte.text = String.format("%.2f€", albaran.importe)
        
        if (albaran.pagado) {
            holder.tvEstado.text = "PAGADO"
            holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvEstado.text = "PENDIENTE"
            holder.tvEstado.setTextColor(Color.parseColor("#F44336"))
        }

        holder.itemView.setOnClickListener { onClick(albaran) }
        holder.itemView.setOnLongClickListener {
            onLongClick(albaran)
            true
        }
    }

    override fun getItemCount() = albaranes.size

    fun updateList(newList: List<Albaran>) {
        albaranes = newList
        notifyDataSetChanged()
    }
}
