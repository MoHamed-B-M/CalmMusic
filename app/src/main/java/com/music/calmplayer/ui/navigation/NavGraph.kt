package com.music.calmplayer.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.music.calmplayer.data.Song
import com.music.calmplayer.ui.screens.HomeScreen
import com.music.calmplayer.ui.screens.LibraryScreen
import com.music.calmplayer.ui.screens.SearchScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.music.calmplayer.domain.MusicViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Settings : Screen("settings")
    object Player : Screen("player")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalmMusicNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onSongClick: (Song, List<Song>) -> Unit,
    onSettingsClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSettingsClick = onSettingsClick, 
                onSongClick = onSongClick
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onSongClick = onSongClick,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Library.route) {
            LibraryScreen(
                onSongClick = onSongClick, 
                onSettingsClick = onSettingsClick
            )
        }
        composable(Screen.Settings.route) {
            com.music.calmplayer.ui.screens.SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Player.route) {
            val musicVm: MusicViewModel = viewModel()
            val song by musicVm.currentSong.collectAsState()
            val isPlaying by musicVm.isPlaying.collectAsState()

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = { Text("Now Playing") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                
                com.music.calmplayer.ui.components.PlayerSheet(
                    song = song,
                    isPlaying = isPlaying,
                    position = 0L,
                    duration = 0L,
                    onPositionChange = { musicVm.seekTo(it) },
                    onPlayPause = { musicVm.togglePlayPause() },
                    onSkipNext = { musicVm.skipNext() },
                    onSkipPrevious = { musicVm.skipPrevious() },
                    onDismiss = { navController.popBackStack() },
                    visible = true
                )
            }
        }
    }
}
