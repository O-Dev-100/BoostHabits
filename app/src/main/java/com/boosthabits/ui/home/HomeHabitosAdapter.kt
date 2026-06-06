package com.boosthabits.ui.home

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boosthabits.R
import com.boosthabits.data.local.entity.HabitCategory
import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.data.local.entity.RecompensaTipo
import com.boosthabits.databinding.ItemHabitHomeBinding

class HomeHabitosAdapter(
    private val alPulsarCompletar: (HabitoEntity, View) -> Unit,
    private val alPulsarEliminar: (HabitoEntity) -> Unit = {},
    private val alPulsarLargo: (HabitoEntity) -> Unit,
    private val alPulsarFocus: ((HabitoEntity) -> Unit)? = null,
    private val esSeccionEspera: Boolean = false
) : ListAdapter<HabitoEntity, HomeHabitosAdapter.ViewHolder>(HabitDiffCallback()) {

    private var progresoActividad: Map<Long, Int> = emptyMap()
    private var valoresRawActividad: Map<Long, String> = emptyMap()
    private val manejador = Handler(Looper.getMainLooper())
    private val tareaActualizacion = object : Runnable {
        override fun run() {
            notifyDataSetChanged()
            manejador.postDelayed(this, 1000)
        }
    }

    fun actualizarProgresoActividad(nuevoProgreso: Map<Long, Int>, nuevosValoresRaw: Map<Long, String> = emptyMap()) {
        this.progresoActividad = nuevoProgreso
        this.valoresRawActividad = nuevosValoresRaw
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (esSeccionEspera) manejador.post(tareaActualizacion)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        manejador.removeCallbacks(tareaActualizacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHabitHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHabitHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(habito: HabitoEntity) {
            val contexto = binding.root.context
            binding.tvHabitIcon.text = habito.icono
            binding.tvHabitName.text = habito.nombre
            binding.tvHabitStreak.text = "🔥 ${habito.rachaActual}"
            
            val valorFinal = habito.obtenerPuntosFinales()

            if (habito.categoria == HabitCategory.DEPORTE || habito.tipoRecompensa == RecompensaTipo.GEMAS) {
                binding.ivRewardIcon.setImageResource(R.drawable.ic_gemas)
                binding.tvHabitRewardValue.text = contexto.getString(R.string.reward_value_gems, valorFinal / 10)
            } else {
                binding.ivRewardIcon.setImageResource(R.drawable.ic_monedas)
                binding.tvHabitRewardValue.text = contexto.getString(R.string.reward_value_coins, valorFinal)
            }

            if (esSeccionEspera) {
                binding.btnCompletar.text = contexto.getString(R.string.home_btn_done)
                
                if (habito.esVerificable) {
                    binding.progressActivity.visibility = View.VISIBLE
                    val progreso = progresoActividad[habito.id] ?: 0
                    val valorRaw = valoresRawActividad[habito.id] ?: "0 / ${habito.valorObjetivo.toInt()}"
                    
                    binding.progressActivity.setProgressCompat(progreso, true)
                    
                    binding.lottieLoading.visibility = if (progreso < 100) View.VISIBLE else View.GONE
                    binding.tvTimerCountdown.visibility = View.VISIBLE
                    binding.tvTimerCountdown.text = "$progreso% ($valorRaw)"
                    
                    // Bloquear el botón hasta que llegue al 100%
                    binding.btnCompletar.isEnabled = (progreso >= 100)
                } else {
                    binding.progressActivity.visibility = View.GONE
                    binding.lottieLoading.visibility = View.GONE
                    binding.tvTimerCountdown.visibility = View.VISIBLE
                    
                    val duracionEfectiva = if (habito.duracionMinutos > 0) habito.duracionMinutos else 2
                    val milisDuracion = duracionEfectiva * 60 * 1000L
                    val transcurrido = System.currentTimeMillis() - habito.tiempoInicioEspera
                    val restante = (milisDuracion - transcurrido).coerceAtLeast(0)
                    
                    val minutos = (restante / 1000) / 60
                    val segundos = (restante / 1000) % 60
                    binding.tvTimerCountdown.text = String.format("%02d:%02d", minutos, segundos)
                    
                    binding.btnCompletar.isEnabled = (restante <= 0)
                }
                binding.btnCompletar.setOnClickListener { alPulsarCompletar(habito, binding.ivRewardIcon) }
            } else {
                binding.btnCompletar.visibility = View.VISIBLE
                binding.btnCompletar.isEnabled = true
                binding.btnCompletar.text = contexto.getString(R.string.home_btn_start)
                
                // Mostrar progreso incluso si no está en espera para hábitos físicos
                if (habito.esVerificable) {
                    binding.progressActivity.visibility = View.VISIBLE
                    val progreso = progresoActividad[habito.id] ?: 0
                    val valorRaw = valoresRawActividad[habito.id] ?: "0 / ${habito.valorObjetivo.toInt()}"
                    
                    binding.progressActivity.setProgressCompat(progreso, true)
                    
                    binding.tvTimerCountdown.visibility = View.VISIBLE
                    binding.tvTimerCountdown.text = "$progreso% ($valorRaw)"
                } else {
                    binding.progressActivity.visibility = View.GONE
                    binding.tvTimerCountdown.visibility = View.GONE
                }

                binding.lottieLoading.visibility = View.GONE
                binding.btnCompletar.setOnClickListener { alPulsarCompletar(habito, binding.ivRewardIcon) }
            }
            
            binding.root.setOnLongClickListener {
                alPulsarLargo(habito)
                true
            }

            // botón para eliminar hábito si no se ha comenzado aún
            val puedeEliminar = !esSeccionEspera && !habito.completadoHoy && habito.tiempoInicioEspera == 0L
            binding.btnDeleteHabit.visibility = if (puedeEliminar) View.VISIBLE else View.GONE
            binding.btnDeleteHabit.setOnClickListener { alPulsarEliminar(habito) }

            // Lógica de Focus para Salud Mental
            if (habito.categoria == HabitCategory.SALUD_MENTAL && !habito.completadoHoy && !esSeccionEspera) {
                binding.btnFocus.visibility = View.VISIBLE
                binding.btnFocus.setOnClickListener { alPulsarFocus?.invoke(habito) }
                iniciarAnimacionPulso(binding.btnFocus)
            } else {
                binding.btnFocus.visibility = View.GONE
            }
        }

        private fun iniciarAnimacionPulso(vista: View) {
            val escalaX = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.15f, 1f)
            val escalaY = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.15f, 1f)
            android.animation.ObjectAnimator.ofPropertyValuesHolder(vista, escalaX, escalaY).apply {
                duration = 2000
                repeatCount = android.animation.ValueAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.RESTART
                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<HabitoEntity>() {
        override fun areItemsTheSame(oldItem: HabitoEntity, newItem: HabitoEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HabitoEntity, newItem: HabitoEntity): Boolean =
            oldItem.rachaActual == newItem.rachaActual && 
            oldItem.tiempoInicioEspera == newItem.tiempoInicioEspera &&
            oldItem == newItem
    }
}
