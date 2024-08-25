package com.example.rootmap

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.*
class LadderView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val verticalLinePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        isAntiAlias = true
    }
    private val horizontalLinePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 8f
        isAntiAlias = true
    }
    private val playerPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 12f
        isAntiAlias = true
    }
    private val redLinePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 8f
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val horizontalLines = mutableListOf<Triple<Int, Int, Int>>()
    private val passedLines = mutableListOf<Triple<Int, Int, Int>>() // 플레이어가 지나간 가로선들을 저장
    private var numOfVerticalLines: Int = 0
    private var names: List<String> = emptyList()
    private var amounts: List<Int> = emptyList()
    private var playerPositions = mutableMapOf<Int, Int>()
    private var animationY: Float = 0f
    private var isAnimating: Boolean = false
    private var playerPosition: Int = -1
    private var onFinish: (() -> Unit)? = null

    fun setLadderData(numberOfPeople: Int, ladderLines: List<Triple<Int, Int, Int>>, names: List<String>, amounts: List<Int>) {
        numOfVerticalLines = numberOfPeople
        horizontalLines.clear()
        passedLines.clear() // 지나간 선 목록 초기화
        this.names = names
        this.amounts = amounts
        playerPositions.clear()
        for (i in 1..numberOfPeople) {
            playerPositions[i] = i
        }
        horizontalLines.addAll(ladderLines)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val verticalSpacing = width / (numOfVerticalLines + 1).toFloat()

        // 사다리 수직선 그리기
        for (i in 1..numOfVerticalLines) {
            val x = i * verticalSpacing
            canvas.drawLine(x, 100f, x, height - 100f, verticalLinePaint)
        }

        // 플레이어가 지나간 가로선 그리기 (빨간색)
        for ((start, end, yPosition) in passedLines) {
            val startX = start * verticalSpacing
            val endX = end * verticalSpacing
            val y = yPosition.toFloat()
            canvas.drawLine(startX, y, endX, y, redLinePaint)
        }

        // 사다리 가로선 그리기 (파란색)
        for ((start, end, yPosition) in horizontalLines) {
            val startX = start * verticalSpacing
            val endX = end * verticalSpacing
            val y = yPosition.toFloat()
            canvas.drawLine(startX, y, endX, y, horizontalLinePaint)
        }

        // 사다리 맨 위에 이름 그리기
        for (i in 1..numOfVerticalLines) {
            val x = i * verticalSpacing
            canvas.drawText(names[i - 1], x, 60f, textPaint)
        }

        // 사다리 맨 밑에 금액 그리기 (고정된 금액)
        for (i in 1..numOfVerticalLines) {
            val x = i * verticalSpacing
            canvas.drawText(amounts[i - 1].toString(), x, height - 20f, textPaint)
        }

        // 플레이어 애니메이션 그리기
        if (playerPosition != -1) {
            val x = playerPosition * verticalSpacing
            canvas.drawCircle(x, animationY, 20f, playerPaint)
            if (isAnimating) {
                updateAnimationPosition(verticalSpacing)
            }
        }
    }

    private fun updateAnimationPosition(verticalSpacing: Float) {
        val nextY = animationY + 5f // 10f에서 5f로 수정하여 속도 감소

        // 현재 위치에서 가로선이 있는지 확인
        for ((start, end, yPosition) in horizontalLines) {
            val y = yPosition.toFloat()
            if (animationY < y && nextY >= y) {
                // 가로선과 교차할 경우, 플레이어를 좌우로 이동
                if (playerPosition == start) {
                    playerPosition += 1
                    passedLines.add(Triple(start, end, yPosition)) // 지나간 선을 빨간선으로 추가
                } else if (playerPosition == end) {
                    playerPosition -= 1
                    passedLines.add(Triple(end, start, yPosition)) // 지나간 선을 빨간선으로 추가
                }
                animationY = y // 정확히 가로선의 위치로 이동
                invalidate()
                return
            }
        }

        // 가로선과 교차하지 않으면 계속 수직으로 이동
        animationY = nextY
        if (animationY >= height - 100f) {
            isAnimating = false
            onFinish?.invoke()
        } else {
            invalidate()
        }
    }

    fun startPlayerAnimation(initialPosition: Int, onFinish: () -> Unit) {
        this.playerPosition = initialPosition
        animationY = 0f
        isAnimating = true
        this.onFinish = onFinish
        invalidate()
    }

    fun startPlayerAnimationSequentially(onAllFinish: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            for (initialPosition in 1..numOfVerticalLines) {
                startPlayerAnimation(initialPosition) {
                    playerPositions[initialPosition] = calculateFinalPosition(initialPosition)
                }
                while (isAnimating) {
                    delay(70) // 50ms에서 70ms로 수정하여 속도 감소
                }
            }
            onAllFinish()
        }
    }

    fun calculateLadderResults(): List<Int> {
        return (1..numOfVerticalLines).map { playerPositions[it] ?: it }
    }

    private fun calculateFinalPosition(initialPosition: Int): Int {
        var currentPosition = initialPosition
        var currentY = 0f

        // Y 방향으로 사다리를 따라 내려감
        while (currentY < height - 100f) {
            currentY += 10f

            // 현재 위치에서 가로선이 있는지 확인
            for ((start, end, yPosition) in horizontalLines) {
                val y = yPosition.toFloat()
                if (currentY < y && currentY + 10f >= y) {
                    // 가로선과 교차할 경우, 플레이어를 좌우로 이동
                    if (currentPosition == start) {
                        currentPosition += 1
                    } else if (currentPosition == end) {
                        currentPosition -= 1
                    }
                    currentY = y // 정확히 가로선의 위치로 이동
                    break
                }
            }
        }

        return currentPosition
    }

    fun generateComplexLadderLines(numberOfPeople: Int): List<Triple<Int, Int, Int>> {
        val lines = mutableListOf<Triple<Int, Int, Int>>()
        val random = java.util.Random()

        // 각 수직선 사이에 여러 개의 가로선을 추가합니다
        for (i in 1 until numberOfPeople) {
            val numLines = random.nextInt(3) + 3 // 3~5개의 가로선을 각 세로선 사이에 추가
            val usedYPositions = mutableSetOf<Int>() // 이미 사용된 Y 위치를 저장
            var lastEnd = -1 // 마지막 가로선의 끝 위치를 추적
            val minSpacing = 50 // 가로선 사이의 최소 간격 (픽셀)

            for (j in 0 until numLines) {
                var yPosition: Int
                do {
                    yPosition = random.nextInt(height - 200) + 100 // Y 위치를 무작위로 결정
                } while (yPosition in usedYPositions || (usedYPositions.any { Math.abs(it - yPosition) < minSpacing }))
                // 동일한 Y 위치에 중복 가로선이 생기지 않도록 하고, 최소 간격 유지

                usedYPositions.add(yPosition)

                // 마지막 가로선과 겹치지 않도록 체크하고, 최소 간격을 확보
                if (i != lastEnd && lastEnd != i + 1) {
                    lines.add(Triple(i, i + 1, yPosition))
                    lastEnd = i + 1 // 현재 가로선의 끝 위치를 갱신
                } else {
                    // 바로 이어지지 않도록 한 칸을 건너뛰고, 최소 간격을 두고 가로선을 추가
                    if (lastEnd != -1 && lastEnd != i) {
                        lines.add(Triple(i, i + 1, yPosition))
                        lastEnd = i + 1
                    }
                }
            }
        }

        return lines
    }
}
