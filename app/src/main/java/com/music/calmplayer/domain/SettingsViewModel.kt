package com.music.calmplayer.domain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.music.calmplayer.data.SettingsStore
import com.music.calmplayer.data.ThemeConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsStore = SettingsStore(application)

    val themeState: StateFlow<ThemeConfig> = settingsStore.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeConfig.SYSTEM
    )

    val blockedFolders: StateFlow<Set<String>> = settingsStore.blockedFoldersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val musicFolderUri: StateFlow<String?> = settingsStore.musicFolderUriFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setMusicFolderUri(uri: String) {
        viewModelScope.launch {
            settingsStore.setMusicFolderUri(uri)
        }
    }

    fun setTheme(theme: ThemeConfig) {
        viewModelScope.launch {
            settingsStore.setTheme(theme)
        }
    }

    fun addBlockedFolder(path: String) {
        viewModelScope.launch {
            settingsStore.addBlockedFolder(path)
        }
    }

    fun removeBlockedFolder(path: String) {
        viewModelScope.launch {
            settingsStore.removeBlockedFolder(path)
        }
    }
}
