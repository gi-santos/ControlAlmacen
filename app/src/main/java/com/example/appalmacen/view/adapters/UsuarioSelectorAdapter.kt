package com.example.appalmacen.view.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appalmacen.R
import com.example.appalmacen.model.entities.Usuario
import de.hdodenhof.circleimageview.CircleImageView

class UsuarioSelectorAdapter(
    private val usuarios: List<Usuario>,
    private val onUsuarioClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioSelectorAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val civFoto: CircleImageView = view.findViewById(R.id.civFoto)
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvRol: TextView = view.findViewById(R.id.tvRol)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_selector, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = usuarios[position]

        holder.tvNombre.text = usuario.nombre
        holder.tvRol.text = if (usuario.esAdmin) "Administrador" else "Empleado"

        if (!usuario.foto.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(usuario.foto)
            if (bitmap != null) {
                holder.civFoto.setImageBitmap(bitmap)
            } else {
                holder.civFoto.setImageResource(R.drawable.ic_person)
            }
        } else {
            holder.civFoto.setImageResource(R.drawable.ic_person)
        }

        holder.itemView.setOnClickListener {
            onUsuarioClick(usuario)
        }
    }

    override fun getItemCount() = usuarios.size
}