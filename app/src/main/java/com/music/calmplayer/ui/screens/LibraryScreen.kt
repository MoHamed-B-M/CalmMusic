package com.music.calmplayer.ui.screens

import androidx.compose.foundation.clickable // Added for clicks
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.* // This provides getValue/setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.music.calmplayer.domain.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(musicVm: MusicViewModel) {
    var selectedFilter by remember { mutableStateOf("Songs") }
    
    // FIX: Changed musicVm.songs to musicVm.librarySongs to match your ViewModel
    val allSongs by musicVm.librarySongs.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(16.dp)) {
            FilterChip(
                selected = selectedFilter == "Songs",
                onClick = { selectedFilter = "Songs" },
                label = { Text("Songs") }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = selectedFilter == "Albums",
                onClick = { selectedFilter = "Albums" },
                label = { Text("Albums") }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = selectedFilter == "Artists",
                onClick = { selectedFilter = "Artists" },
                label = { Text("Artists") }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            when (selectedFilter) {
                "Songs" -> {
                    items(allSongs, key = { it.id }) { song ->
                        ListItem(
                            headlineContent = { Text(song.title) },
                            supportingContent = { Text(song.artist) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { musicVm.playSong(song, allSongs) } // Now it plays!
                        )
                    }
                }
                "Albums" -> {
                    val albums = allSongs.groupBy { it.album }
                    items(albums.keys.toList()) { albumName ->
                        // it.album can be null, handled with ?:
                        ListItem(headlineContent = { Text(albumName ?: "Unknown Album") })
                    }
                }
                "Artists" -> {
                    val artists = allSongs.groupBy { it.artist }
                    items(artists.keys.toList()) { artistName ->
                        ListItem(headlineContent = { Text(artistName ?: "Unknown Artist") })
                    }
                }
            }
        }
    }
}