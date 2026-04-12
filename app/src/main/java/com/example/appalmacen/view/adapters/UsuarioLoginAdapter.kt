package com.example.appalmacen.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appalmacen.databinding.ItemUsuarioLoginBinding
import com.example.appalmacen.model.entities.Usuario

class UsuarioLoginAdapter(
    private var usuarios: List<Usuario>,
    private val onUserSelected: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioLoginAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(private val binding: ItemUsuarioLoginBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(usuario: Usuario) {
            binding.tvUserName.text = usuario.nombre
            binding.ivAdminBadge.visibility = if (usuario.esAdmin) View.VISIBLE else View.GONE
            
            Glide.with(binding.root)
                .load(usuario.foto)
                .circleCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivUserPhoto)

            binding.root.setOnClickListener {
                onUserSelected(usuario)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioLoginBinding.inflate(
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
