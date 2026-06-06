package com.boosthabits.ui.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.time.LocalDate
import java.time.Year
import java.time.format.TextStyle
import java.util.Locale

class ActivityCalendarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: Map<LocalDate, Int> = emptyMap()
    private val anioActual = Year.now().value

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#424242")
        textSize = 24f
        strokeWidth = 2f
    }

    // mini celdas para que quepa todo el año en pantalla sin scroll masivo
    private val miniCellSize = 14f
    private val miniGap = 3f

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

    // Dimensiones de cada bloque de mes (7 días de ancho x 6 semanas de alto máximo)
    private val monthWidth = (7 * (miniCellSize + miniGap))
    private val monthHeight = (6 * (miniCellSize + miniGap)) + 40f // 40f para el nombre del mes

    private val gridSpacingX = 40f
    private val gridSpacingY = 40f

    fun SetDatosAnuales(newData: Map<LocalDate, Int>) {
        this.data = newData
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Cuadrícula de 3 meses por fila, 4 filas en total (= 12 meses)
        val totalWidth = (monthWidth * 3) + (gridSpacingX * 2) + paddingLeft + paddingRight
        val totalHeight = (monthHeight * 4) + (gridSpacingY * 3) + paddingTop + paddingBottom
        setMeasuredDimension(totalWidth.toInt(), totalHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (month in 1..12) {
            // Calcular posición en la cuadrícula de 3x4
            val row = (month - 1) / 3
            val col = (month - 1) % 3

            val startX = paddingLeft + col * (monthWidth + gridSpacingX)
            val startY = paddingTop + row * (monthHeight + gridSpacingY)

            // dibujar el título del mes y los deías del mees en formato calendario
            val firstDayOfMonth = LocalDate.of(anioActual, month, 1)
            val monthLabel = firstDayOfMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { it.uppercase() }
            canvas.drawText(monthLabel, startX, startY + 25f, textPaint)


            val daysInMonth = firstDayOfMonth.lengthOfMonth()
            // saber en qué día de la semana empieza (1 = Lunes, 7 = Domingo)
            val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1

            for (day in 1..daysInMonth) {
                val currentDate = LocalDate.of(anioActual, month, day)
                val count = data[currentDate] ?: 0

                paint.color = getColorForCount(count)

                // Posición de la celda dentro de su mes
                val cellIndex = dayOfWeekOffset + (day - 1)
                val cellCol = cellIndex % 7
                val cellRow = cellIndex / 7

                val x = startX + cellCol * (miniCellSize + miniGap)
                val y = startY + 40f + cellRow * (miniCellSize + miniGap)

                canvas.drawRoundRect(x, y, x + miniCellSize, y + miniCellSize, 3f, 3f, paint)
            }
        }
    }

    private fun getColorForCount(count: Int): Int = when {
        count == 0 -> colorEmpty
        count == 1 -> colorLevel1
        count <= 3 -> colorLevel2
        count <= 5 -> colorLevel3
        else -> colorLevel4
    }
}