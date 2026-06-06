package com.boosthabits.data.repository

import com.boosthabits.R
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import java.util.*

class RecompensasRepository(private val database: AppDatabase, private val context: android.content.Context) {

    private val rewardDao = database.rewardDao()
    private val userStatsDao = database.userStatsDao()
    private val auth = FirebaseAuth.getInstance()
    private val dbFirestore = FirebaseFirestore.getInstance()
    private val economiaRepository = EconomiaRepository(database, dbFirestore)

    fun obtenerRecompensasPorMoneda(tipoMoneda: CurrencyType): Flow<List<RecompensaEntity>> {
        val idUsuario = auth.currentUser?.uid ?: ""
        return rewardDao.obtenerRecompensasDisponiblesPorMoneda(tipoMoneda, idUsuario)
    }

    fun obtenerEstadisticasUsuario(): Flow<UserStatsEntity?> {
        val idUsuario = auth.currentUser?.uid ?: ""
        return userStatsDao.obtenerEstadisticasUsuario(idUsuario)
    }

    suspend fun canjearRecompensa(recompensa: RecompensaEntity): Result<String> {
        val idUsuario = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        val estadisticas = userStatsDao.obtenerEstadisticasUsuarioUnaVez(idUsuario) ?: return Result.failure(Exception("No se encontraron estadísticas"))

        val tieneSaldo = when (recompensa.tipoMoneda) {
            CurrencyType.GEMA -> estadisticas.gemas >= recompensa.coste
            CurrencyType.MONEDA -> estadisticas.monedas >= recompensa.coste
        }

        if (!tieneSaldo) {
            return Result.failure(Exception("Saldo insuficiente"))
        }

        when (recompensa.tipoMoneda) {
            CurrencyType.GEMA -> economiaRepository.sumarGemasYSincronizar(idUsuario, -recompensa.coste)
            CurrencyType.MONEDA -> economiaRepository.sumarMonedasYSincronizar(idUsuario, -recompensa.coste)
        }

        return when (recompensa.tipoItem) {
            RewardItemType.CUPON -> {
                val codigo = generarCodigoAleatorio()
                val cupon = CuponEntity(
                    idUsuario = idUsuario,
                    idRecompensa = recompensa.id,
                    titulo = recompensa.titulo,
                    codigo = codigo
                )
                rewardDao.insertarCupon(cupon)
                Result.success(context.getString(R.string.rewards_redeem_success_coupon, codigo))
            }
            RewardItemType.COSMETICO -> {
                val cosmetico = PersonalizacionEntity(
                    idUsuario = idUsuario,
                    idRecompensa = recompensa.id
                )
                rewardDao.insertarCosmeticoDesbloqueado(cosmetico)
                
                val estadisticasActuales = userStatsDao.obtenerEstadisticasUsuarioUnaVez(idUsuario)
                if (estadisticasActuales != null) {
                    val nuevasEstadisticas = when {
                        recompensa.titulo.contains("Nombre", ignoreCase = true) || recompensa.id in listOf("3", "4", "5") || recompensa.id.startsWith("style_") || recompensa.id.startsWith("color_") ->
                            estadisticasActuales.copy(idNombreEquipado = recompensa.id)
                        recompensa.titulo.contains("Fondo", ignoreCase = true) || recompensa.id in listOf("7", "8") -> 
                            estadisticasActuales.copy(idFondoPantallaEquipado = recompensa.id)
                        recompensa.titulo.contains("Perfil", ignoreCase = true) || recompensa.id in listOf("9", "10", "11", "12", "13", "14") -> 
                            estadisticasActuales.copy(idFotoPerfilEquipada = recompensa.id)
                        else -> estadisticasActuales
                    }
                    userStatsDao.upsert(nuevasEstadisticas)
                    economiaRepository.sincronizarLocalAFirebaseSeguro(idUsuario)
                }
                
                Result.success(context.getString(R.string.rewards_redeem_success_cosmetic))
            }
        }
    }

    private fun generarCodigoAleatorio(): String {
        return UUID.randomUUID().toString().substring(0, 8).uppercase()
    }

    suspend fun canjearOfertaMarketplace(oferta: RecompensaEntity): Result<String> {
        val idUsuario = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
        val estadisticas = userStatsDao.obtenerEstadisticasUsuarioUnaVez(idUsuario) ?: return Result.failure(Exception("No se encontraron estadísticas"))

        if (estadisticas.gemas < oferta.coste) {
            return Result.failure(Exception("Saldo insuficiente"))
        }

        economiaRepository.sumarGemasYSincronizar(idUsuario, -oferta.coste)
        
        val codigo = oferta.codigoVoucher ?: generarCodigoAleatorio()
        
        val cupon = CuponEntity(
            idUsuario = idUsuario,
            idRecompensa = oferta.id,
            titulo = oferta.titulo,
            codigo = codigo,
            coste = oferta.coste
        )
        rewardDao.insertarCupon(cupon)

        return Result.success(codigo)
    }

    suspend fun insertarRecompensasDePrueba() {
        val recompensas = listOf(
            RecompensaEntity(
                id = "m1",
                titulo = "30% de descuento en Nike",
                descripcion = "Válido para toda la colección de running.",
                coste = 25,
                tipoMoneda = CurrencyType.GEMA,
                tipoItem = RewardItemType.CUPON,
                urlImagen = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=1000&auto=format&fit=crop",
                urlImagenMarca = "ic_nike",
                terminosYCondiciones = "• Un uso por cliente.\n• Válido hasta fin de mes.\n• No acumulable con otras ofertas.",
                urlExterna = "https://www.nike.com",
                stock = 100,
                resTitulo = R.string.reward_nike_title,
                resDescripcion = R.string.reward_nike_desc
            ),
            RecompensaEntity(
                id = "m2",
                titulo = "Envío GRATIS en Adidas",
                descripcion = "Sin pedido mínimo para miembros de BoostHabits.",
                coste = 4,
                tipoMoneda = CurrencyType.GEMA,
                tipoItem = RewardItemType.CUPON,
                urlImagen = "https://images.unsplash.com/photo-1518002171953-a080ee817e1f?q=80&w=1000&auto=format&fit=crop",
                urlImagenMarca = "ic_adidas",
                terminosYCondiciones = "• Aplicable a envíos estándar.\n• Válido en toda la web.",
                urlExterna = "https://www.adidas.com",
                stock = 50,
                resTitulo = R.string.reward_adidas_title,
                resDescripcion = R.string.reward_adidas_desc
            ),
            RecompensaEntity(
                id = "m3",
                titulo = "50% Descuento en Shein",
                descripcion = "Consultar requisitos mínimos para efecutar el cupón",
                coste = 50,
                tipoMoneda = CurrencyType.GEMA,
                tipoItem = RewardItemType.CUPON,
                urlImagen = "img_shein",
                urlImagenMarca = "ic_shein",
                terminosYCondiciones = "• Aplicable en pedidos superiores a 29€.\n• Válido para nuevos usuarios y existentes.",
                stock = 10,
                codigoVoucher = "SHEIN-BOOST-50",
                resTitulo = R.string.reward_shein_title,
                resDescripcion = R.string.reward_shein_desc
            ),
            RecompensaEntity("3", "Nombre Legendario", "Efecto de gradiente dorado y brillo", 500, CurrencyType.MONEDA, RewardItemType.COSMETICO, "", resTitulo = R.string.reward_legendary_name_title, resDescripcion = R.string.reward_legendary_name_desc),
            RecompensaEntity("4", "Nombre Oro Puro", "Efecto metálico dorado intenso", 600, CurrencyType.MONEDA, RewardItemType.COSMETICO, "", resTitulo = R.string.reward_gold_name_title, resDescripcion = R.string.reward_gold_name_desc),
            RecompensaEntity("5", "Nombre Ámbar", "Efecto de brillo ámbar cálido", 400, CurrencyType.MONEDA, RewardItemType.COSMETICO, "", resTitulo = R.string.reward_amber_name_title, resDescripcion = R.string.reward_amber_name_desc),
            RecompensaEntity("style_bold", "Nombre en Negrita", "Resalta tu nombre con grosor extra", 150, CurrencyType.MONEDA, RewardItemType.COSMETICO, ""),
            RecompensaEntity("style_italic", "Nombre en Cursiva", "Dale un toque elegante e inclinado", 150, CurrencyType.MONEDA, RewardItemType.COSMETICO, ""),
            RecompensaEntity("style_bold_italic", "Nombre Pro", "Combinación de negrita y cursiva", 250, CurrencyType.MONEDA, RewardItemType.COSMETICO, ""),
            RecompensaEntity("style_monospace", "Estilo Código", "Fuente de ancho fijo retro", 200, CurrencyType.MONEDA, RewardItemType.COSMETICO, ""),
            RecompensaEntity("style_underline", "Nombre Subrayado", "Añade una línea bajo tu nombre", 125, CurrencyType.MONEDA, RewardItemType.COSMETICO, ""),
            RecompensaEntity("color_red", "Nombre Rojo", "Destaca con un color vibrante", 100, CurrencyType.MONEDA, RewardItemType.COSMETICO, ""),
            RecompensaEntity("color_blue", "Nombre Azul", "Un tono profesional y sereno", 100, CurrencyType.MONEDA, RewardItemType.COSMETICO, ""),
            RecompensaEntity("color_green", "Nombre Verde", "Color de vitalidad y naturaleza", 100, CurrencyType.MONEDA, RewardItemType.COSMETICO, ""),
            RecompensaEntity("color_purple", "Nombre Púrpura", "Elegancia y creatividad", 100, CurrencyType.MONEDA, RewardItemType.COSMETICO, ""),
            RecompensaEntity("7", "Fondo Claro", "Tema nocturno azul marino con morado", 600, CurrencyType.MONEDA, RewardItemType.COSMETICO, "fondo_claro", resTitulo = R.string.reward_bg_light_title, resDescripcion = R.string.reward_bg_light_desc),
            RecompensaEntity("8", "Fondo Nublado", "Tema turquesa y blanco gradiente", 600, CurrencyType.MONEDA, RewardItemType.COSMETICO, "fondo_nublado", resTitulo = R.string.reward_bg_cloudy_title, resDescripcion = R.string.reward_bg_cloudy_desc),
            RecompensaEntity("9", "Perfil Agua", "Icono de gota refrescante", 250, CurrencyType.MONEDA, RewardItemType.COSMETICO, "pfp_agua", resTitulo = R.string.reward_pfp_water_title, resDescripcion = R.string.reward_pfp_water_desc),
            RecompensaEntity("10", "Perfil Yoga", "Icono de equilibrio y paz", 250, CurrencyType.MONEDA, RewardItemType.COSMETICO, "pfp_yoga", resTitulo = R.string.reward_pfp_yoga_title, resDescripcion = R.string.reward_pfp_yoga_desc),
            RecompensaEntity("11", "Perfil Manzana", "Icono de salud y nutrición", 250, CurrencyType.MONEDA, RewardItemType.COSMETICO, "pfp_apple", resTitulo = R.string.reward_pfp_apple_title, resDescripcion = R.string.reward_pfp_apple_desc),
            RecompensaEntity("12", "Perfil Libro", "Icono de sabiduría y lectura", 250, CurrencyType.MONEDA, RewardItemType.COSMETICO, "pfp_libro", resTitulo = R.string.reward_pfp_book_title, resDescripcion = R.string.reward_pfp_book_desc),
            RecompensaEntity("13", "Perfil Ensalada", "Icono de comida saludable", 250, CurrencyType.MONEDA, RewardItemType.COSMETICO, "pfp_salad", resTitulo = R.string.reward_pfp_salad_title, resDescripcion = R.string.reward_pfp_salad_desc),
            RecompensaEntity("14", "Perfil Zapatillas", "Icono de deporte y acción", 250, CurrencyType.MONEDA, RewardItemType.COSMETICO, "pfp_shoes", resTitulo = R.string.reward_pfp_shoes_title, resDescripcion = R.string.reward_pfp_shoes_desc)
        )

        rewardDao.insertarRecompensas(recompensas)
    }
}
