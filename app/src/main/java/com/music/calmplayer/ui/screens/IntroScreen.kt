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
import androidx.compose.ui.graphics.Color
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
    
    // Permission States
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
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
                    Text(
                        "${pagerState.currentPage + 1} of 5",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = {
                        if (pagerState.currentPage < 4) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            // Save preference and finish
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
                2 -> CustomizationStep(themeConfig, onThemeChange = { viewModel.setTheme(it) })
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
        Text(
            "Welcome to a refined listening experience. Optimized for your device, CalmMusic provides high-fidelity offline playback with total privacy.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PermissionsStep(
    hasMusicPermission: Boolean,
    hasBluetoothPermission: Boolean,
    onRequestMusic: () -> Unit,
    onRequestBluetooth: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Permissions Granted!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "All necessary permissions help us serve you better. You are ready to proceed!",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        PermissionCard(
            title = "Music Library Access",
            description = "Read your local music files and display them in the app",
            isGranted = hasMusicPermission,
            onClick = { if (!hasMusicPermission) onRequestMusic() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        PermissionCard(
            title = "Bluetooth Connectivity",
            description = "Connect to Bluetooth speakers, headphones, and audio devices",
            isGranted = hasBluetoothPermission,
            onClick = { if (!hasBluetoothPermission) onRequestBluetooth() }
        )
    }
}

@Composable
fun PermissionCard(title: String, description: String, isGranted: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(if (isGranted) Icons.Filled.Check else Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isGranted) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Text("✓ Granted", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun CustomizationStep(themeConfig: ThemeConfig, onThemeChange: (ThemeConfig) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Palette, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Make Rhythm Yours", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Personalize CalmMusic's appearance to match your style. Choose your system theme, font, or Material You dynamic colors (Android 12+).", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                CustomizationSwitch(
                    title = "Follow System Theme",
                    subtitle = "Automatically switch between light and dark themes",
                    icon = Icons.Filled.DarkMode,
                    checked = themeConfig == ThemeConfig.SYSTEM,
                    onCheckedChange = { if (it) onThemeChange(ThemeConfig.SYSTEM) else onThemeChange(ThemeConfig.DARK) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                CustomizationSwitch(
                    title = "Dynamic Colors (Material You)",
                    subtitle = "Apply colors extracted from your wallpaper",
                    icon = Icons.Filled.ColorLens,
                    checked = true, // Placeholders for now
                    onCheckedChange = { }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                CustomizationSwitch(
                    title = "Festive Theme",
                    subtitle = "Enable festive decorations and seasonal themes",
                    icon = Icons.Filled.AutoAwesome,
                    checked = false,
                    onCheckedChange = { }
                )
            }
        }
    }
}

@Composable
fun GesturesStep() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Swipe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Touch Gestures", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Navigate your music library faster with intuitive swipe and tap gestures.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Mini Player", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start), color = MaterialTheme.colorScheme.primary)
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)) {
            CustomizationSwitch(
                title = "Mini Player Swipe",
                subtitle = "Swipe left/right to skip tracks",
                icon = Icons.Filled.SwipeLeftAlt,
                checked = true,
                onCheckedChange = {}
            )
        }
        
        Text("Full Player", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start), color = MaterialTheme.colorScheme.primary)
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            CustomizationSwitch(
                title = "Swipe to Dismiss",
                subtitle = "Swipe down on full player to return",
                icon = Icons.Filled.SwipeDown,
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}

@Composable
fun FilterMusicStep() {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.FilterList, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Filter Your Music", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Exclude unwanted audio items like voice notes, ringtones or short audios.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column {
                CustomizationSwitch(
                    title = "Blacklist Mode",
                    subtitle = "Hide specific folders from your library",
                    icon = Icons.Filled.Block,
                    checked = true,
                    onCheckedChange = {}
                )
            }
        }
    }
}

@Composable
fun CustomizationSwitch(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if(checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if(checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

