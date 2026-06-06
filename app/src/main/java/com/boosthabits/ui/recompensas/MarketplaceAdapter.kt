package com.boosthabits.ui.recompensas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.boosthabits.data.local.entity.RecompensaEntity
import com.boosthabits.databinding.ItemMarketplaceOfferBinding

// adaptador para mostrar las ofertas de marca en el marketplace
class MarketplaceAdapter(
    private val onItemClick: (RecompensaEntity) -> Unit
) : ListAdapter<RecompensaEntity, MarketplaceAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemMarketplaceOfferBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(oferta: RecompensaEntity, onItemClick: (RecompensaEntity) -> Unit) {
            val context = binding.root.context
            // asignamos los textos de la oferta traducidos
            binding.tvOfferTitle.text = if (oferta.resTitulo != 0) context.getString(oferta.resTitulo) else oferta.titulo
            binding.tvOfferSubtitle.text = if (oferta.resDescripcion != 0) context.getString(oferta.resDescripcion) else oferta.descripcion
            binding.tvOfferCost.text = oferta.coste.toString()

            // cargamos la imagen principal (promocional) y el logo de la marca
            cargarImagen(oferta.urlImagen, binding.ivBrandImage)
            cargarImagen(oferta.urlImagenMarca ?: "", binding.ivBrandLogo)

            binding.root.setOnClickListener { onItemClick(oferta) }
        }

        private fun cargarImagen(fuente: String, imageView: android.widget.ImageView) {
            if (fuente.isEmpty()) return
            
            if (fuente.startsWith("http")) {
                imageView.load(fuente) {
                    crossfade(true)
                }
            } else {
                val context = imageView.context
                val resId = context.resources.getIdentifier(fuente, "drawable", context.packageName)
                if (resId != 0) {
                    imageView.setImageResource(resId)
                } else {
                    imageView.load(fuente)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMarketplaceOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    private object DiffCallback : DiffUtil.ItemCallback<RecompensaEntity>() {
        override fun areItemsTheSame(oldItem: RecompensaEntity, newItem: RecompensaEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RecompensaEntity, newItem: RecompensaEntity) = oldItem == newItem
    }
}
