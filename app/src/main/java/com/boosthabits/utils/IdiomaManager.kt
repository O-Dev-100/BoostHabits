package com.boosthabits.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object IdiomaManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_IDIOMA = "selected_language"

    fun setIdioma(context: Context, languageCode: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_IDIOMA, languageCode).apply()
        updateResources(context, languageCode)
    }

    fun getIdioma(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_IDIOMA, "es") ?: "es"
    }

    fun aplicarIdioma(context: Context) {
        val idiomaCode = getIdioma(context)
        updateResources(context, idiomaCode)
    }

    fun wrapContext(context: Context): Context {
        val idiomaCode = getIdioma(context)
        val locale = Locale(idiomaCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }

    private fun updateResources(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        // para asegurar que el contexto de la aplicación también se actualiza
        val appContext = context.applicationContext
        appContext.resources.updateConfiguration(config, appContext.resources.displayMetrics)
    }
}