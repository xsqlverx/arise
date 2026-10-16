package com.example.ui.sheets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.LocalReduceMotion
import com.example.ui.components.LocalSheetFeedback
import com.example.ui.theme.*

/** A separate modal window traps focus and keeps its footer above actual IME insets. */
@Composable
fun EvolveSheet(title: String, onClose: () -> Unit, footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit) {
    val reduced = LocalReduceMotion.current
    val entrance = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, tween(if (reduced) 0 else 280)) }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .64f * entrance.value))
            .windowInsetsPadding(WindowInsets.safeDrawing).imePadding()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
            contentAlignment = Alignment.BottomCenter) {
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().fillMaxHeight(.94f)
                .graphicsLayer { translationY = (1f - entrance.value) * 120f; alpha = entrance.value }
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)).background(EvolveSurface)
                .border(1.dp, EvolveLine, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(horizontal = 24.dp, vertical = 16.dp)) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(36.dp, 3.dp).background(EvolveMuted, RoundedCornerShape(4.dp)))
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, Modifier.weight(1f), fontFamily = SpaceGroteskFontFamily, fontSize = 25.sp, color = EvolveText)
                    IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Close", tint = EvolveMuted) }
                }
                Spacer(Modifier.height(12.dp))
                LocalSheetFeedback.current?.let {
                    Text(it, color = EvolveWarm, fontFamily = ManropeFontFamily, fontSize = 12.sp, lineHeight = 19.sp)
                    Spacer(Modifier.height(12.dp))
                }
                Column(Modifier.weight(1f), content = content)
                footer?.let { Spacer(Modifier.height(12.dp)); it() }
            }
        }
    }
}
