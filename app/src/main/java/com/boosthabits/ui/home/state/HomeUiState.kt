package com.boosthabits.ui.home.state

import com.boosthabits.data.local.entity.HabitoEntity
import com.boosthabits.data.local.entity.UserStatsEntity

data class HomeUiState(
    val habitosPendientes: List<HabitoEntity> = emptyList(),
    val habitosEnEspera: List<HabitoEntity> = emptyList(),
    val estadisticasUsuario: UserStatsEntity? = null,
    val totalMonedas: Int = 0,
    val totalGemas: Int = 0,
    val completadosHoy: Int = 0,
    val estaCargando: Boolean = false,
    val error: String? = null,
    val motivationalMessage: String = ""
)
