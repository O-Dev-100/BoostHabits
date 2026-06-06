package com.boosthabits.ui.perfil

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.boosthabits.R
import com.boosthabits.databinding.ViewCustomAvatarBinding
import com.airbnb.lottie.LottieAnimationView

class CustomAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCustomAvatarBinding

    init {

        binding = ViewCustomAvatarBinding.inflate(LayoutInflater.from(context), this)
    }

    val imageView: ImageView get() = binding.ivAvatar
    val lottieView: LottieAnimationView get() = binding.lottieFrame

    // los marcos/frames para el avatar del usuario a modo de customización han sido desechados por ahora.
    fun setFrame(lottieRes: Int?) {
        if (lottieRes != null) {
            binding.lottieFrame.setAnimation(lottieRes)
            binding.lottieFrame.playAnimation()
            binding.lottieFrame.visibility = View.VISIBLE
        } else {
            binding.lottieFrame.cancelAnimation()
            binding.lottieFrame.visibility = View.GONE
        }
    }

    fun setFrameByName(frameName: String?) {
        val resId = when (frameName) {
            "fire" -> R.raw.frame_fire
            "electric" -> R.raw.frame_electric
            "flowers" -> R.raw.frame_flowers
            else -> null
        }
        setFrame(resId)
    }
}
