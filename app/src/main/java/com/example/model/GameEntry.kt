package com.example.model

enum class GameStoreType(val label: String, val badgeColorHex: Long) {
    STANDALONE("Standalone .EXE", 0xFF00E5FF),
    STEAM("Steam (Goldberg Stub)", 0xFF2196F3),
    EPIC_GAMES("Epic Games (EOS Stub)", 0xFFE040FB)
}

data class GameEntry(
    val id: String,
    val title: String,
    val exePath: String = "",
    val exeName: String,
    val gameDirectory: String = "",
    val workingDirectory: String = "",
    val folderFileCount: Int = 0,
    val folderSizeBytes: Long = 0L,
    val detectedDlls: List<String> = emptyList(),
    val folderTreeUri: String? = null,
    val storeType: GameStoreType = GameStoreType.STANDALONE,
    val steamAppId: String = "",
    val epicAppName: String = "",
    val launchArgs: String = "",
    val installSizeMb: Int = 0,
    val targetFps: Int = 120,
    val recommendedDriver: GraphicsDriver = GraphicsDriver.TURNIP_V24_3_0,
    val dynarecPreset: DynarecPreset = DynarecPreset.AGGRESSIVE,
    val enableSteamStub: Boolean = true,
    val enableEpicStub: Boolean = false,
    val linkSharedSteamIpc: Boolean = true, // Consente a giochi esterni di comunicare con il client Steam condiviso
    val isStoreClient: Boolean = false,
    val playTimeMinutes: Int = 0,
    val lastPlayed: String = "Non avviato"
)

object DefaultStoreClients {
    val SteamClient = GameEntry(
        id = "store_steam_client",
        title = "Steam",
        exePath = "C:/Program Files (x86)/Steam/steam.exe",
        exeName = "steam.exe",
        gameDirectory = "C:/Program Files (x86)/Steam",
        workingDirectory = "C:/Program Files (x86)/Steam",
        folderFileCount = 342,
        storeType = GameStoreType.STEAM,
        launchArgs = "-no-browser -no-cef-sandbox -cef-disable-gpu +open steam://open/minigameslist",
        steamAppId = "",
        enableSteamStub = true,
        enableEpicStub = false,
        linkSharedSteamIpc = true,
        isStoreClient = true,
        recommendedDriver = GraphicsDriver.TURNIP_V24_3_0,
        dynarecPreset = DynarecPreset.BALANCED
    )

    val EpicGamesClient = GameEntry(
        id = "store_epic_client",
        title = "Epic Games Store",
        exePath = "C:/Program Files/Epic Games/Launcher/Portal/Binaries/Win64/EpicGamesLauncher.exe",
        exeName = "EpicGamesLauncher.exe",
        gameDirectory = "C:/Program Files/Epic Games/Launcher",
        workingDirectory = "C:/Program Files/Epic Games/Launcher/Portal/Binaries/Win64",
        folderFileCount = 418,
        storeType = GameStoreType.EPIC_GAMES,
        launchArgs = "-SkipBuildPatchPrereq -OpenGL",
        enableSteamStub = false,
        enableEpicStub = true,
        linkSharedSteamIpc = false,
        isStoreClient = true,
        recommendedDriver = GraphicsDriver.TURNIP_V24_3_0,
        dynarecPreset = DynarecPreset.BALANCED
    )
}
