package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Missing days are gaps, not fabricated zero scores or interpolated academic improvement. */
@Composable
fun TrendChart(values: List<Pair<LocalDate, Float?>>, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(110.dp)) {
            val usable = size.height - 16.dp.toPx()
            for (level in 0..2) drawLine(EvolveLine, Offset(0f, usable * level / 2f), Offset(size.width, usable * level / 2f))
            val points = values.mapIndexed { index, (_, value) -> value?.let {
                Offset(size.width * index / maxOf(1, values.lastIndex), usable * (1f - it.coerceIn(0f, 1f)) + 4.dp.toPx())
            } }
            points.forEachIndexed { index, point ->
                if (point != null) {
                    val previous = points.getOrNull(index - 1)
                    if (previous != null) {
                        val path = Path().apply { moveTo(previous.x, previous.y)
                            val middle = (previous.x + point.x) / 2
                            cubicTo(middle, previous.y, middle, point.y, point.x, point.y) }
                        drawPath(path, EvolveAccent, style = Stroke(1.5.dp.toPx()))
                    }
                    drawCircle(EvolveAccent, 3.dp.toPx(), point)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            values.forEach { (date, _) -> Text(date.format(DateTimeFormatter.ofPattern("EEEEE", Locale.ENGLISH)), color = EvolveDim, fontSize = 10.sp) }
        }
    }
}
