package com.boosthabits.ui.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ContributionMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: Map<LocalDate, Int> = emptyMap()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        textSize = 28f
    }

    // Configuración de dimensiones
    private val cellSize = 36f
    private val gap = 8f
    private val cornerRadius = 6f

    private val leftMargin = 60f   // Espacio para las etiquetas de días (L, M, V...)
    private val topMargin = 50f    // Espacio para los meses (Ene, Feb...)
    private val bottomMargin = 70f // Espacio para la leyenda

    private val columns = 18 // Mostraremos unas 18 semanas atrás (aprox. 4 meses visibles sin scroll)

    // Colores de los niveles
    private var colorEmpty = Color.parseColor("#EBEDF0")
    private var colorLevel1 = Color.parseColor("#CBF0FF")
    private var colorLevel2 = Color.parseColor("#89CEFF")
    private var colorLevel3 = Color.parseColor("#00B0FF")
    private var colorLevel4 = Color.parseColor("#006494")

    init {
        colorEmpty = androidx.core.content.ContextCompat.getColor(context, com.boosthabits.R.color.heatmap_empty)
        colorLevel1 = androidx.core.content.ContextCompat.getColor(context, com.boosthabits.R.color.heatmap_level_1)
        colorLevel2 = androidx.core.content.ContextCompat.getColor(context, com.boosthabits.R.color.heatmap_level_2)
        colorLevel3 = androidx.core.content.ContextCompat.getColor(context, com.boosthabits.R.color.heatmap_level_3)
        colorLevel4 = androidx.core.content.ContextCompat.getColor(context, com.boosthabits.R.color.heatmap_level_4)
    }

    fun setData(newData: Map<LocalDate, Int>) {
        this.data = newData
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // calcular ancho y alto exactos basados en las celdas para evitar scrolls innecesarios
        val desiredWidth = leftMargin + (columns * (cellSize + gap))
        val desiredHeight = topMargin + (7 * (cellSize + gap)) + bottomMargin
        setMeasuredDimension(desiredWidth.toInt(), desiredHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val today = LocalDate.now()
        val startDate = today.minusWeeks((columns - 1).toLong()).with(DayOfWeek.MONDAY)

        val locale = Locale.getDefault()
        val daysToDraw = listOf(
            Pair(0, DayOfWeek.MONDAY.getDisplayName(TextStyle.NARROW, locale).uppercase()),
            Pair(2, DayOfWeek.WEDNESDAY.getDisplayName(TextStyle.NARROW, locale).uppercase()),
            Pair(4, DayOfWeek.FRIDAY.getDisplayName(TextStyle.NARROW, locale).uppercase())
        )
        for ((idx, label) in daysToDraw) {
            val y = topMargin + (idx * (cellSize + gap)) + (cellSize / 2) + 10f
            canvas.drawText(label, 10f, y, textPaint)
        }

        //Dibujar Cuadrícula y Etiquetas de Meses
        var lastMonthName = ""
        for (c in 0 until columns) {
            val columnFirstDate = startDate.plusWeeks(c.toLong())

            val currentMonthName = columnFirstDate.month.getDisplayName(TextStyle.SHORT, locale)
            if (currentMonthName != lastMonthName && c % 2 == 0) {
                val xText = leftMargin + (c * (cellSize + gap))
                canvas.drawText(currentMonthName, xText, 35f, textPaint)
                lastMonthName = currentMonthName
            }

            for (r in 0 until 7) {
                val currentDate = columnFirstDate.plusDays(r.toLong())

                // importante, no dibujar días futuros a hoy
                if (currentDate.isAfter(today)) continue

                val count = data[currentDate] ?: 0
                paint.color = getColorForCount(count)

                val left = leftMargin + (c * (cellSize + gap))
                val top = topMargin + (r * (cellSize + gap))

                canvas.drawRoundRect(
                    left, top, left + cellSize, top + cellSize,
                    cornerRadius, cornerRadius, paint
                )
            } }

        // la Leyenda del Fondo
        val legendY = height - 30f
        val lessText = context.getString(com.boosthabits.R.string.stats_less)
        val moreText = context.getString(com.boosthabits.R.string.stats_more)
        
        canvas.drawText(lessText, leftMargin, legendY, textPaint)

        val colors = listOf(colorEmpty, colorLevel1, colorLevel2, colorLevel3, colorLevel4)
        var startX = leftMargin + 100f

        for (color in colors) {
            paint.color = color
            canvas.drawRoundRect(
                startX, height - 55f, startX + cellSize, height - 55f + cellSize,
                cornerRadius, cornerRadius, paint
            )
            startX += cellSize + gap
        }
        canvas.drawText(moreText, startX + 10f, legendY, textPaint)
    }

    private fun getColorForCount(count: Int): Int = when {
        count == 0 -> colorEmpty
        count == 1 -> colorLevel1
        count <= 3 -> colorLevel2
        count <= 5 -> colorLevel3
        else -> colorLevel4
    }
}