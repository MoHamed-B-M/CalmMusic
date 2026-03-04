package com.music.calmplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.music.calmplayer.domain.MusicViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(musicVm: MusicViewModel, onBack: () -> Unit) {
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
                // Album Art
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier.size(300.dp).padding(16.dp)
                )

                Text(song.title, style = MaterialTheme.typography.headlineMedium)
                Text(song.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(32.dp))

                // Playback Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { musicVm.skipToPrevious() }) {
                        Icon(Icons.Default.SkipPrevious, null, modifier = Modifier.size(48.dp))
                    }
                    
                    // Main Play/Pause Button
                    FilledIconButton(
                        onClick = { musicVm.togglePlayPause() },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                    }

                    IconButton(onClick = { musicVm.skipToNext() }) {
                        Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(48.dp))
                    }
                }
            } ?: run {
                Text("No song playing")
            }
        }
    }
}