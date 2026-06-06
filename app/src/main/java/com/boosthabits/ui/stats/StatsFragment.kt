package com.boosthabits.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.boosthabits.R
import com.boosthabits.databinding.FragmentStatsBinding
import com.github.mikephil.charting.data.*
import java.time.LocalDate

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatsViewModel by viewModels()
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarTablaClasificacion()
        configurarGraficos()
        observarDatos()
    }

    private fun configurarTablaClasificacion() {
        leaderboardAdapter = LeaderboardAdapter()
        binding.rvLeaderboard.adapter = leaderboardAdapter
    }

    private fun observarDatos() {
        viewModel.totalPuntos.observe(viewLifecycleOwner) { puntos ->
            binding.tvTotalPuntos.text = (puntos ?: 0).toString()
            actualizarEstadoVacio()
        }

        viewModel.rachaMaxima.observe(viewLifecycleOwner) { racha ->
            binding.tvRachaActual.text = "$racha ${getString(R.string.stats_days)}"
        }

        viewModel.puntosSemanales.observe(viewLifecycleOwner) { lista ->
            val entries = lista.mapIndexed { index, puntosPorDia -> 
                Entry(index.toFloat(), puntosPorDia.puntos.toFloat()) 
            }
            actualizarGraficoLineas(entries)
            actualizarEstadoVacio()
        }

        viewModel.habitosCompletadosHoy.observe(viewLifecycleOwner) { completados ->
            val totalActivos = viewModel.totalHabitosActivos.value ?: 0
            val pendientes = (totalActivos - completados).coerceAtLeast(0)
            actualizarGraficoPie(completados.toFloat(), pendientes.toFloat())
        }

        viewModel.completadosPorDia.observe(viewLifecycleOwner) { lista ->
            val mapData = lista?.associate { 
                LocalDate.ofEpochDay(it.fecha) to it.conteo 
            } ?: emptyMap()
            
            binding.contributionMap.setData(mapData)
            actualizarEstadoVacio()
        }

        viewModel.logsAnuales.observe(viewLifecycleOwner) { data ->
            val safeData = data ?: emptyMap()
            binding.yearlyCalendarMap.SetDatosAnuales(safeData)
        }

        viewModel.leaderboard.observe(viewLifecycleOwner) { list ->
            leaderboardAdapter.submitList(list)
        }
    }

    private fun actualizarEstadoVacio() {
        val puntos = viewModel.totalPuntos.value ?: 0
        val tieneLog = !(viewModel.completadosPorDia.value.isNullOrEmpty())
        
        val isEmpty = puntos == 0 && !tieneLog
        
        binding.layoutStatsContent.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutEmptyStats.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun configurarGraficos() {

        val textColor = ContextCompat.getColor(requireContext(), R.color.color_on_surface)
        val gridColor = ContextCompat.getColor(requireContext(), R.color.color_outline).let {
            (it and 0x00FFFFFF) or 0x33000000 
        }

        binding.lineChart.apply {
            description.isEnabled = false
            setNoDataText(getString(R.string.stats_loading_weekly))
            setNoDataTextColor(textColor)
            
            xAxis.apply {
                setDrawGridLines(false)
                this.textColor = textColor
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            }
            axisLeft.apply {
                setDrawGridLines(true)
                this.gridColor = gridColor
                this.textColor = textColor
            }
            axisRight.isEnabled = false
            legend.textColor = textColor
            
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            
            animateY(1000)
        }

        binding.pieChart.apply {
            description.isEnabled = false
            setNoDataText(getString(R.string.stats_no_data_today))
            setNoDataTextColor(textColor)
            holeRadius = 60f
            transparentCircleRadius = 65f
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setEntryLabelColor(textColor)
            legend.textColor = textColor
            animateY(1000)
        }
    }

    private fun actualizarGraficoLineas(entries: List<Entry>) {
        if (entries.isEmpty()) return
        
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.color_primary)
        val textColor = ContextCompat.getColor(requireContext(), R.color.color_on_surface)

        val dataSet = LineDataSet(entries, getString(R.string.stats_coins_obtained)).apply {
            color = primaryColor
            setCircleColor(primaryColor)
            lineWidth = 3f
            circleRadius = 4f
            setDrawCircleHole(true)
            circleHoleColor = ContextCompat.getColor(requireContext(), R.color.color_surface)
            valueTextSize = 10f
            valueTextColor = textColor
            setDrawFilled(true)
            fillColor = primaryColor
            fillAlpha = 40
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }
        
        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.invalidate()
    }

    private fun actualizarGraficoPie(completados: Float, pendientes: Float) {
        if (completados == 0f && pendientes == 0f) {
            binding.pieChart.clear()
            return
        }

        val entradas = listOf(
            PieEntry(completados, getString(R.string.stats_completed)),
            PieEntry(pendientes, getString(R.string.stats_pending))
        )
        
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.color_primary)
        val secondaryColor = ContextCompat.getColor(requireContext(), R.color.color_primary_container)
        val textColor = ContextCompat.getColor(requireContext(), R.color.color_on_primary)

        val dataSet = PieDataSet(entradas, "").apply {
            colors = listOf(primaryColor, secondaryColor)
            valueTextSize = 14f
            valueTextColor = textColor
            setDrawValues(true)
        }
        
        binding.pieChart.data = PieData(dataSet).apply {
            setValueFormatter(com.github.mikephil.charting.formatter.DefaultValueFormatter(0))
        }
        binding.pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
