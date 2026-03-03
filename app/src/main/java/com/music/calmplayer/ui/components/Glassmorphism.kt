package com.music.calmplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.glass(
    shape: Shape = RoundedCornerShape(28.dp), // Extra Large rounded corners
    alpha: Float = 0.85f, // Higher alpha for legibility without blur
    showBorder: Boolean = true
): Modifier = composed {
    val color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha)
    
    this
        .clip(shape)
        .background(color = color)
        .then(
            if (showBorder) {
                Modifier.border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = shape
                )
            } else {
                Modifier
            }
        )
}
