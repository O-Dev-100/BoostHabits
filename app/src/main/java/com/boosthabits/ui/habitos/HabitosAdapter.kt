package com.boosthabits.ui.habitos

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boosthabits.R
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.databinding.ItemHabitBinding

class HabitosAdapter : ListAdapter<HabitoEntity, HabitosAdapter.ViewHolder>(DiffCallback) {

    private var onItemClick: ((HabitoEntity) -> Unit)? = null
    private var onFocusClick: ((HabitoEntity) -> Unit)? = null

    fun setOnItemClickListener(listener: (HabitoEntity) -> Unit) {
        onItemClick = listener
    }

    fun setOnFocusClickListener(listener: (HabitoEntity) -> Unit) {
        onFocusClick = listener
    }

    class ViewHolder(private val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(habito: HabitoEntity, onClick: ((HabitoEntity) -> Unit)?, onFocusClick: ((HabitoEntity) -> Unit)?) {
            val context = binding.root.context
            binding.tvIcono.text = habito.icono
            binding.tvNombre.text = habito.nombre
            binding.tvObjetivo.text = context.getString(R.string.habit_daily_goal, habito.objetivoDiario)
            binding.tvDificultad.text = "★".repeat(habito.dificultad)
            
            if (habito.esVerificable) {
                binding.layoutProgress.visibility = View.VISIBLE
                val progreso = if (habito.valorObjetivo > 0) (habito.valorActual / habito.valorObjetivo * 100).toInt() else 0
                
                // animación suave del progreso
                ObjectAnimator.ofInt(binding.progressIndicator, "progress", progreso).apply {
                    duration = 1000
                    interpolator = DecelerateInterpolator()
                    start()
                }

                val unidad = when {
                    habito.nombre.contains("pasos", ignoreCase = true) || habito.nombre.contains("steps", ignoreCase = true) -> context.getString(R.string.stats_steps)
                    habito.nombre.contains("m", ignoreCase = true) -> context.getString(R.string.habit_unit_meters)
                    habito.nombre.contains("calorías", ignoreCase = true) || habito.nombre.contains("kcal", ignoreCase = true) || habito.nombre.contains("calories", ignoreCase = true) -> context.getString(R.string.habit_unit_kcal)
                    habito.nombre.contains("min", ignoreCase = true) -> context.getString(R.string.habit_unit_min)
                    else -> ""
                }
                
                binding.tvProgressText.text = "${habito.valorActual.toInt()} / ${habito.valorObjetivo.toInt()} $unidad"
            } else {
                binding.layoutProgress.visibility = View.GONE
            }

            binding.root.setOnClickListener { onClick?.invoke(habito) }

            // lógica de Focus para Salud Mental
            if (habito.categoria == com.boosthabits.data.local.entity.HabitCategory.SALUD_MENTAL && !habito.completadoHoy) {
                binding.btnFocus.visibility = View.VISIBLE
                binding.btnFocus.setOnClickListener { onFocusClick?.invoke(habito) }
                startPulseAnimation(binding.btnFocus)
            } else {
                binding.btnFocus.visibility = View.GONE
            }
        }


        //lógica de la animación:
        private fun startPulseAnimation(view: View) {
            val scaleX = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f)
            val scaleY = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
            android.animation.ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
                duration = 2000
                repeatCount = android.animation.ValueAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.RESTART
                start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, onFocusClick)
    }

    private object DiffCallback : DiffUtil.ItemCallback<HabitoEntity>() {
        override fun areItemsTheSame(oldItem: HabitoEntity, newItem: HabitoEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HabitoEntity, newItem: HabitoEntity) = oldItem == newItem
    }
}
