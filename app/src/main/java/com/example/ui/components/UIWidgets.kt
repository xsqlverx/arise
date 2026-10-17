package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

val LocalReduceMotion = compositionLocalOf { false }
val LocalSheetFeedback = compositionLocalOf<String?> { null }

@Composable
fun Eyebrow(
    text: String,
    accent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(Locale.ROOT),
        fontFamily = ManropeFontFamily,
        fontSize = 10.sp,
        letterSpacing = 1.6.sp,
        color = if (accent) EvolveAccent else EvolveMuted,
        modifier = modifier
    )
}

@Composable
fun EvolveAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondary: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) Color(0x1BFFFFFF) else EvolvePillBg,
            contentColor = if (secondary) EvolveText else EvolvePillText,
            disabledContainerColor = Color(0x33E8E2F3),
            disabledContentColor = Color(0x55E8E2F3)
        )
    ) {
        Text(
            text = label,
            fontFamily = ManropeFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EvolveField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    multiline: Boolean = false,
    hint: String = "",
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = EvolveMuted,
                fontFamily = ManropeFontFamily,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(6.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (multiline) 140.dp else 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0B000000))
                .border(1.dp, Color(0x23FFFFFF), RoundedCornerShape(12.dp))
                .padding(14.dp),
            textStyle = TextStyle(
                color = EvolveText,
                fontFamily = ManropeFontFamily,
                fontSize = 14.sp
            ),
            singleLine = !multiline,
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart
                ) {
                    if (value.isEmpty() && hint.isNotEmpty()) {
                        Text(
                            text = hint,
                            color = Color(0xFF55505D),
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun EvolveSheet(
    title: String,
    onClose: () -> Unit,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(EvolveSurface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = EvolveText,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp),
                        tint = EvolveMuted
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            content()
            if (footer != null) {
                Spacer(Modifier.height(20.dp))
                footer()
            }
        }
    }
}
