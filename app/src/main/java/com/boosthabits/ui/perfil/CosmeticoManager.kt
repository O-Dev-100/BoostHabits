package com.boosthabits.ui.perfil

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.boosthabits.R
import com.boosthabits.data.local.entity.UserStatsEntity
import com.google.firebase.auth.FirebaseAuth

object CosmeticoManager {

    fun applyCosmetics(
        context: Context,
        stats: UserStatsEntity?,
        nameTextView: TextView?,
        avatarView: CustomAvatarView?,
        backgroundView: View? = null
    ) {
        if (stats == null) return

        val montserrat = ResourcesCompat.getFont(context, R.font.montserrat)
        val montserratBold = ResourcesCompat.getFont(context, R.font.montserrat_bold)

        // 1. Aplicar Nombre
        nameTextView?.let { tv ->
            // Resetear estilos y efectos antes de aplicar el nuevo
            tv.paint.shader = null
            tv.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
            tv.typeface = montserrat ?: Typeface.DEFAULT
            tv.paintFlags = tv.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            tv.paintFlags = tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            
            when (stats.idNombreEquipado) {
                "3" -> applyLegendaryNameEffect(tv, intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#FFFFFF"), Color.parseColor("#FFD700")), Color.parseColor("#FFEE58"))
                "4" -> applyLegendaryNameEffect(tv, intArrayOf(Color.parseColor("#FFC107"), Color.parseColor("#FFD700"), Color.parseColor("#FFC107")), Color.parseColor("#FFD700"))
                "5" -> applyLegendaryNameEffect(tv, intArrayOf(Color.parseColor("#FF8F00"), Color.parseColor("#FFB300"), Color.parseColor("#FF8F00")), Color.parseColor("#FFA000"))
                
                // Nuevas personalizaciones de texto con Montserrat
                "style_bold" -> tv.typeface = montserratBold ?: Typeface.DEFAULT_BOLD
                "style_italic" -> tv.typeface = Typeface.create(montserrat, Typeface.ITALIC)
                "style_bold_italic" -> tv.typeface = Typeface.create(montserratBold, Typeface.BOLD_ITALIC)
                "style_monospace" -> tv.typeface = Typeface.MONOSPACE
                "style_underline" -> {
                    tv.typeface = montserrat
                    tv.paintFlags = tv.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }
                
                // Nuevos colores de nombre
                "color_red" -> tv.setTextColor(Color.parseColor("#E53935"))
                "color_blue" -> tv.setTextColor(Color.parseColor("#1E88E5"))
                "color_green" -> tv.setTextColor(Color.parseColor("#43A047"))
                "color_purple" -> tv.setTextColor(Color.parseColor("#8E24AA"))
                
                else -> {
                    tv.setTextColor(context.getColorFromAttr(android.R.attr.textColorPrimary))
                }
            }
        }

        // 2. Aplicar Marco
        avatarView?.let {
            val frameName = when (stats.idMarcoAvatarEquipado) {
                "4", "5", "6" -> null 
                else -> null
            }
            it.setFrameByName(frameName)
        }

        // 3. Aplicar Foto de Perfil (PFP)
        avatarView?.imageView?.let { iv ->
            if (stats.idFotoPerfilEquipada != null) {
                val pfpResName = when(stats.idFotoPerfilEquipada) {
                    "9" -> "pfp_agua"
                    "10" -> "pfp_yoga"
                    "11" -> "pfp_apple"
                    "12" -> "pfp_libro"
                    "13" -> "pfp_salad"
                    "14" -> "pfp_shoes"
                    else -> null
                }
                
                if (pfpResName != null) {
                    val resId = context.resources.getIdentifier(pfpResName, "mipmap", context.packageName)
                    if (resId != 0) {
                        iv.setImageResource(resId)
                    }
                }
            } else {
                val user = FirebaseAuth.getInstance().currentUser
                user?.photoUrl?.let { url ->
                    iv.load(url) {
                        crossfade(true)
                        placeholder(R.mipmap.ic_launcher_round)
                        transformations(CircleCropTransformation())
                    }
                } ?: iv.setImageResource(R.mipmap.ic_launcher_round)
            }
        }

        // 4. Aplicar Fondo de Pantalla
        backgroundView?.let { bg ->
            val wallpaperResName = when(stats.idFondoPantallaEquipado) {
                "7" -> "fondo_claro"
                "8" -> "fondo_nublado"
                else -> null
            }

            if (wallpaperResName != null) {
                val resId = context.resources.getIdentifier(wallpaperResName, "drawable", context.packageName)
                if (resId != 0) {
                    bg.setBackgroundResource(resId)
                } else {
                    bg.setBackgroundResource(R.color.color_background)
                }
            } else {
                bg.setBackgroundResource(R.color.color_background)
            }
        }
    }

    private fun Context.getColorFromAttr(attr: Int): Int {
        val typedArray = obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, Color.BLACK)
        typedArray.recycle()
        return color
    }

    fun getThemeForWallpaper(wallpaperId: String?): Int {
        return when (wallpaperId) {
            "7" -> R.style.Theme_BoostHabits_Cloudy // Swapped: Claro -> Navy/Purple
            "8" -> R.style.Theme_BoostHabits_Light  // Swapped: Nublado -> Turquoise/White
            else -> R.style.Theme_BoostHabits
        }
    }

    fun setupTheme(context: Context) {
        val themeId = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            .getInt("cosmetic_theme", R.style.Theme_BoostHabits)
        context.setTheme(themeId)
    }

    fun applyLegendaryNameEffect(
        textView: TextView,
        colors: IntArray = intArrayOf(
            Color.parseColor("#FFD700"), // Gold
            Color.parseColor("#FFFFFF"), // White
            Color.parseColor("#FFD700")  // Gold
        ),
        glowColor: Int = Color.parseColor("#FFEE58")
    ) {
        val text = textView.text.toString()
        if (text.isEmpty()) return

        val paint = textView.paint
        val width = paint.measureText(text)
        if (width <= 0f) return
        
        val shader = LinearGradient(
            0f, 0f, width, textView.textSize,
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        
        textView.paint.shader = shader
        textView.setShadowLayer(15f, 0f, 0f, glowColor)
        textView.invalidate()
    }

    fun removeLegendaryNameEffect(textView: TextView, keepStyle: Boolean = false) {
        textView.paint.shader = null
        textView.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
        if (!keepStyle) {
            val montserrat = ResourcesCompat.getFont(textView.context, R.font.montserrat)
            textView.typeface = montserrat ?: Typeface.DEFAULT
            textView.paintFlags = textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        textView.invalidate()
    }
}
