package com.example.appalmacen.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appalmacen.R
import com.example.appalmacen.model.entities.Usuario
import com.google.android.material.switchmaterial.SwitchMaterial

class AdminUsuariosAdapter(
    private val onEstadoChanged: (Usuario, Boolean) -> Unit
) : ListAdapter<Usuario, AdminUsuariosAdapter.UsuarioViewHolder>(UsuarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = getItem(position)
        holder.bind(usuario, onEstadoChanged)
    }

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatarUsuario)
        private val ivAvatarFoto: ImageView = itemView.findViewById(R.id.ivAvatarFoto)
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreUsuario)
        private val tvEstadoBadge: TextView = itemView.findViewById(R.id.tvEstadoBadgeUsuario)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmailUsuario)
        private val switchEstado: SwitchMaterial = itemView.findViewById(R.id.switchEstadoUsuario)

        fun bind(usuario: Usuario, onEstadoChanged: (Usuario, Boolean) -> Unit) {
            tvNombre.text = usuario.nombre
            tvEmail.text = usuario.email

            tvAvatar.text = usuario.nombre.take(1).uppercase()

            switchEstado.setOnCheckedChangeListener(null)

            val estaHabilitado = usuario.habilitado
            switchEstado.isChecked = estaHabilitado

            // 3. Ajustamos el texto y diseño del Badge según el estado inicial
            actualizarDiseñoBadge(estaHabilitado)


            switchEstado.setOnCheckedChangeListener { _, isChecked ->
                actualizarDiseñoBadge(isChecked)
                onEstadoChanged(usuario, isChecked)
            }
        }

        private fun actualizarDiseñoBadge(estaHabilitado: Boolean) {
            if (estaHabilitado) {
                tvEstadoBadge.text = "ACTIVO"
                tvEstadoBadge.setBackgroundResource(R.drawable.bg_badge_activo)
            } else {
                tvEstadoBadge.text = "INACTIVO"
                tvEstadoBadge.setBackgroundResource(R.drawable.bg_badge_rol)
            }
        }
    }

    // ── Optimizador de actualizaciones de la lista (DiffUtil) ──
    class UsuarioDiffCallback : DiffUtil.ItemCallback<Usuario>() {
        override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
            return oldItem == newItem
        }
    }
}