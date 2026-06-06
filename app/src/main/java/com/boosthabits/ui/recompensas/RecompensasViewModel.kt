package com.boosthabits.ui.recompensas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.CurrencyType
import com.boosthabits.data.local.entity.RecompensaEntity
import com.boosthabits.data.local.entity.UserStatsEntity
import com.boosthabits.data.repository.RecompensasRepository
import kotlinx.coroutines.launch

// viewmodel para gestionar la logica de la pantalla de recompensas
class RecompensasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecompensasRepository
    // flujo de estadisticas del usuario (monedas y gemas)
    val userStats: LiveData<UserStatsEntity?>

    private val _redeemResult = MutableLiveData<Result<String>?>()
    val redeemResult: LiveData<Result<String>?> = _redeemResult

    private val _marketplaceRedeemResult = MutableLiveData<Result<String>?>()
    val marketplaceRedeemResult: LiveData<Result<String>?> = _marketplaceRedeemResult

    private val _previewReward = MutableLiveData<RecompensaEntity?>()
    val previewReward: LiveData<RecompensaEntity?> = _previewReward

    private val _coinFilter = MutableLiveData<String>("all")
    val coinFilter: LiveData<String> = _coinFilter

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RecompensasRepository(database, application)
        userStats = repository.obtenerEstadisticasUsuario().asLiveData()
        
        // Cargar datos de prueba si es necesario
        viewModelScope.launch {
            repository.insertarRecompensasDePrueba()
        }
    }

    fun getRewards(currencyType: CurrencyType): LiveData<List<RecompensaEntity>> {
        return repository.obtenerRecompensasPorMoneda(currencyType).asLiveData()
    }

    fun redeemReward(reward: RecompensaEntity) {
        viewModelScope.launch {
            val result = repository.canjearRecompensa(reward)
            _redeemResult.postValue(result)
        }
    }

    fun redeemMarketplaceOffer(offer: RecompensaEntity) {
        // lanzamos el canje de una oferta del marketplace
        viewModelScope.launch {
            val result = repository.canjearOfertaMarketplace(offer)
            _marketplaceRedeemResult.postValue(result)
        }
    }

    fun setPreviewReward(reward: RecompensaEntity?) {
        _previewReward.value = reward
    }

    fun setCoinFilter(filter: String) {
        _coinFilter.value = filter
    }

    fun clearRedeemResult() {
        _redeemResult.value = null
        _marketplaceRedeemResult.value = null
    }
}