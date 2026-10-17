package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/** Details keep their space and reveal on request. No continuously running animation. */
@Composable
fun EvolveDisclosure(title: String, expanded: Boolean, onToggle: () -> Unit,
    subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    val reduced = LocalReduceMotion.current
    val angle by animateFloatAsState(if (expanded) 180f else 0f,
        if (reduced) snap() else tween(180), label = "disclosure arrow")
    Column(Modifier.fillMaxWidth().animateContentSize(if (reduced) snap() else tween(220))) {
        Row(Modifier.fillMaxWidth().semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
            .clickable(role = Role.Button, onClick = onToggle).heightIn(min = 56.dp).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = EvolveText, fontFamily = SpaceGroteskFontFamily, fontSize = 18.sp, lineHeight = 24.sp)
                subtitle?.let { Spacer(Modifier.height(5.dp)); Text(it, color = EvolveMuted, fontSize = 12.sp) }
            }
            Icon(Icons.Outlined.KeyboardArrowDown, null, Modifier.padding(start = 12.dp).rotate(angle), tint = EvolveMuted)
        }
        if (expanded) content()
    }
}

@Composable
fun SavedMark() {
    val reduced = LocalReduceMotion.current
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val scale by animateFloatAsState(if (shown) 1f else .82f,
        if (reduced) snap() else spring(dampingRatio = .7f, stiffness = 500f), label = "saved confirmation")
    Box(Modifier.size(48.dp).graphicsLayer { scaleX = scale; scaleY = scale }
        .background(EvolveAccent.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.Check, "Saved", tint = EvolveAccent)
    }
}
