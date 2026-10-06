package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.core.GamesRepository
import com.example.core.NativeBridge
import com.example.model.ContainerProfile
import com.example.model.GameEntry
import com.example.model.GameStoreType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GamesRepository(application)

    private val _games = MutableStateFlow<List<GameEntry>>(emptyList())
    val games = _games.asStateFlow()

    private val _selectedTab = MutableStateFlow(AppTab.LIBRARY)
    val selectedTab = _selectedTab.asStateFlow()

    private val _containerProfile = MutableStateFlow(ContainerProfile())
    val containerProfile = _containerProfile.asStateFlow()

    private val _activeSessionPid = MutableStateFlow<Int?>(null)
    val activeSessionPid = _activeSessionPid.asStateFlow()

    private val _activeRunningGame = MutableStateFlow<GameEntry?>(null)
    val activeRunningGame = _activeRunningGame.asStateFlow()

    init {
        _games.value = repository.loadGames()
    }

    fun setSelectedTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun updateContainerProfile(profile: ContainerProfile) {
        _containerProfile.value = profile
    }

    fun addGame(game: GameEntry) {
        val current = _games.value
        // Avoid duplicate ids
        val updated = current.filter { it.id != game.id } + game
        _games.value = updated
        repository.saveGames(updated)
    }

    fun removeGame(game: GameEntry) {
        val updated = _games.value.filter { it.id != game.id }
        _games.value = updated
        repository.saveGames(updated)
    }

    fun launchGame(game: GameEntry): Int {
        val pid = NativeBridge.startSession(
            rootfs = "/data/data/com.example/files/rootfs",
            winePrefix = _containerProfile.value.winePrefixPath,
            executable = game.exePath.ifBlank { game.exeName },
            workingDir = game.workingDirectory.ifBlank { game.gameDirectory },
            turnipIcd = "/opt/turnip/share/vulkan/icd.d/freedreno_icd.aarch64.json",
            dynarecLevel = game.dynarecPreset.bigBlock,
            steamAppId = if (game.storeType == GameStoreType.STEAM) game.steamAppId else "",
            isEpic = (game.storeType == GameStoreType.EPIC_GAMES),
            linkSteamIpc = game.linkSharedSteamIpc
        )
        _activeSessionPid.value = pid
        _activeRunningGame.value = game
        return pid
    }

    fun stopSession() {
        NativeBridge.stopSession()
        _activeSessionPid.value = null
        _activeRunningGame.value = null
    }
}
