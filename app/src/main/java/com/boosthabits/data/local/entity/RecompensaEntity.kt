package com.boosthabits.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.io.Serializable

enum class CurrencyType {
    GEMA, MONEDA
}

enum class RewardItemType {
    CUPON, COSMETICO
}

@Entity(tableName = "recompensas")
data class RecompensaEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String,
    val coste: Int,
    val tipoMoneda: CurrencyType,
    val tipoItem: RewardItemType,
    val urlImagen: String,
    val estaActivo: Boolean = true,
    val urlImagenMarca: String? = null,
    val urlExterna: String? = null,
    val terminosYCondiciones: String? = null,
    val stock: Int? = null,
    val codigoVoucher: String? = null,
    val resTitulo: Int = 0,
    val resDescripcion: Int = 0
) : Serializable
