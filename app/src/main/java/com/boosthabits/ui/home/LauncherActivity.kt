package com.boosthabits.ui.home

import android.content.Intent
import android.os.Bundle
import com.boosthabits.MainActivity
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.prefs.PreferenciasApp
import com.boosthabits.data.repository.EconomiaRepository
import com.boosthabits.ui.BaseActivity
import com.boosthabits.ui.auth.LoginActivity
import com.boosthabits.ui.bienvenida.BienvenidaActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class LauncherActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        com.boosthabits.ui.perfil.CosmeticoManager.setupTheme(this)
        super.onCreate(savedInstanceState)

        val prefs = PreferenciasApp(this)
        val usuarioActual = FirebaseAuth.getInstance().currentUser

        if (!prefs.onboardingCompletado) {
            startActivity(Intent(this, BienvenidaActivity::class.java))
            finish()
            return
        }

        if (usuarioActual != null) {
            val db = AppDatabase.getDatabase(applicationContext)
            val economiaRepository = EconomiaRepository(db)
            val habitoRepository = com.boosthabits.data.repository.HabitoRepository(db, applicationContext)
            
            lifecycleScope.launch {
                // sincronización completa al arrancar si ya hay sesión
                economiaRepository.sincronizarEstadisticasUsuarioSeguro(usuarioActual.uid)
                habitoRepository.sincronizarTodoDesdeFirestore(usuarioActual.uid)

                startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
                finish()
            }
            return
        }

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

