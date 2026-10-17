package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveLine

@Composable
fun TrendChart(
    data: List<Pair<java.time.LocalDate, Float?>> = emptyList(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(138.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            val chartBottom = h - 28f
            val gridY1 = chartBottom * 0.28f
            val gridY2 = chartBottom * 0.58f
            val gridY3 = chartBottom * 0.88f

            drawLine(color = EvolveLine, start = Offset(0f, gridY1), end = Offset(w, gridY1), strokeWidth = 1f)
            drawLine(color = EvolveLine, start = Offset(0f, gridY2), end = Offset(w, gridY2), strokeWidth = 1f)
            drawLine(color = EvolveLine, start = Offset(0f, gridY3), end = Offset(w, gridY3), strokeWidth = 1f)

            val points = if (data.size >= 2) {
                data.mapIndexed { idx, pair ->
                    val x = (w * (idx.toFloat() / (data.size - 1))).coerceIn(w * 0.02f, w * 0.98f)
                    val acc = pair.second ?: 0.5f
                    val y = chartBottom * (1f - (acc.coerceIn(0.1f, 0.95f) * 0.78f + 0.1f))
                    Offset(x, y)
                }
            } else {
                listOf(
                    Offset(w * 0.01f, chartBottom * 0.88f),
                    Offset(w * 0.16f, chartBottom * 0.78f),
                    Offset(w * 0.32f, chartBottom * 0.62f),
                    Offset(w * 0.48f, chartBottom * 0.56f),
                    Offset(w * 0.64f, chartBottom * 0.42f),
                    Offset(w * 0.80f, chartBottom * 0.30f),
                    Offset(w * 0.98f, chartBottom * 0.12f)
                )
            }

            val curvePath = Path()
            val fillPath = Path()

            curvePath.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, points[0].y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val cx = (p0.x + p1.x) / 2f
                curvePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, chartBottom)
            fillPath.lineTo(points.first().x, chartBottom)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(EvolveAccent.copy(alpha = 0.22f), Color.Transparent),
                    startY = 0f,
                    endY = chartBottom
                )
            )

            drawPath(path = curvePath, color = EvolveAccent, style = Stroke(width = 2.4f))

            val thuPoint = points[5]
            drawLine(
                color = EvolveAccent.copy(alpha = 0.35f),
                start = Offset(thuPoint.x, thuPoint.y + 6f),
                end = Offset(thuPoint.x, chartBottom),
                strokeWidth = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            drawCircle(color = EvolveBlack, radius = 6f, center = thuPoint)
            drawCircle(color = Color(0xFFD8CAFA), radius = 4.2f, center = thuPoint)

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#DFD3F3")
                textSize = 28f
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            drawContext.canvas.nativeCanvas.drawText("68%", thuPoint.x - 22f, thuPoint.y - 14f, paint)

            val dayLabels = if (data.isNotEmpty()) {
                data.map { it.first.dayOfWeek.name.take(3) }
            } else {
                listOf("SAT", "SUN", "MON", "TUE", "WED", "THU", "FRI")
            }
            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#89838F")
                textSize = 24f
                isAntiAlias = true
            }

            dayLabels.forEachIndexed { idx, label ->
                val xPos = (w * (idx.toFloat() / 6f)).coerceIn(8f, w - 42f)
                drawContext.canvas.nativeCanvas.drawText(label, xPos, h - 4f, labelPaint)
            }
        }
    }
}
