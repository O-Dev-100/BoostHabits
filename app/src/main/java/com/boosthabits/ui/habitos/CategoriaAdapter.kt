package com.boosthabits.ui.habitos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.boosthabits.data.HabitoCategoria
import com.boosthabits.databinding.ItemCategoryBinding

class CategoriaAdapter(private val alPulsarCategoria: (HabitoCategoria) -> Unit) :
    ListAdapter<com.boosthabits.data.HabitoCategoria, CategoriaAdapter.CategoriaViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoriaViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoria: com.boosthabits.data.HabitoCategoria) {
            val contexto = binding.root.context
            binding.tvCategoryTitle.text = contexto.getString(categoria.titulo)
            
            if (categoria.icon.contains("ic_") || categoria.icon.contains("icono_")) {
                binding.tvCategoriaIcon.visibility = View.GONE
                binding.ivCategoryIconDrawable.visibility = View.VISIBLE
                val idRes = contexto.resources.getIdentifier(categoria.icon, "drawable", contexto.packageName)
                if (idRes != 0) {
                    binding.ivCategoryIconDrawable.setImageResource(idRes)
                }
            } else {
                binding.tvCategoriaIcon.visibility = View.VISIBLE
                binding.ivCategoryIconDrawable.visibility = View.GONE
                binding.tvCategoriaIcon.text = categoria.icon
            }
            
            // muestra icono de gemas solo para Actividad Física (ID 1)
            binding.ivGemIcon.visibility = if (categoria.id == 1) View.VISIBLE else View.GONE
            

            binding.ivZenIconCategory.visibility = View.GONE

            // muestra burbuja focus solo para Salud Mental (ID 4)
            binding.btnFocusBadge.visibility = if (categoria.id == 4) View.VISIBLE else View.GONE
            
            binding.cardCategory.setOnClickListener { alPulsarCategoria(categoria) }
        }
    }

    fun getCategoria(posicion: Int): com.boosthabits.data.HabitoCategoria = getItem(posicion)

    class CategoryDiffCallback : DiffUtil.ItemCallback<com.boosthabits.data.HabitoCategoria>() {
        override fun areItemsTheSame(oldItem: com.boosthabits.data.HabitoCategoria, newItem: com.boosthabits.data.HabitoCategoria): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: com.boosthabits.data.HabitoCategoria, newItem: com.boosthabits.data.HabitoCategoria): Boolean {
            return oldItem == newItem
        }
    }
}
