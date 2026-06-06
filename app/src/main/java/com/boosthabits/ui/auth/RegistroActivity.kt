package com.boosthabits.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.boosthabits.MainActivity
import com.boosthabits.R
import com.boosthabits.databinding.ActivityRegisterBinding
import com.boosthabits.ui.BaseActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Pantalla de registro con email/contraseña.
 *
 * Requisito del TFG: al registrarse debe crearse `users/{uid}` en Firestore.
 * Esto se realiza desde AuthRepository tras `createUserWithEmailAndPassword`.
 */
class RegistroActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        com.boosthabits.ui.perfil.CosmeticoManager.setupTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.linkYaTengoCuenta.setOnClickListener {
            finish()
        }

        binding.botonRegistro.setOnClickListener {
            val nombre = binding.inputNombre.text?.toString()?.trim().orEmpty()
            val correo = binding.inputEmail.text?.toString()?.trim().orEmpty()
            val contrasena = binding.inputPassword.text?.toString().orEmpty()

            if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                mostrarError(getString(R.string.auth_error_campos_vacios))
                return@setOnClickListener
            }

            if (contrasena.length < 6) {
                mostrarError(getString(R.string.auth_error_password_corta))
                return@setOnClickListener
            }

            viewModel.registrarUsuarioEmail(correo, contrasena, nombre)
        }

        viewModel.estado.observe(this) { estado ->
            binding.progress.visibility = if (estado.cargando) View.VISIBLE else View.GONE

            estado.mensajeError?.let { msg ->
                mostrarError(msg)
                viewModel.limpiarMensajes()
            }

            estado.usuario?.let {
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
    }
}

