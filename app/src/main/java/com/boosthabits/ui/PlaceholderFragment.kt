package com.boosthabits.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boosthabits.R
import com.boosthabits.databinding.FragmentInfoTextBinding

class PlaceholderFragment : Fragment() {
    private var _binding: FragmentInfoTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInfoTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val title = arguments?.getString("title") ?: getString(R.string.nav_ayuda)
        binding.toolbarInfo.title = title
        binding.toolbarInfo.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvContent.text = getContentForTitle(title)
    }

    private fun getContentForTitle(title: String): String {
        return when (title) {
            getString(R.string.nav_privacidad) -> getString(R.string.privacy_content)
            getString(R.string.nav_legal) -> getString(R.string.legal_content)
            getString(R.string.nav_ayuda) -> getString(R.string.help_content)
            else -> getString(R.string.placeholder_development, title)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}