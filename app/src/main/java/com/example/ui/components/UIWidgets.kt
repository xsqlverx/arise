package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveDim
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolvePillBg
import com.example.ui.theme.EvolvePillText
import com.example.ui.theme.EvolveSurface
import com.example.ui.theme.EvolveText
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun Eyebrow(
    text: String,
    accent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
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
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) Color(0x1AFFFFFF) else EvolvePillBg,
            contentColor = if (secondary) EvolveText else EvolvePillText,
            disabledContainerColor = Color(0x33E8E2F3),
            disabledContentColor = Color(0x5517131D)
        )
    ) {
        Text(
            text = label,
            fontFamily = ManropeFontFamily,
            fontSize = 13.sp
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
                fontFamily = ManropeFontFamily,
                fontSize = 11.sp,
                color = EvolveMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (multiline) 140.dp else 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0AFFFFFF))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                .padding(14.dp),
            contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.fillMaxSize(),
                singleLine = !multiline,
                cursorBrush = SolidColor(EvolveAccent),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    lineHeight = if (multiline) 20.sp else 16.sp,
                    color = EvolveText
                ),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF8C8595)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun EvolveSheet(
    title: String,
    onClose: () -> Unit,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onClose)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(EvolveSurface)
                .border(1.dp, EvolveLine, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clickable(enabled = false) {}
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 34.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF48434F))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontFamily = SpaceGroteskFontFamily, fontSize = 24.sp, color = EvolveText)
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp).border(1.dp, EvolveLine, CircleShape)
                ) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = EvolveMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                content()
            }

            if (footer != null) {
                Spacer(modifier = Modifier.height(14.dp))
                footer()
            }
        }
    }
}
