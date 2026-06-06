package com.boosthabits

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.boosthabits.databinding.ActivityMainBinding
import androidx.core.view.updatePadding
import androidx.lifecycle.asLiveData
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.boosthabits.data.ReminderWorker
import com.boosthabits.data.health.HealthSyncWorker
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.ui.perfil.CosmeticoManager
import com.boosthabits.ui.BaseActivity
import com.boosthabits.utils.IdiomaManager
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import java.util.concurrent.TimeUnit

// actividad principal que gestiona la navegacion y servicios
class MainActivity : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var currentCosmeticThemeId: Int = -1

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scheduleReminder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        IdiomaManager.aplicarIdioma(this)
        
        CosmeticoManager.setupTheme(this)
        currentCosmeticThemeId = getSharedPreferences("theme_prefs", MODE_PRIVATE)
            .getInt("cosmetic_theme", R.style.Theme_BoostHabits)

        val themeMode = getSharedPreferences("theme_prefs", MODE_PRIVATE).getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        if (AppCompatDelegate.getDefaultNightMode() != themeMode) {
            AppCompatDelegate.setDefaultNightMode(themeMode)
        }

        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeCosmetics()
        scheduleHealthSync()
        checkNotificationPermission()
        startFabPulse()
    }

    private fun startFabPulse() {
        val scaleX = android.animation.ObjectAnimator.ofFloat(binding.fabMain, View.SCALE_X, 1f, 1.08f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(binding.fabMain, View.SCALE_Y, 1f, 1.08f)

        scaleX.repeatMode = android.animation.ValueAnimator.REVERSE
        scaleX.repeatCount = android.animation.ValueAnimator.INFINITE
        scaleY.repeatMode = android.animation.ValueAnimator.REVERSE
        scaleY.repeatCount = android.animation.ValueAnimator.INFINITE

        android.animation.AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 1000
            start()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                scheduleReminder()
            } else {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleReminder()
        }
    }

    private fun scheduleReminder() {
        val calendar = Calendar.getInstance()
        val nowMillis = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 20)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val initialDelay = calendar.timeInMillis - nowMillis
        
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            24, TimeUnit.HOURS
        ).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        val navHostView: View? = findViewById(R.id.nav_host_fragment_content_main)
        binding.bottomAppBar.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            navHostView?.let { container ->
                val bottomBarHeight = binding.bottomAppBar.height
                container.updatePadding(bottom = bottomBarHeight)
            }
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_stats,
                R.id.navigation_rewards,
                R.id.navigation_profile
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.menu.findItem(R.id.placeholder)?.isEnabled = false

        binding.fabMain.setOnClickListener {
            navController.navigate(R.id.categorySelectorFragment)
        }

        setupDrawer(navController)
    }

    private fun setupDrawer(navController: androidx.navigation.NavController) {
        val menu = binding.navigationView.menu
        
        val langItem = menu.findItem(R.id.menu_toggle_language)
        val langSwitch = langItem.actionView as? Switch
        val currentLang = IdiomaManager.getIdioma(this)
        langSwitch?.isChecked = currentLang == "en"
        langSwitch?.setOnCheckedChangeListener { _, isChecked ->
            val newLang = if (isChecked) "en" else "es"
            if (newLang != IdiomaManager.getIdioma(this)) {
                IdiomaManager.setIdioma(this, newLang)
                recreate()
            }
        }

        val themeItem = menu.findItem(R.id.menu_toggle_theme)
        val themeSwitch = themeItem.actionView as? Switch
        val isNightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        themeSwitch?.isChecked = isNightMode
        themeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
            getSharedPreferences("theme_prefs", MODE_PRIVATE).edit().putInt("night_mode", mode).apply()
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val bundle = when (menuItem.itemId) {
                R.id.privacyFragment -> Bundle().apply { putString("title", getString(R.string.nav_privacidad_titulo)) }
                R.id.legalFragment -> Bundle().apply { putString("title", getString(R.string.nav_legal_titulo)) }
                R.id.helpFragment -> Bundle().apply { putString("title", getString(R.string.nav_ayuda_titulo)) }
                else -> null
            }

            when (menuItem.itemId) {
                R.id.navigation_home -> navController.navigate(R.id.navigation_home)
                R.id.navigation_profile -> navController.navigate(R.id.navigation_profile)
                R.id.settingsFragment -> navController.navigate(R.id.settingsFragment)
                R.id.privacyFragment -> navController.navigate(R.id.privacyFragment, bundle)
                R.id.legalFragment -> navController.navigate(R.id.legalFragment, bundle)
                R.id.helpFragment -> navController.navigate(R.id.helpFragment, bundle)
                R.id.menu_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = android.content.Intent(this, com.boosthabits.ui.auth.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            if (menuItem.itemId != R.id.menu_toggle_language && menuItem.itemId != R.id.menu_toggle_theme) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            true
        }
    }

    private fun observeCosmetics() {
        val database = AppDatabase.getDatabase(this)
        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        
        database.userStatsDao().obtenerEstadisticasUsuario(userId).asLiveData().observe(this) { stats ->
            val newThemeId = CosmeticoManager.getThemeForWallpaper(stats?.idFondoPantallaEquipado)
            if (currentCosmeticThemeId != -1 && currentCosmeticThemeId != newThemeId) {
                getSharedPreferences("theme_prefs", MODE_PRIVATE).edit()
                    .putInt("cosmetic_theme", newThemeId)
                    .apply()
                recreate()
            }

            val mainContainer = findViewById<View>(R.id.main_content_container)
            CosmeticoManager.applyCosmetics(
                context = this,
                stats = stats,
                nameTextView = null,
                avatarView = null,
                backgroundView = mainContainer
            )
        }
    }

    private fun scheduleHealthSync() {
        val syncRequest = PeriodicWorkRequestBuilder<HealthSyncWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "HealthSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
