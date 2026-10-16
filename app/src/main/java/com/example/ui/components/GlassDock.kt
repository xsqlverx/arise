package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.os.Build
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolveText
import com.example.ui.theme.ManropeFontFamily

enum class NavDestination(val label: String, val icon: ImageVector) {
    TODAY("Today", Icons.Outlined.Home),
    PLAN("Plan", Icons.Outlined.CalendarMonth),
    SUBJECTS("Class notes", Icons.AutoMirrored.Outlined.MenuBook),
    PROGRESS("Progress", Icons.Outlined.TrendingUp)
}

@Composable
fun GlassDock(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    isVisible: Boolean = true,
    backdrop: GraphicsLayer? = null,
    backdropOrigin: Offset = Offset.Zero,
    modifier: Modifier = Modifier
) {
    val reduced = LocalReduceMotion.current
    var origin by remember { mutableStateOf(Offset.Zero) }

    AnimatedVisibility(
        visible = isVisible,
        enter = if (reduced) EnterTransition.None else slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = if (reduced) ExitTransition.None else slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp)
        ) {
            // Main Smoked-Glass Capsule
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(67.dp)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(38.dp),
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(38.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0x38FFFFFF),
                                Color(0x0AFFFFFF),
                                Color(0x1FFFFFFF)
                            )
                        )
                    )
                    .background(Color(0xBA22212A))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x4DFFFFFF),
                                Color(0x14FFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(38.dp)
                    )
                    .padding(6.dp)
            ) {
                // Only the background sample is blurred; labels and the moving lens remain sharp.
                if (backdrop != null) Box(Modifier.matchParentSize()
                    .onGloballyPositioned { origin = it.positionInRoot() - backdropOrigin }
                    .graphicsLayer {
                        if (Build.VERSION.SDK_INT >= 31) renderEffect = BlurEffect(18f, 18f, TileMode.Clamp)
                    }
                    .drawWithContent {
                        translate(-origin.x, -origin.y) { drawLayer(backdrop) }
                        drawRect(Color(0xB51C1923))
                    })
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val tabWidth = maxWidth / 4
                    val targetIndex = currentDestination.ordinal.toFloat()

                    // Lens animated offset with spring
                    val animatedIndex by animateFloatAsState(
                        targetValue = targetIndex,
                        animationSpec = if (reduced) snap() else spring(dampingRatio = .8f, stiffness = 350f),
                        label = "dock_lens"
                    )

                    // Floating Glass Lens
                    Box(
                        modifier = Modifier
                            .offset(x = tabWidth * animatedIndex)
                            .width(tabWidth)
                            .height(53.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0x40E6D7FF),
                                        Color(0x15B3A0DD)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x66FFFFFF),
                                        Color(0x14FFFFFF)
                                    )
                                ),
                                shape = RoundedCornerShape(30.dp)
                            )
                    )

                    // 4 Navigation Items
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavDestination.values().forEach { destination ->
                            val isSelected = currentDestination == destination

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(53.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .selectable(
                                        selected = isSelected,
                                        role = Role.Tab,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, color = EvolveAccent),
                                        onClick = { onNavigate(destination) }
                                    )
                                    .testTag("nav_tab_${destination.name.lowercase()}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFFF7F2FF) else EvolveMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = destination.label,
                                    fontFamily = ManropeFontFamily,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color(0xFFF7F2FF) else EvolveMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
