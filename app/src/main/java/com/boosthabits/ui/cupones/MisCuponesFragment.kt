package com.boosthabits.ui.cupones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.boosthabits.data.local.AppDatabase
import com.boosthabits.data.local.entity.CuponEntity
import com.boosthabits.databinding.FragmentMyCouponsBinding
import com.boosthabits.databinding.ItemCouponBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MisCuponesFragment : Fragment() {

    private var _binding: FragmentMyCouponsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CuponesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCouponsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CuponesAdapter()
        binding.rvCoupons.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            database.rewardDao().obtenerCuponesUsuario(userId).collectLatest { cupones ->
                adapter.submitList(cupones)
                binding.tvEmptyCoupons.visibility = if (cupones.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class CuponesAdapter : androidx.recyclerview.widget.ListAdapter<CuponEntity, CuponesAdapter.ViewHolder>(COUPON_DIFF_CALLBACK) {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCouponBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ViewHolder(private val binding: ItemCouponBinding) : RecyclerView.ViewHolder(binding.root) {
            private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            fun bind(cupon: CuponEntity) {
                binding.tvCouponTitle.text = cupon.titulo
                binding.tvCouponCost.text = "${cupon.coste} Gemas"
                binding.tvCouponDate.text = "Reclamado el: ${dateFormat.format(Date(cupon.canjeadoEn))}"
                binding.tvCouponCode.text = cupon.codigo
            }  }}

    // con este bloque el recyclerview actualiza los cupones que realmente cambian
    companion object {
        private val COUPON_DIFF_CALLBACK = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<CuponEntity>() {
            override fun areItemsTheSame(oldItem: CuponEntity, newItem: CuponEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: CuponEntity, newItem: CuponEntity) = oldItem == newItem
        }
    }
}
