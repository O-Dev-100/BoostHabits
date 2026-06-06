package com.boosthabits.ui.auth

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.boosthabits.R
import com.boosthabits.databinding.ActivityForgotPasswordBinding
import com.boosthabits.ui.BaseActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Pantalla de recuperación de contraseña.
 */
class RecPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        com.boosthabits.ui.perfil.CosmeticoManager.setupTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.botonEnviar.setOnClickListener {
            val correo = binding.inputEmail.text?.toString()?.trim().orEmpty()
            if (correo.isBlank()) {
                mostrarMensaje(getString(R.string.auth_error_campos_vacios))
                return@setOnClickListener
            }
            viewModel.recuperarContrasena(correo)
        }

        viewModel.estado.observe(this) { estado ->
            binding.progress.visibility = if (estado.cargando) View.VISIBLE else View.GONE

            estado.mensajeError?.let { msg ->
                mostrarMensaje(msg)
                viewModel.limpiarMensajes()
            }

            estado.mensajeInfo?.let {
                mostrarMensaje(getString(R.string.auth_ok_email_enviado))
                viewModel.limpiarMensajes()
            }
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
    }
}

