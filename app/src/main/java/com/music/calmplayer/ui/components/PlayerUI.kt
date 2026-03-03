package com.music.calmplayer.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.music.calmplayer.data.Song

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
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

@OptIn(ExperimentalSharedTransitionApi::class)
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

    var sliderScrubbingValue by remember { mutableFloatStateOf(0f) }
    var isScrubbing by remember { mutableStateOf(false) }

    val currentProgress = if (isScrubbing) sliderScrubbingValue else {
        if (duration > 0) position.toFloat() / duration.toFloat() else 0f
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(primaryColor, surfaceColor, surfaceColor)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 50f) onCollapse()
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

@OptIn(ExperimentalSharedTransitionApi::class)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Filled.KeyboardArrowDown, "Collapse", modifier = Modifier.size(32.dp))
            }
            Text(text = "Now Playing", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { }) { Icon(Icons.Filled.QueueMusic, "Queue") }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Album Art
        Box(
            modifier = Modifier
                .size(320.dp)
                .graphicsLayer {
                    shadowElevation = 12.dp.toPx()
                    shape = RoundedCornerShape(24.dp)
                    clip = true
                }
        ) {
            val imageModifier = Modifier.fillMaxSize()
            val finalModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
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
                modifier = finalModifier,
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Song Info
        Column(Modifier.fillMaxWidth()) {
            Text(text = song.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(text = song.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Seekbar
        Slider(
            value = currentProgress,
            onValueChange = { onPositionChange((it * duration).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(position), style = MaterialTheme.typography.labelMedium)
            Text(formatTime(duration), style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSkipPrevious) { Icon(Icons.Filled.SkipPrevious, null, modifier = Modifier.size(48.dp)) }
            
            Surface(
                onClick = onPlayPause,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, modifier = Modifier.size(40.dp))
                }
            }

            IconButton(onClick = onSkipNext) { Icon(Icons.Filled.SkipNext, null, modifier = Modifier.size(48.dp)) }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}