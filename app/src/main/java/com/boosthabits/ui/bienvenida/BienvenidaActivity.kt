package com.boosthabits.ui.bienvenida

import android.content.Intent
import android.os.Bundle
import com.boosthabits.data.prefs.PreferenciasApp
import com.boosthabits.databinding.ActivityOnboardingBinding
import com.boosthabits.ui.BaseActivity
import com.boosthabits.ui.auth.LoginActivity

/**
 * Onboarding simple para el primer arranque.
 *
 * Requisito del TFG: marcar una bandera en SharedPreferences para no volver a mostrar esta pantalla.
 */
class BienvenidaActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // se fuerza el modo claro para la bienvenida, sino el banner de bienvenida no se distingue bien
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        com.boosthabits.ui.perfil.CosmeticoManager.setupTheme(this)
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.botonEmpezar.setOnClickListener {
            PreferenciasApp(this).onboardingCompletado = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }}

