package com.music.calmplayer.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.music.calmplayer.data.Song
import androidx.compose.foundation.gestures.detectVerticalDragGestures

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

    val currentProgress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f

    // Matching the dark brown tonal background from your image
    val tonalBackground = Color(0xFF5D4037) 

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tonalBackground)
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
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(28.dp), tint = Color.White)
            }
            Text("Now Playing", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.8f))
            Row {
                IconButton(onClick = {}) { Icon(Icons.Filled.PlaylistAdd, null, tint = Color.White) }
                IconButton(onClick = {}) { Icon(Icons.Filled.Tune, null, tint = Color.White) }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // --- Album Art ---
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .graphicsLayer {
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

        Spacer(modifier = Modifier.weight(0.5f))

        // --- Info ---
        Text(song.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Text(song.artist, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.6f))

        Spacer(modifier = Modifier.height(32.dp))

        // --- Seekbar ---
        Slider(
            value = currentProgress,
            onValueChange = { onPositionChange((it * duration).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFD7CCC8), // Light brown/cream thumb
                activeTrackColor = Color(0xFFD7CCC8),
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(position), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            Text(formatTime(duration), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Main Controls (Pill Container) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.15f)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSkipPrevious) { Icon(Icons.Filled.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(32.dp)) }

            // Custom "Squircle" Play/Pause Button
            Surface(
                onClick = onPlayPause,
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFD7CCC8), // Creamy tonal color
                modifier = Modifier.size(width = 90.dp, height = 70.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color(0xFF3E2723), modifier = Modifier.size(40.dp))
                }
            }

            IconButton(onClick = onSkipNext) { Icon(Icons.Filled.SkipNext, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Secondary Controls (Small Pill) ---
        Row(
            modifier = Modifier
                .width(220.dp)
                .height(64.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.1f)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) { Icon(Icons.Filled.Shuffle, null, tint = Color.White.copy(alpha = 0.7f)) }
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Repeat, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            IconButton(onClick = {}) { Icon(Icons.Filled.FavoriteBorder, null, tint = Color.White.copy(alpha = 0.7f)) }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}