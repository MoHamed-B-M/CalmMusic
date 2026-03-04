package com.music.calmplayer.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.music.calmplayer.domain.SettingsViewModel
import com.music.calmplayer.data.ThemeConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroScreen(
    onComplete: () -> Unit,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    
    val themeConfig by viewModel.themeState.collectAsState()
    val isDynamicColorEnabled by viewModel.dynamicColorState.collectAsState()
    
    var hasMusicPermission by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        ) 
    }
    var hasBluetoothPermission by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else { true }
        )
    }

    val requestMusicPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasMusicPermission = it }
    val requestBluetoothPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasBluetoothPermission = it }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) { Text("Back") }
                } else { Spacer(modifier = Modifier.weight(1f)) }
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${pagerState.currentPage + 1} of 5", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = {
                        if (pagerState.currentPage < 4) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            val sharedPrefs = context.getSharedPreferences("calm_prefs", Context.MODE_PRIVATE)
                            sharedPrefs.edit().putBoolean("is_first_run", false).apply()
                            onComplete()
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text(if (pagerState.currentPage < 4) "Next" else "Finish")
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)
        ) { page ->
            when (page) {
                0 -> WelcomeStep()
                1 -> PermissionsStep(
                    hasMusicPermission, hasBluetoothPermission,
                    onRequestMusic = {
                        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
                        requestMusicPermission.launch(perm)
                    },
                    onRequestBluetooth = { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH_CONNECT) }
                )
                2 -> CustomizationStep(
                    themeConfig, 
                    isDynamicEnabled = isDynamicColorEnabled,
                    onThemeChange = { viewModel.setTheme(it) },
                    onDynamicToggle = { viewModel.setDynamicColor(it) } 
                )
                3 -> GesturesStep()
                4 -> FilterMusicStep()
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.MusicNote, null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        Text("CalmMusic", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
        Text("Pure Sound. Zero Distraction.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Welcome to a refined listening experience. Optimized for your device.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun PermissionsStep(hasMusicPermission: Boolean, hasBluetoothPermission: Boolean, onRequestMusic: () -> Unit, onRequestBluetooth: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Permissions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        PermissionCard("Music Access", "Required to play songs", hasMusicPermission, onClick = onRequestMusic)
        Spacer(modifier = Modifier.height(16.dp))
        PermissionCard("Bluetooth", "Required for wireless audio", hasBluetoothPermission, onClick = onRequestBluetooth)
    }
}

@Composable
fun PermissionCard(title: String, description: String, isGranted: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isGranted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CustomizationStep(themeConfig: ThemeConfig, isDynamicEnabled: Boolean, onThemeChange: (ThemeConfig) -> Unit, onDynamicToggle: (Boolean) -> Unit) {
    val canUseDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Appearance", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                CustomizationSwitch("Dark Theme", "Force dark mode", Icons.Filled.DarkMode, themeConfig == ThemeConfig.DARK, onCheckedChange = { onThemeChange(if(it) ThemeConfig.DARK else ThemeConfig.LIGHT) })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                CustomizationSwitch("Dynamic Colors", "Material You colors", Icons.Filled.ColorLens, isDynamicEnabled && canUseDynamic, onCheckedChange = onDynamicToggle)
            }
        }
    }
}
/* 
@Composable
fun GesturesStep() {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.Swipe, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Gestures", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Swipe the player to change tracks or dismiss.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun FilterMusicStep() {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.FilterList, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Filtering", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Hide short audio files and voice notes automatically.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
    }
}
*/
@Composable
fun CustomizationSwitch(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}