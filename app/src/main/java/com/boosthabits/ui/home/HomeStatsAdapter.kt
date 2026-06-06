package com.boosthabits.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boosthabits.R
import com.boosthabits.databinding.ItemHomeStatCardBinding

class HomeStatsAdapter(
    private val alPulsarAnadirHabito: () -> Unit
) : RecyclerView.Adapter<HomeStatsAdapter.StatViewHolder>() {

    fun actualizarValores(nuevasMonedas: Int, nuevasGemas: Int) {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val binding = ItemHomeStatCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = 4

    inner class StatViewHolder(private val binding: ItemHomeStatCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(posicion: Int) {
            val contexto = binding.root.context
            binding.statsContainer.visibility = View.GONE
            binding.actionContainer.visibility = View.GONE

            when (posicion) {
                0 -> { // Monedas
                    binding.statsContainer.visibility = View.VISIBLE
                    binding.tvCardLabel.text = contexto.getString(R.string.home_card_coins_title)
                    binding.tvCardDescription.text = contexto.getString(R.string.home_card_coins_desc)
                    binding.ivCardIcon.setImageResource(R.drawable.ic_monedas)
                }
                1 -> { // Gemas
                    binding.statsContainer.visibility = View.VISIBLE
                    binding.tvCardLabel.text = contexto.getString(R.string.home_card_gems_title)
                    binding.tvCardDescription.text = contexto.getString(R.string.home_card_gems_desc)
                    binding.ivCardIcon.setImageResource(R.drawable.ic_gemas)
                }
                2 -> { // Recompensas
                    binding.statsContainer.visibility = View.VISIBLE
                    binding.tvCardLabel.text = contexto.getString(R.string.home_card_rewards_title)
                    binding.tvCardDescription.text = contexto.getString(R.string.home_card_rewards_desc)
                    binding.ivCardIcon.setImageResource(R.drawable.ic_aplicacion)
                }
                3 -> { // Acción (Añadir Hábito)
                    binding.actionContainer.visibility = View.VISIBLE
                    binding.tvActionText.text = contexto.getString(R.string.home_card_add_habit)
                    binding.btnAddHabitCard.setOnClickListener { alPulsarAnadirHabito() }
                }
            }
        }
    }
}
