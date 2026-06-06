package com.boosthabits.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.boosthabits.MainActivity
import com.boosthabits.R
import com.boosthabits.databinding.ActivityLoginBinding
import com.boosthabits.ui.BaseActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar

/**
 * Pantalla de login (email/contraseña + Google).
 */
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    private val launcherGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val tarea = GoogleSignIn.getSignedInAccountFromIntent(resultado.data)
        try {
            val cuenta = tarea.getResult(ApiException::class.java)
            val idToken = cuenta.idToken
            if (!idToken.isNullOrBlank()) {
                viewModel.iniciarSesionGoogleConIdToken(idToken)
            } else {
                mostrarError("No se pudo obtener el token de ID de Google.")
            }
        } catch (ex: ApiException) {
            val extraInfo = when (ex.statusCode) {
                10 -> " (Error de configuración/SHA-1)" //este error me pasó porque configuré la consola firebase de la aplicación que desarrollé en mis prácticas con la misma credencial SHA-1 (se consigue por comando, es única para cada equipo)
                7 -> " (Error de red)"
                12500 -> " (Error de Play Services)"
                else -> ""
            }
            mostrarError("Error Google ${ex.statusCode}$extraInfo: ${ex.statusMessage ?: ex.message}")
        } catch (ex: Throwable) {
            mostrarError(ex.message ?: "Error desconocido con Google")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        com.boosthabits.ui.perfil.CosmeticoManager.setupTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.botonLogin.setOnClickListener {
            val correo = binding.inputEmail.text?.toString()?.trim().orEmpty()
            val contrasena = binding.inputPassword.text?.toString().orEmpty()
            if (correo.isBlank() || contrasena.isBlank()) {
                mostrarError(getString(R.string.auth_error_campos_vacios))
                return@setOnClickListener
            }
            viewModel.iniciarSesionEmail(correo, contrasena)
        }

        binding.botonGoogle.setOnClickListener {
            iniciarGoogleSignIn()
        }

        binding.linkCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        binding.linkOlvidoPassword.setOnClickListener {
            startActivity(Intent(this, RecPasswordActivity::class.java))
        }

        observarEstado()
    }

    private fun iniciarGoogleSignIn() {
        val webClientId = getString(R.string.google_web_client_id)
        val opciones = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
        val cliente = GoogleSignIn.getClient(this, opciones)
        launcherGoogle.launch(cliente.signInIntent)
    }

    private fun observarEstado() {
        viewModel.estado.observe(this) { estado ->
            binding.progress.visibility = if (estado.cargando) View.VISIBLE else View.GONE

            estado.mensajeError?.let { msg ->
                mostrarError(msg)
                viewModel.limpiarMensajes()
            }

            estado.usuario?.let {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
    }
}

