package com.example.appalmacen.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appalmacen.databinding.ItemUsuarioGestionBinding
import com.example.appalmacen.model.entities.Usuario

class UsuarioGestionAdapter(
    private var usuarios: List<Usuario>,
    private val onEdit: (Usuario) -> Unit,
    private val onDelete: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioGestionAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(private val binding: ItemUsuarioGestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(usuario: Usuario) {
            binding.tvUserNameGestion.text = usuario.nombre
            binding.tvUserRoleGestion.text = if (usuario.esAdmin) "Administrador" else "Usuario"
            
            Glide.with(binding.root)
                .load(usuario.foto)
                .circleCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivUserPhotoGestion)

            binding.btnEditUser.setOnClickListener { onEdit(usuario) }
            binding.btnDeleteUser.setOnClickListener { onDelete(usuario) }
            
            // No permitir borrar el admin principal (id 1 por lo general) o a uno mismo si fuera necesario
            // Por simplicidad, solo mostramos el botón. El repository/DAO se encargará si hay restricciones.
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioGestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(usuarios[position])
    }

    override fun getItemCount(): Int = usuarios.size

    fun updateList(newList: List<Usuario>) {
        usuarios = newList
        notifyDataSetChanged()
    }
}
