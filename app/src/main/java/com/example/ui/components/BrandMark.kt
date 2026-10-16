package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EvolveAccent

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val dotRadius = w * 0.08f
        val cols = 5
        val rows = 6

        val stepX = (w - dotRadius * 2) / (cols - 1)
        val stepY = (h - dotRadius * 2) / (rows - 1)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (c == 0 || r == 0 || r == 2 || r == 5) {
                    val cx = dotRadius + c * stepX
                    val cy = dotRadius + r * stepY
                    drawCircle(
                        color = EvolveAccent,
                        radius = dotRadius,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}
