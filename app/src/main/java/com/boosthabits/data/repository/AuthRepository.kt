package com.boosthabits.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// repositorio para autenticacion y perfil
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    fun usuarioActual(): FirebaseUser? = auth.currentUser

    fun cerrarSesion() {
        auth.signOut()
    }

    fun iniciarSesionConEmail(
        correo: String,
        contrasena: String,
        onOk: (FirebaseUser) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        auth.signInWithEmailAndPassword(correo, contrasena)
            .addOnSuccessListener { resultado ->
                val usuario = resultado.user
                if (usuario != null) onOk(usuario) else onError(IllegalStateException("Usuario nulo"))
            }
            .addOnFailureListener { ex -> onError(ex) }
    }

    fun registrarConEmail(
        correo: String,
        contrasena: String,
        nombrePantalla: String,
        onOk: (FirebaseUser) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnSuccessListener { resultado ->
                val usuario = resultado.user
                if (usuario == null) {
                    onError(IllegalStateException("Usuario nulo"))
                    return@addOnSuccessListener
                }

                actualizarOCrearDocumentoUsuario(
                    uid = usuario.uid,
                    correo = correo,
                    nombrePantalla = nombrePantalla,
                    onOk = { onOk(usuario) },
                    onError = onError
                )
            }
            .addOnFailureListener { ex -> onError(ex) }
    }

    fun enviarRecuperacionPassword(
        correo: String,
        onOk: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        auth.sendPasswordResetEmail(correo)
            .addOnSuccessListener { onOk() }
            .addOnFailureListener { ex -> onError(ex) }
    }

    fun actualizarPerfil(
        nombrePantalla: String,
        onOk: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val usuario = auth.currentUser ?: return onError(IllegalStateException("No hay sesión activa"))
        
        val updates = UserProfileChangeRequest.Builder()
            .setDisplayName(nombrePantalla)
            .build()

        usuario.updateProfile(updates)
            .addOnSuccessListener {
                firestore.collection("usuarios").document(usuario.uid)
                    .update("nombre_pantalla", nombrePantalla)
                    .addOnSuccessListener { onOk() }
                    .addOnFailureListener { ex -> onError(ex) }
            }
            .addOnFailureListener { ex -> onError(ex) }
    }

    fun cambiarPassword(
        nuevaContrasena: String,
        onOk: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val usuario = auth.currentUser ?: return onError(IllegalStateException("No hay sesión activa"))
        usuario.updatePassword(nuevaContrasena)
            .addOnSuccessListener { onOk() }
            .addOnFailureListener { ex -> onError(ex) }
    }

    // login con google o proveedores externos
    fun iniciarSesionConCredenciales(
        credential: AuthCredential,
        onOk: (FirebaseUser) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { resultado ->
                val usuario = resultado.user
                if (usuario == null) {
                    onError(IllegalStateException("Usuario nulo"))
                    return@addOnSuccessListener
                }

                val correo = usuario.email ?: ""
                val nombrePantalla = usuario.displayName ?: ""
                
                actualizarOCrearDocumentoUsuario(
                    uid = usuario.uid,
                    correo = correo,
                    nombrePantalla = nombrePantalla,
                    onOk = { onOk(usuario) },
                    onError = onError
                )
            }
            .addOnFailureListener { ex -> onError(ex) }
    }

    // asegura que el usuario tenga un perfil en firestore
    private fun actualizarOCrearDocumentoUsuario(
        uid: String,
        correo: String,
        nombrePantalla: String,
        onOk: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val docRef = firestore.collection("usuarios").document(uid)
        docRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val updates = mutableMapOf<String, Any>()
                    if (snapshot.getString("correo").isNullOrBlank() && correo.isNotBlank()) {
                        updates["correo"] = correo
                    }
                    if (snapshot.getString("nombre_pantalla").isNullOrBlank() && nombrePantalla.isNotBlank()) {
                        updates["nombre_pantalla"] = nombrePantalla
                    }
                    
                    if (updates.isNotEmpty()) {
                        docRef.update(updates).addOnCompleteListener { onOk() }
                    } else {
                        onOk()
                    }
                    return@addOnSuccessListener
                }

                val datos = hashMapOf(
                    "correo" to correo,
                    "nombre_pantalla" to nombrePantalla,
                    "creado_el" to FieldValue.serverTimestamp(),
                    "gemas_totales" to 0,
                    "monedas_totales" to 0,
                    "es_premium" to false
                )

                docRef.set(datos)
                    .addOnSuccessListener { onOk() }
                    .addOnFailureListener { ex -> onError(ex) }
            }
            .addOnFailureListener { ex -> onError(ex) }
    }
}
