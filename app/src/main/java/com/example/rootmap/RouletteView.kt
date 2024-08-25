package com.example.rootmap

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RouletteView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val rectF = RectF()
    private val strokePaint = Paint()
    private val fillPaint = Paint()
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 40f
        isAntiAlias = true
    }

    private var names: List<String> = emptyList()

    init {
        strokePaint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 15f
            isAntiAlias = true
        }

        fillPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    fun setNames(names: List<String>) {
        this.names = names
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (names.isEmpty()) return

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) - 20
        val rectLeft = centerX - radius
        val rectRight = centerX + radius
        val rectTop = centerY - radius
        val rectBottom = centerY + radius

        rectF.set(rectLeft, rectTop, rectRight, rectBottom)

        drawRoulette(canvas, rectF, centerX, centerY, radius)
    }

    private fun drawRoulette(canvas: Canvas, rectF: RectF, centerX: Float, centerY: Float, radius: Float) {
        canvas.drawArc(rectF, 0f, 360f, true, strokePaint)

        if (names.size in 2..8) {
            val sweepAngle = 360f / names.size.toFloat()
            val colors = listOf("#B39EB5", "#AEC6CF", "#FFB7C5", "#B19CD9", "#ADD8E6", "#FFD1DC", "#C9C0BB", "#AFEEEE")

            for (i in names.indices) {
                fillPaint.color = Color.parseColor(colors[i % colors.size])
                val startAngle = sweepAngle * i
                canvas.drawArc(rectF, startAngle, sweepAngle, true, fillPaint)

                drawTextOnArc(canvas, rectF, names[i], startAngle, sweepAngle, centerX, centerY, radius)
            }
        } else {
            throw RuntimeException("size out of roulette")
        }
    }

    private fun drawTextOnArc(canvas: Canvas, rectF: RectF, text: String, startAngle: Float, sweepAngle: Float, centerX: Float, centerY: Float, radius: Float) {
        val angle = Math.toRadians((startAngle + sweepAngle / 2 - 90).toDouble())
        val textRadius = radius * 0.7
        val textX = (centerX + cos(angle) * textRadius).toFloat()
        val textY = (centerY + sin(angle) * textRadius).toFloat() + textPaint.textSize / 3

        canvas.drawText(text, textX, textY, textPaint)
    }
}