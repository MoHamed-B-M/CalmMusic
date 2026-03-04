package com.music.calmplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.music.calmplayer.domain.MusicViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(musicVm: MusicViewModel, onBack: () -> Unit) {
    // Correctly observing the state from MusicViewModel
    val currentSong by musicVm.currentSong.collectAsState()
    val isPlaying by musicVm.isPlaying.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Now Playing", style = MaterialTheme.typography.titleSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ExpandMore, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            currentSong?.let { song ->
                // Album Art with better styling
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(320.dp)
                        .clip(RoundedCornerShape(24.dp)) // Nice rounded corners
                        .padding(bottom = 24.dp)
                )

                Text(
                    text = song.title, 
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1 // Prevent long titles from breaking layout
                )
                Text(
                    text = song.artist, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Playback Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // FIX: Changed from skipToPrevious() to skipPrevious()
                    IconButton(onClick = { musicVm.skipPrevious() }) {
                        Icon(Icons.Default.SkipPrevious, null, modifier = Modifier.size(48.dp))
                    }
                    
                    // Main Play/Pause Button
                    FilledIconButton(
                        onClick = { musicVm.togglePlayPause() },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // FIX: Changed from skipToNext() to skipNext()
                    IconButton(onClick = { musicVm.skipNext() }) {
                        Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(48.dp))
                    }
                }
            } ?: run {
                // Empty state if no song is selected
                Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.outline)
                Text("No song playing", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}