package com.boosthabits.data.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Acceso centralizado a SharedPreferences para mantener el código simple y fácil de documentar.
 */
class PreferenciasApp(contexto: Context) {

    private val prefs: SharedPreferences =
        contexto.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE)

    var onboardingCompletado: Boolean
        get() = prefs.getBoolean(CLAVE_ONBOARDING_COMPLETADO, false)
        set(valor) {
            prefs.edit().putBoolean(CLAVE_ONBOARDING_COMPLETADO, valor).apply()
        }

    private companion object {
        private const val NOMBRE_PREFERENCIAS = "boosthabits_prefs"
        private const val CLAVE_ONBOARDING_COMPLETADO = "onboarding_completado"
    }
}

