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
    
    // Collecting states from ViewModel
    val themeConfig by viewModel.themeState.collectAsState()
    val isDynamicColorEnabled by viewModel.dynamicColorState.collectAsState() // New state from VM
    
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
                            // FIXED: Mark intro as done in SharedPreferences
                            val sharedPrefs = context.getSharedPreferences("calm_prefs", Context.MODE_PRIVATE)
                            sharedPrefs.edit().putBoolean("is_first_run", false).apply()
                            
                            // Trigger navigation to Home
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
                // FIXED: Passing dynamic color state
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

// ... WelcomeStep and PermissionsStep remain same as your code ...

@Composable
fun CustomizationStep(
    themeConfig: ThemeConfig, 
    isDynamicEnabled: Boolean,
    onThemeChange: (ThemeConfig) -> Unit,
    onDynamicToggle: (Boolean) -> Unit
) {
    val canUseDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
        Text("Make CalmMusic Yours", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Personalize appearance to match your style. Supports Material You dynamic colors on Android 12+.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
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
                
                // FIXED: Linked to logic and build check
                CustomizationSwitch(
                    title = "Dynamic Colors",
                    subtitle = if (canUseDynamic) "Apply colors from your wallpaper" else "Not supported on this Android version",
                    icon = Icons.Filled.ColorLens,
                    checked = isDynamicEnabled && canUseDynamic,
                    onCheckedChange = { if (canUseDynamic) onDynamicToggle(it) }
                )
            }
        }
    }
}

// ... Rest of your Step functions (Welcome, Gestures, etc.) ...
/* 
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
*/
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

