package com.boosthabits.ui.perfil

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boosthabits.R
import com.boosthabits.data.local.entity.RecompensaEntity
import com.boosthabits.data.local.entity.UserStatsEntity
import com.boosthabits.databinding.ItemPersonalizationBinding

class CosmeticoAdapter(
    private var estadisticasUsuario: UserStatsEntity?,
    private val alAlternar: (RecompensaEntity) -> Unit
) : ListAdapter<RecompensaEntity, CosmeticoAdapter.CosmeticViewHolder>(CosmeticDiffCallback()) {

    fun actualizarEstadisticas(nuevasEstadisticas: UserStatsEntity?) {
        estadisticasUsuario = nuevasEstadisticas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CosmeticViewHolder {
        val binding = ItemPersonalizationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CosmeticViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CosmeticViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CosmeticViewHolder(private val binding: ItemPersonalizationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recompensa: RecompensaEntity) {
            binding.tvCosmeticName.text = recompensa.titulo
            
            val titulo = recompensa.titulo
            val context = binding.root.context
            binding.tvCosmeticType.text = when {
                titulo.contains("Nombre", ignoreCase = true) -> context.getString(R.string.personalizacion_type_name)
                titulo.contains("Marco", ignoreCase = true) -> context.getString(R.string.personalizacion_type_frame)
                titulo.contains("Fondo", ignoreCase = true) -> context.getString(R.string.personalization_type_wallpaper)
                titulo.contains("Perfil", ignoreCase = true) -> context.getString(R.string.personalization_type_pfp)
                else -> "Cosmético"
            }
            
            // preview dinámico, pienso mejorarlo a futuro
            when {
                titulo.contains("Nombre", ignoreCase = true) -> {
                    binding.ivPreview.setImageResource(R.drawable.ic_aplicacion)
                    CosmeticoManager.applyLegendaryNameEffect(binding.tvCosmeticName)
                }
                titulo.contains("Marco", ignoreCase = true) -> {
                    binding.ivPreview.setImageResource(R.mipmap.ic_launcher_round)
                }
                titulo.contains("Fondo", ignoreCase = true) -> {
                    val resId = context.resources.getIdentifier(
                        recompensa.urlImagen, "drawable", context.packageName
                    )
                    if (resId != 0) binding.ivPreview.setImageResource(resId)
                    else binding.ivPreview.setImageResource(R.drawable.bg_gradient_light_blue)
                    CosmeticoManager.removeLegendaryNameEffect(binding.tvCosmeticName)
                }
                titulo.contains("Perfil", ignoreCase = true) -> {
                    val resId = context.resources.getIdentifier(
                        recompensa.urlImagen, "mipmap", context.packageName
                    )
                    if (resId != 0) binding.ivPreview.setImageResource(resId)
                    else binding.ivPreview.setImageResource(R.mipmap.ic_launcher_round)
                    CosmeticoManager.removeLegendaryNameEffect(binding.tvCosmeticName)
                }
                else -> {
                    binding.ivPreview.setImageResource(R.drawable.ic_aplicacion)
                    CosmeticoManager.removeLegendaryNameEffect(binding.tvCosmeticName)
                }
            }

            val estaActivo = when {
                titulo.contains("Nombre", ignoreCase = true) -> estadisticasUsuario?.idNombreEquipado == recompensa.id
                titulo.contains("Marco", ignoreCase = true) -> estadisticasUsuario?.idMarcoAvatarEquipado == recompensa.id
                titulo.contains("Fondo", ignoreCase = true) -> estadisticasUsuario?.idFondoPantallaEquipado == recompensa.id
                titulo.contains("Perfil", ignoreCase = true) -> estadisticasUsuario?.idFotoPerfilEquipada == recompensa.id
                else -> false
            }

            binding.rbActive.isChecked = estaActivo
            binding.root.setOnClickListener { alAlternar(recompensa) }
            binding.rbActive.setOnClickListener { alAlternar(recompensa) }
        }
    }

    class CosmeticDiffCallback : DiffUtil.ItemCallback<RecompensaEntity>() {
        override fun areItemsTheSame(oldItem: RecompensaEntity, newItem: RecompensaEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RecompensaEntity, newItem: RecompensaEntity): Boolean =
            oldItem == newItem
    }
}
