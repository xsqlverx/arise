package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveViolet
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

data class ContourRidge(
    val points: List<Offset>,
    val opacity: Float,
    val isHighlight: Boolean
)

@Composable
fun ContourLandscape(
    modifier: Modifier = Modifier,
    height: Dp = 196.dp
) {
    // Deterministic procedural contour computation cached in memory
    val ridges = remember {
        val list = mutableListOf<ContourRidge>()
        val baseWidth = 430f
        val baseHeight = 260f

        for (j in 0 until 48) {
            val v = -1f + (j.toFloat() / 47f) * 2f
            val points = mutableListOf<Offset>()

            for (i in 0..92) {
                val u = -1.25f + (i.toFloat() / 92f) * 2.5f

                val term1 = 0.93f * exp(-((u + 0.24f) * (u + 0.24f) / 0.22f + (v + 0.12f) * (v + 0.12f) / 0.48f))
                val term2 = 0.64f * exp(-((u - 0.55f) * (u - 0.55f) / 0.13f + (v - 0.29f) * (v - 0.29f) / 0.29f))
                var h = term1 + term2
                h += 0.025f * sin(u * 24f + v * 7f) * exp(-u * u - v * v)
                h += 0.018f * cos(u * 39f - v * 13f) * exp(-u * u - v * v)

                val px = 215f + (u * 0.76f + v * 0.4f) * 172f
                val py = 187f + (v * 0.34f - u * 0.12f) * 102f - h * 139f

                // Normalize to [0..1] based on 430x260
                points.add(Offset(px / baseWidth, py / baseHeight))
            }

            val opacity = 0.33f + (j.toFloat() / 47f) * 0.5f
            list.add(ContourRidge(points, opacity, isHighlight = (j == 20)))
        }
        list
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Center glow ellipse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7950B1).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.54f, h * 0.52f),
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = Offset(w * 0.54f, h * 0.52f)
            )

            val strokeBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFD9C6FF),
                    Color(0xFFA389D4),
                    Color(0xFF3D2C5C)
                ),
                start = Offset.Zero,
                end = Offset(w * 0.85f, h)
            )

            // Draw ridges
            ridges.forEach { ridge ->
                val path = Path()
                ridge.points.forEachIndexed { index, pt ->
                    val x = pt.x * w
                    val y = pt.y * h
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                if (ridge.isHighlight) {
                    // Glowing highlight ridge
                    drawPath(
                        path = path,
                        color = Color(0xFFBB95FF).copy(alpha = 0.45f),
                        style = Stroke(width = 3.5f)
                    )
                    drawPath(
                        path = path,
                        color = EvolveAccent,
                        style = Stroke(width = 1.6f)
                    )
                } else {
                    drawPath(
                        path = path,
                        brush = strokeBrush,
                        alpha = ridge.opacity,
                        style = Stroke(width = 0.85f)
                    )
                }
            }

            // Top and bottom vignetting to blend into canvas
            drawRect(
                brush = Brush.verticalGradient(
                    0.0f to EvolveBlack,
                    0.18f to Color.Transparent,
                    0.72f to Color.Transparent,
                    1.0f to EvolveBlack
                )
            )
        }
    }
}
