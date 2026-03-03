package com.music.calmplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.music.calmplayer.data.Song
import com.music.calmplayer.domain.MusicViewModel
import com.music.calmplayer.domain.SettingsViewModel
import com.music.calmplayer.ui.screens.IntroScreen
import com.music.calmplayer.ui.navigation.CalmMusicNavHost
import com.music.calmplayer.ui.navigation.Screen
import com.music.calmplayer.ui.theme.CalmMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make the app draw under the status bar for a modern look
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val themeConfig by settingsVm.themeState.collectAsState()
            
            val isDark = when(themeConfig) {
                com.music.calmplayer.data.ThemeConfig.DARK -> true
                com.music.calmplayer.data.ThemeConfig.LIGHT -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            CalmMusicTheme(darkTheme = isDark) {
                var hasSeenIntro by remember { mutableStateOf(false) } 
                
                if (!hasSeenIntro) {
                    IntroScreen(
                        onComplete = { hasSeenIntro = true },
                        viewModel = settingsVm
                    )
                } else {
                    AppMainLayout()
                }
            }
        }
    }
}

// 🔥 Added OptIn for SharedTransitionLayout and AnimatedVisibility
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppMainLayout() {
    val navController = rememberNavController()
    val musicVm: MusicViewModel = viewModel()
    val currentSong by musicVm.currentSong.collectAsState()
    val isPlaying by musicVm.isPlaying.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    SharedTransitionLayout {
        Scaffold(
            bottomBar = {
                Column {
                    AnimatedVisibility(
                        visible = currentSong != null && currentRoute != Screen.Player.route,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it }
                    ) {
                        currentSong?.let { song ->
                            BouncyMiniPlayer(
                                song = song,
                                isPlaying = isPlaying,
                                onPlayPause = { musicVm.togglePlayPause() },
                                onExpand = { navController.navigate(Screen.Player.route) },
                                sharedScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this
                            )
                        }
                    }
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == Screen.Home.route,
                            onClick = { navController.navigate(Screen.Home.route) },
                            icon = { Icon(Icons.Filled.Home, null) },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Library.route,
                            onClick = { navController.navigate(Screen.Library.route) },
                            icon = { Icon(Icons.Filled.LibraryMusic, null) },
                            label = { Text("Library") }
                        )
                    }
                }
            }
        ) { padding ->
            CalmMusicNavHost(
                navController = navController,
                modifier = Modifier.padding(padding),
                onSongClick = { song, list ->
                    musicVm.playSong(song, list)
                    navController.navigate(Screen.Player.route)
                },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
    }
}

// 🔥 Added OptIn for shared element transitions
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BouncyMiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onExpand: () -> Unit,
    sharedScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val animatedY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "miniPlayerBounce"
    )

    with(sharedScope) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(64.dp)
                .graphicsLayer { translationY = animatedY }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (offsetY < -40f) onExpand()
                            offsetY = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            offsetY = (offsetY + dragAmount).coerceIn(-100f, 0f)
                        }
                    )
                }
                .clickable { onExpand() },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        // 🔥 This enables the cool transition to the Player screen
                        .sharedElement(
                            rememberSharedContentState(key = "album_art_${song.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                IconButton(onClick = onPlayPause) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null)
                }
            }
        }
    }
}