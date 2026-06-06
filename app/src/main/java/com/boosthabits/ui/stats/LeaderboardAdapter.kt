package com.boosthabits.ui.stats

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boosthabits.R
import com.boosthabits.data.model.LeaderboardUser
import com.boosthabits.databinding.ItemLeaderboardBinding
import com.boosthabits.ui.perfil.CosmeticoManager

class LeaderboardAdapter : ListAdapter<LeaderboardUser, LeaderboardAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(usuario: LeaderboardUser) {
            val context = itemView.context
            val montserrat = ResourcesCompat.getFont(context, R.font.montserrat) //es el estilo de letra general de la aplicación
            val montserratBold = ResourcesCompat.getFont(context, R.font.montserrat_bold)

            binding.tvRank.text = usuario.rango.toString()
            binding.tvUsername.text = usuario.nombreUsuario
            binding.tvFlag.text = usuario.emojiBandera
            binding.tvXp.text = usuario.gemasGastadas.toString()
            binding.tvUserLevel.text = context.getString(R.string.reward_type_gems)

            //Resetear y Aplicar efecto de nombre
            binding.tvUsername.paint.shader = null
            binding.tvUsername.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
            binding.tvUsername.typeface = montserrat ?: Typeface.DEFAULT
            binding.tvUsername.paintFlags = binding.tvUsername.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            
            when (usuario.idEfectoNombre) {
                "3" -> CosmeticoManager.applyLegendaryNameEffect(binding.tvUsername, intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#FFFFFF"), Color.parseColor("#FFD700")), Color.parseColor("#FFEE58"))
                "4" -> CosmeticoManager.applyLegendaryNameEffect(binding.tvUsername, intArrayOf(Color.parseColor("#FFC107"), Color.parseColor("#FFD700"), Color.parseColor("#FFC107")), Color.parseColor("#FFD700"))
                "5" -> CosmeticoManager.applyLegendaryNameEffect(binding.tvUsername, intArrayOf(Color.parseColor("#FF8F00"), Color.parseColor("#FFB300"), Color.parseColor("#FF8F00")), Color.parseColor("#FFA000"))
                
                "style_bold" -> binding.tvUsername.typeface = montserratBold ?: Typeface.DEFAULT_BOLD
                "style_italic" -> binding.tvUsername.typeface = Typeface.create(montserrat, Typeface.ITALIC)
                "style_bold_italic" -> binding.tvUsername.typeface = Typeface.create(montserratBold, Typeface.BOLD_ITALIC)
                "style_monospace" -> binding.tvUsername.typeface = Typeface.MONOSPACE
                "style_underline" -> {
                    binding.tvUsername.typeface = montserrat
                    binding.tvUsername.paintFlags = binding.tvUsername.paintFlags or Paint.UNDERLINE_TEXT_FLAG}


                "color_red" -> binding.tvUsername.setTextColor(Color.parseColor("#E53935"))
                "color_blue" -> binding.tvUsername.setTextColor(Color.parseColor("#1E88E5"))
                "color_green" -> binding.tvUsername.setTextColor(Color.parseColor("#43A047"))
                "color_purple" -> binding.tvUsername.setTextColor(Color.parseColor("#8E24AA"))

                else -> {
                    CosmeticoManager.removeLegendaryNameEffect(binding.tvUsername)
                    binding.tvUsername.setTextColor(ContextCompat.getColor(context, R.color.color_on_surface))
                }
            }

            // aplicar avatar
            if (usuario.nombreRecursoAvatar != null) {
                val resId = context.resources.getIdentifier(usuario.nombreRecursoAvatar, "mipmap", context.packageName)
                if (resId != 0) binding.ivAvatar.setImageResource(resId)
            }

            // aplicar marco (Lottie)
            if (usuario.nombreRecursoMarcoAvatar.isNotEmpty()) {
                val resId = context.resources.getIdentifier(usuario.nombreRecursoMarcoAvatar, "raw", context.packageName)
                if (resId != 0) {
                    binding.ivFrame.setAnimation(resId)
                    binding.ivFrame.playAnimation()
                } else {
                    binding.ivFrame.clearAnimation()
                    binding.ivFrame.setImageDrawable(null)
                }
            } else {
                binding.ivFrame.clearAnimation()
                binding.ivFrame.setImageDrawable(null)
            }

            // Destacar Top 3
            when (usuario.rango) {
                1 -> {
                    binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.rank_gold))
                    binding.tvRank.setBackgroundResource(R.drawable.bg_economy_chip)
                    binding.cardLeaderboard.setCardBackgroundColor(Color.parseColor("#FFFDF0")) 
                    binding.cardLeaderboard.strokeColor = ContextCompat.getColor(context, R.color.rank_gold)
                }
                2 -> {
                    binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.rank_silver))
                    binding.tvRank.setBackgroundResource(R.drawable.bg_economy_chip)
                    binding.cardLeaderboard.setCardBackgroundColor(Color.parseColor("#F5F5F5")) 
                    binding.cardLeaderboard.strokeColor = ContextCompat.getColor(context, R.color.rank_silver)
                }
                3 -> {
                    binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.rank_bronze))
                    binding.tvRank.setBackgroundResource(R.drawable.bg_economy_chip)
                    binding.cardLeaderboard.setCardBackgroundColor(Color.parseColor("#FFF8F4")) 
                    binding.cardLeaderboard.strokeColor = ContextCompat.getColor(context, R.color.rank_bronze)
                }
                else -> {
                    binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.color_on_surface_variant))
                    binding.tvRank.background = null
                    binding.cardLeaderboard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_surface))
                    binding.cardLeaderboard.strokeColor = ContextCompat.getColor(context, R.color.color_surface_variant)
                }
            }
        } }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLeaderboardBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LeaderboardUser>() {
        override fun areItemsTheSame(oldItem: LeaderboardUser, newItem: LeaderboardUser): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LeaderboardUser, newItem: LeaderboardUser): Boolean = oldItem == newItem
    }
}
