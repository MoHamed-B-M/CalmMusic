package com.music.calmplayer.ui.components

import android.graphics.Matrix
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun getSquircleShape() = MorphingShape(0f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun getMorphingCircleShape() = MorphingShape(1f)

@Composable
fun ExpressiveWaveformSeekbar(
    progress: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    var draggingProgress by remember { mutableFloatStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }

    val pulse by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        draggingProgress = newProgress
                        try {
                            awaitRelease()
                        } finally {
                            isDragging = false
                            onValueChange(draggingProgress)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        onValueChange(draggingProgress)
                    },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { change, _ ->
                        val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        draggingProgress = newProgress
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val barWidth = 3.dp.toPx()
            val gap = 3.dp.toPx()
            val count = (width / (barWidth + gap)).toInt()
            
            val currentProgress = if (isDragging) draggingProgress else progress

            for (i in 0 until count) {
                val x = i * (barWidth + gap)
                val normalizedX = i.toFloat() / count
                // Sine wave based height
                val sinValue = kotlin.math.sin(i * 0.3f + currentProgress * 20f)
                val absSin = if (sinValue < 0f) -sinValue else sinValue
                val barHeight = (height * (0.3f + 0.5f * absSin)).coerceIn(4.dp.toPx(), height)
                
                val colorToUse = if (normalizedX <= currentProgress) color else inactiveColor
                
                drawRoundRect(
                    color = colorToUse,
                    topLeft = androidx.compose.ui.geometry.Offset(x, (height - barHeight) / 2f),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingShape(
    morphProgress: Float
): Shape {
    val squircle = remember {
        RoundedPolygon(
            numVertices = 4,
            rounding = CornerRounding(0.4f)
        )
    }
    val circle = remember {
        RoundedPolygon(
            numVertices = 12,
            rounding = CornerRounding(radius = 1f)
        )
    }
    val morph = remember(squircle, circle) {
        Morph(squircle, circle)
    }
    
    return remember(morph, morphProgress) {
        object : Shape {
            private val androidPath = android.graphics.Path()
            private val matrix = Matrix()
            
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                androidPath.reset()
                morph.toPath(morphProgress, androidPath)
                
                matrix.reset()
                matrix.postScale(size.width / 2f, size.height / 2f)
                matrix.postTranslate(size.width / 2f, size.height / 2f)
                androidPath.transform(matrix)
                
                return Outline.Generic(androidPath.asComposePath())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SquircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit
) {
    val morphProgress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, 
            stiffness = Spring.StiffnessLow
        ),
        label = "shapeMorph"
    )
    
    val shape = MorphingShape(morphProgress)
    
    Box(
        modifier = modifier
            .graphicsLayer {
                this.shape = shape
                clip = true
            }
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ExpressiveControlLayout(
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSkipPrevious,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Filled.SkipPrevious, null, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            SquircleButton(
                onClick = onPlayPause,
                modifier = Modifier.size(80.dp),
                isPlaying = isPlaying,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                val iconScale by animateFloatAsState(
                    targetValue = if (isPlaying) 1.1f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "iconScale"
                )
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp * iconScale),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSkipNext,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Filled.SkipNext, null, modifier = Modifier.size(28.dp))
            }
        }
    }
}
