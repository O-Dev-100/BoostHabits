package com.boosthabits.ui.recompensas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boosthabits.R
import com.boosthabits.data.local.entity.CurrencyType
import com.boosthabits.data.local.entity.RecompensaEntity
import com.boosthabits.databinding.ItemRewardBinding

// adaptador para la lista de recompensas estandar (cosmeticos)
class RecompensasAdapter(
    private val userBalance: Int,
    private val onRedeemClick: (RecompensaEntity) -> Unit,
    private val onItemClick: (RecompensaEntity) -> Unit
) : ListAdapter<RecompensaEntity, RecompensasAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemRewardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            recompensa: RecompensaEntity,
            userBalance: Int,
            onRedeemClick: (RecompensaEntity) -> Unit,
            onItemClick: (RecompensaEntity) -> Unit
        ) {
            val context = binding.root.context
            // configuramos los textos traducidos si están disponibles
            binding.tvTitle.text = if (recompensa.resTitulo != 0) context.getString(recompensa.resTitulo) else recompensa.titulo
            binding.tvDescription.text = if (recompensa.resDescripcion != 0) context.getString(recompensa.resDescripcion) else recompensa.descripcion
            
            val currencySuffix = if (recompensa.tipoMoneda == CurrencyType.GEMA) 
                context.getString(R.string.reward_type_gems) else 
                context.getString(R.string.reward_type_coins)
            binding.tvCost.text = "${recompensa.coste} $currencySuffix"
            
            // habilitamos el boton solo si hay saldo suficiente
            binding.btnRedeem.isEnabled = userBalance >= recompensa.coste
            binding.btnRedeem.text = context.getString(R.string.rewards_redeem)
            
            binding.btnRedeem.setOnClickListener { onRedeemClick(recompensa) }
            binding.root.setOnClickListener { onItemClick(recompensa) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), userBalance, onRedeemClick, onItemClick)
    }

    private object DiffCallback : DiffUtil.ItemCallback<RecompensaEntity>() {
        override fun areItemsTheSame(oldItem: RecompensaEntity, newItem: RecompensaEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RecompensaEntity, newItem: RecompensaEntity) = oldItem == newItem
    }
}
