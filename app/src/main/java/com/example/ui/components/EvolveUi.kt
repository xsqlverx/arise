package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

val LocalReduceMotion = staticCompositionLocalOf { false }
val LocalSheetFeedback = staticCompositionLocalOf<String?> { null }

@Composable
fun Eyebrow(text: String, accent: Boolean = false) {
    Text(text.uppercase(), fontFamily = ManropeFontFamily, fontSize = 10.sp,
        letterSpacing = 1.5.sp, color = if (accent) EvolveAccent else EvolveDim)
}

@Composable
fun EvolveAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val reduced = LocalReduceMotion.current
    val scale by animateFloatAsState(if (pressed && !reduced) .975f else 1f, spring(stiffness = 650f), label = "press")
    Box(modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(RoundedCornerShape(16.dp)).background(if (enabled) EvolvePillBg else EvolveRaised)
        .clickable(enabled = enabled, interactionSource = interactions, indication = null, onClick = onClick)
        .defaultMinSize(minHeight = 52.dp).padding(horizontal = 18.dp, vertical = 16.dp)) {
        Text(text, fontFamily = ManropeFontFamily, fontSize = 14.sp, color = if (enabled) EvolvePillText else EvolveDim)
    }
}

@Composable
fun EvolveField(label: String, value: String, onChange: (String) -> Unit, multiline: Boolean = false,
    hint: String = "", enabled: Boolean = true) {
    Text(label, color = EvolveMuted, fontFamily = ManropeFontFamily, fontSize = 12.sp)
    Spacer(Modifier.height(7.dp))
    BasicTextField(value, onChange, enabled = enabled, singleLine = !multiline,
        cursorBrush = SolidColor(EvolveAccent), textStyle = TextStyle(color = EvolveText,
            fontFamily = ManropeFontFamily, fontSize = 14.sp, lineHeight = 23.sp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(EvolveRaised)
            .border(1.dp, EvolveLine, RoundedCornerShape(14.dp)).padding(15.dp),
        decorationBox = { field -> Box { if (value.isEmpty()) Text(hint, color = EvolveDim, fontSize = 14.sp); field() } })
    Spacer(Modifier.height(16.dp))
}
