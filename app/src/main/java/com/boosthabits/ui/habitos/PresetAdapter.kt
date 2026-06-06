package com.boosthabits.ui.habitos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boosthabits.R
import com.boosthabits.data.HabitoPreset
import com.boosthabits.databinding.ItemPresetBinding

class PresetAdapter(private val onAddClick: (HabitoPreset) -> Unit) :
    ListAdapter<HabitoPreset, PresetAdapter.PresetViewHolder>(PresetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val binding = ItemPresetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PresetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PresetViewHolder(private val binding: ItemPresetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(preset: HabitoPreset) {
            val contexto = binding.root.context
            binding.tvPresetName.text = contexto.getString(preset.nombre)
            binding.tvPresetIcon.text = preset.icon
            
            val diffText = when(preset.dificultad) {
                1 -> contexto.getString(R.string.difficulty_easy)
                2 -> contexto.getString(R.string.difficulty_medium)
                3 -> contexto.getString(R.string.difficulty_hard)
                else -> contexto.getString(R.string.difficulty_easy)
            }
            
            binding.tvPresetInfo.text = diffText
            
            // muestra indicador de verificable
            binding.layoutVerifiable.visibility = if (preset.verificable) View.VISIBLE else View.GONE
            
            binding.btnAddPreset.setOnClickListener { onAddClick(preset) }
        }
    }

    class PresetDiffCallback : DiffUtil.ItemCallback<HabitoPreset>() {
        override fun areItemsTheSame(oldItem: HabitoPreset, newItem: HabitoPreset): Boolean =
            oldItem.nombre == newItem.nombre

        override fun areContentsTheSame(oldItem: HabitoPreset, newItem: HabitoPreset): Boolean =
            oldItem == newItem
    }
}
