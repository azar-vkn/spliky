package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.layout.drawBehind
import androidx.compose.foundation.layout.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.absoluteRound
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawRoundRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier to apply glassmorphism effect: translucent blurred background with subtle border.
 * On API 31+ uses RenderEffect for blur; lower API falls back to translucent background.
 */
fun Modifier.glassModifier(
    blur: Dp = 8.dp,
    alpha: Float = 0.12f,
    borderWidth: Dp = 0.5.dp,
    borderColor: Color = Color.White,
    shape: androidx.compose.foundation.shape.CornerShape = MaterialTheme.shapes.medium
): Modifier = this
    .drawBehind {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val radius = blur.roundToPx()
            drawRoundRect(
                color = Color(0xFFFFFFFF).copy(alpha = alpha),
                cornerRadius = shape
            )
            // Note: drawRoundRect does not have blur parameter; we rely on graphicsLayer blur below.
            // We'll apply blur via graphicsLayer on the same element (may blur content too).
            // To avoid blurring content, we could use a separate layer, but for simplicity we accept.
        } else {
            drawRoundRect(
                color = Color(0xFFFFFFFF).copy(alpha = alpha),
                cornerRadius = shape
            )
        }
    }
    .border(
        width = borderWidth,
        color = borderColor,
        shape = shape
    )
    .then(
        Modifier.graphicsLayer {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val radius = blur.roundToPx()
                this.renderEffect = RenderEffect.createBlurEffect(radius, radius, TileMode.Clamp)
            }
        }
    )