package com.boosthabits.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.boosthabits.R
import com.boosthabits.databinding.FragmentProfileBinding
import com.boosthabits.ui.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider

class ProfileFragment : Fragment() {



    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarInfoUsuario()
        configurarListeners()
        observarEstado()
        observarCosmeticos(view)
    }

    private fun observarEstado() {
        viewModel.profileUpdateResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {
                Snackbar.make(binding.root, R.string.profile_update_success, Snackbar.LENGTH_SHORT).show()
                configurarInfoUsuario()
                viewModel.limpiarResultados()
            }?.onFailure { ex ->
                Snackbar.make(binding.root, getString(R.string.profile_update_error, ex.message), Snackbar.LENGTH_LONG).show()
                viewModel.limpiarResultados()
            }
        }

        viewModel.passwordUpdateResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {
                Snackbar.make(binding.root, R.string.profile_password_success, Snackbar.LENGTH_SHORT).show()
                viewModel.limpiarResultados()
            }?.onFailure { ex ->
                Snackbar.make(binding.root, getString(R.string.profile_update_error, ex.message), Snackbar.LENGTH_LONG).show()
                viewModel.limpiarResultados()
            }
        }
    }

    private fun configurarInfoUsuario() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvProfileName.text = user.displayName ?: user.email?.substringBefore("@") ?: getString(R.string.profile_default_user)
            binding.tvProfileEmail.text = user.email ?: getString(R.string.profile_no_email)
        }
    }

    private fun observarCosmeticos(rootView: View) {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            CosmeticoManager.applyCosmetics(
                context = requireContext(),
                stats = stats,
                nameTextView = binding.tvProfileName,
                avatarView = binding.ivProfilePicture,
                backgroundView = rootView
            )
        }}

    private fun configurarListeners() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        
        binding.btnEditProfile.setOnClickListener {
            mostrarDialogoEditarNombre()
        }

        binding.btnChangePassword.setOnClickListener {
            val user = auth.currentUser
            val isEmailUser = user?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true
            if (isEmailUser) {
                mostrarDialogoCambiarPassword()
            } else {
                Snackbar.make(binding.root, R.string.profile_email_only, Snackbar.LENGTH_LONG).show()
            }
        }

        binding.btnPersonalization.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_personalizationFragment)
        }

        binding.btnCoupons.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_myCouponsFragment)
        }

        // Nuevos listeners para Información y Ayuda
        binding.btnPrivacy.setOnClickListener {
            val bundle = Bundle().apply { putString("title", getString(R.string.nav_privacidad)) }
            findNavController().navigate(R.id.action_navigation_profile_to_privacyFragment, bundle)
        }
        binding.btnLegal.setOnClickListener {
            val bundle = Bundle().apply { putString("title", getString(R.string.nav_legal)) }
            findNavController().navigate(R.id.action_navigation_profile_to_legalFragment, bundle)
        }
        binding.btnHelp.setOnClickListener {
            val bundle = Bundle().apply { putString("title", getString(R.string.nav_ayuda)) }
            findNavController().navigate(R.id.action_navigation_profile_to_helpFragment, bundle)
        }
    }

    private fun mostrarDialogoEditarNombre() {
        val input = TextInputEditText(requireContext())
        input.setText(auth.currentUser?.displayName)
        input.setHint(R.string.profile_edit_name_hint)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_edit_name_title)
            .setView(input)
            .setPositiveButton(R.string.profile_save) { _, _ ->
                val nuevoNombre = input.text?.toString()?.trim().orEmpty()
                if (nuevoNombre.isNotBlank()) {
                    viewModel.actualizarNombre(nuevoNombre)
                }
            }
            .setNegativeButton(R.string.home_delete_negative, null)
            .show()
    }

    private fun mostrarDialogoCambiarPassword() {
        val input = TextInputEditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.setHint(R.string.auth_hint_password)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_change_password)
            .setMessage(R.string.auth_error_password_corta)
            .setView(input)
            .setPositiveButton(R.string.profile_change_password_btn) { _, _ ->
                val nuevaPass = input.text?.toString().orEmpty()
                if (nuevaPass.length >= 6) {
                    viewModel.cambiarPassword(nuevaPass)
                } else {
                    Snackbar.make(binding.root, R.string.profile_password_too_short, Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.home_delete_negative, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
