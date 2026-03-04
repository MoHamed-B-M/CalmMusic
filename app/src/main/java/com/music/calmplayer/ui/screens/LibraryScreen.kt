package com.music.calmplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.music.calmplayer.domain.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(musicVm: MusicViewModel) {
    // State to track which filter is selected
    var selectedFilter by remember { mutableStateOf("Songs") }
    val allSongs by musicVm.songs.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Buttons
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

        // List logic
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            when (selectedFilter) {
                "Songs" -> {
                    items(allSongs) { song ->
                        ListItem(
                            headlineContent = { Text(song.title) },
                            supportingContent = { Text(song.artist) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                "Albums" -> {
                    val albums = allSongs.groupBy { it.album }
                    items(albums.keys.toList()) { albumName ->
                        ListItem(headlineContent = { Text(albumName) })
                    }
                }
                "Artists" -> {
                    val artists = allSongs.groupBy { it.artist }
                    items(artists.keys.toList()) { artistName ->
                        ListItem(headlineContent = { Text(artistName) })
                    }
                }
            }
        }
    }
}