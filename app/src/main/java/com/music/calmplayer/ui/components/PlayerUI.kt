package com.music.calmplayer.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.music.calmplayer.data.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSheet(
    song: Song?,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onPositionChange: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onDismiss: () -> Unit,
    visible: Boolean,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        if (song != null) {
            FullPlayerContent(
                song = song,
                isPlaying = isPlaying,
                position = position,
                duration = duration,
                onPositionChange = onPositionChange,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onCollapse = onDismiss,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    }
}


@Composable
fun FullPlayerContent(
    song: Song,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onPositionChange: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onCollapse: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager }

    // Performance optimization: local state for sliding to avoid recomposition spam
    var sliderScrubbingValue by remember { mutableFloatStateOf(0f) }
    var isScrubbing by remember { mutableStateOf(false) }

    val currentProgress = if (isScrubbing) sliderScrubbingValue else {
        if (duration > 0) position.toFloat() / duration.toFloat() else 0f
    }

    // Gradient background: NO blur — performant on Intel UHD 600 and Oppo Reno 3
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(primaryColor, surfaceColor, surfaceColor)
    )


    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientBrush)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 50f) { // Swipe down to collapse
                                onCollapse()
                            }
                        }
                    }
            ) {
                PlayerUIBody(
                    song = song,
                    isPlaying = isPlaying,
                    position = position,
                    duration = duration,
                    onPositionChange = onPositionChange,
                    onPlayPause = onPlayPause,
                    onSkipNext = onSkipNext,
                    onSkipPrevious = onSkipPrevious,
                    onCollapse = onCollapse,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    audioManager = audioManager,
                    currentProgress = currentProgress
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 50f) { // Swipe down to collapse
                            onCollapse()
                        }
                    }
                }
        ) {
            PlayerUIBody(
                song = song,
                isPlaying = isPlaying,
                position = position,
                duration = duration,
                onPositionChange = onPositionChange,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onCollapse = onCollapse,
                sharedTransitionScope = null,
                animatedVisibilityScope = null,
                audioManager = audioManager,
                currentProgress = currentProgress
            )
        }
    }
}


@Composable
private fun PlayerUIBody(
    song: Song,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onPositionChange: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onCollapse: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    audioManager: android.media.AudioManager,
    currentProgress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Filled.KeyboardArrowDown, "Collapse", modifier = Modifier.size(32.dp))
            }
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { /* Queue shortcut */ }) {
                Icon(Icons.Filled.QueueMusic, "Queue")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Album Art with Volume Gesture — NO blur, clean shadow via graphicsLayer
        Box(
            modifier = Modifier
                .size(340.dp)
                .graphicsLayer {
                    shadowElevation = 20.dp.toPx()
                    shape = RoundedCornerShape(24.dp)
                    clip = true
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        // Swipe up/down to control system volume
                        val delta = -dragAmount / 10f
                        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
                        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                        val nextVolume = (currentVolume + delta).coerceIn(0f, maxVolume.toFloat()).toInt()
                        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, nextVolume, 0)
                    }
                }
        ) {
            val imageModifier = Modifier.fillMaxSize()
            val finalImageModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    imageModifier.sharedElement(
                        rememberSharedContentState(key = "album_art_${song.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            } else imageModifier

            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                modifier = finalImageModifier,
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Info Section
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} • ${song.mood} (${song.bpm} BPM)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Expressive Waveform Seekbar
        ExpressiveWaveformSeekbar(
            progress = currentProgress,
            onValueChange = {
                onPositionChange((it * duration).toLong())
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(position), style = MaterialTheme.typography.labelMedium)
            Text(formatTime(duration), style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Expressive Controls — centered with breathing room
        ExpressiveControlLayout(
            onPlayPause = onPlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            isPlaying = isPlaying
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom reach zone spacers are handled by the weight and padding
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
