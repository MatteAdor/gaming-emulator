package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.example.core.ExecutableInfo
import com.example.core.FolderScanResult
import com.example.core.GameFolderScanner
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun AddGameDialog(
    onDismiss: () -> Unit,
    onAdd: (GameEntry) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var exePath by remember { mutableStateOf("") }
    var exeName by remember { mutableStateOf("") }
    var gameDirectory by remember { mutableStateOf("") }
    var folderFileCount by remember { mutableIntStateOf(0) }
    var folderSizeBytes by remember { mutableLongStateOf(0L) }
    var detectedDlls by remember { mutableStateOf<List<String>>(emptyList()) }
    var storeType by remember { mutableStateOf(GameStoreType.STANDALONE) }
    var steamAppId by remember { mutableStateOf("") }
    var linkSharedSteamIpc by remember { mutableStateOf(true) }
    var launchArgs by remember { mutableStateOf("") }
    var selectedTargetFps by remember { mutableIntStateOf(120) }
    var selectedDriver by remember { mutableStateOf(GraphicsDriver.TURNIP_V24_3_0) }
    var selectedDynarec by remember { mutableStateOf(DynarecPreset.AGGRESSIVE) }

    var showStorageBrowser by remember { mutableStateOf(false) }
    var lastScanResult by remember { mutableStateOf<FolderScanResult?>(null) }

    fun applyScanResult(scan: FolderScanResult, chosenExe: ExecutableInfo?) {
        lastScanResult = scan
        gameDirectory = scan.directoryPath
        folderFileCount = scan.totalFilesCount
        folderSizeBytes = scan.totalSizeBytes
        detectedDlls = scan.detectedDlls

        val exe = chosenExe ?: scan.executables.firstOrNull()
        if (exe != null) {
            exePath = exe.absolutePath
            exeName = exe.name
            if (title.isBlank()) {
                val cleanTitle = scan.directoryName.replace("_", " ").replace("-", " ")
                    .ifBlank { exe.name.substringBeforeLast(".exe") }
                title = cleanTitle
            }
        } else {
            exePath = "${scan.directoryPath}/game.exe"
            exeName = "game.exe"
            if (title.isBlank()) {
                title = scan.directoryName
            }
        }

        // Auto-configure Steam or Epic compatibility based on detected files in folder
        if (scan.autoDetectedStoreType == GameStoreType.STEAM) {
            storeType = GameStoreType.STEAM
            linkSharedSteamIpc = true
            if (scan.detectedSteamAppId.isNotBlank()) {
                steamAppId = scan.detectedSteamAppId
            }
        } else if (scan.autoDetectedStoreType == GameStoreType.EPIC_GAMES) {
            storeType = GameStoreType.EPIC_GAMES
        }
    }

    // System SAF Document Tree Picker (Select Whole Game Folder)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val scan = GameFolderScanner.scanDocumentTree(context, uri)
            applyScanResult(scan, scan.executables.firstOrNull())
            Toast.makeText(context, "Cartella selezionata: ${scan.directoryName} (${scan.totalFilesCount} file)", Toast.LENGTH_SHORT).show()
        }
    }

    // System SAF Single File Picker (Select .exe)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val doc = DocumentFile.fromSingleUri(context, uri)
            val fileName = doc?.name ?: "game.exe"
            exePath = uri.toString()
            exeName = fileName
            if (title.isBlank()) {
                title = fileName.substringBeforeLast(".exe")
            }
            gameDirectory = doc?.parentFile?.uri?.toString() ?: uri.toString().substringBeforeLast("/")
            folderFileCount = 1
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Aggiungi gioco",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Seleziona la cartella con tutti i file del gioco o il file .exe",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Action: Browse Phone Storage (Folder & Files)
                Surface(
                    color = AppSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (gameDirectory.isNotBlank()) AccentBlue else AppBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Origine File di Gioco",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (gameDirectory.isNotBlank()) {
                                TextButton(
                                    onClick = { showStorageBrowser = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Cambia", fontSize = 11.sp, color = AccentBlue)
                                }
                            }
                        }

                        if (gameDirectory.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            // Folder inspection badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = gameDirectory.substringAfterLast("/"),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = gameDirectory,
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            // Companion files guarantee
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = AppBackground,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusActive, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$folderFileCount file rilevati nella cartella",
                                            color = StatusActive,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "Tutti i file (.dll, asset, cartelle dati) rimangono intatti e disponibili come working directory per Wine.",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    if (detectedDlls.isNotEmpty()) {
                                        Text(
                                            text = "Librerie rilevate: ${detectedDlls.joinToString(", ")}",
                                            color = StatusSteam,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 3.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Sfoglia la memoria per scegliere la cartella del gioco. I giochi per PC richiedono DLL e file accessori: CracrabracBox preserverà l'intera directory.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showStorageBrowser = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).testTag("open_storage_browser_btn"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sfoglia cartelle", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }

                                OutlinedButton(
                                    onClick = { folderPickerLauncher.launch(null) },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).testTag("system_saf_folder_btn"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gestione File", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Game Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome del gioco") },
                    placeholder = { Text("es. Half-Life 2, Cyberpunk 2077") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("game_title_input"),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = AppBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextMuted
                    )
                )

                // Executable selection / text
                OutlinedTextField(
                    value = if (exeName.isNotBlank()) exeName else exePath,
                    onValueChange = {
                        exeName = it
                        exePath = it
                    },
                    label = { Text("Eseguibile principale (.exe)") },
                    placeholder = { Text("es. hl2.exe, bin/x64/game.exe") },
                    trailingIcon = {
                        IconButton(onClick = { showStorageBrowser = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Sfoglia", tint = AccentBlue)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("game_exe_input"),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = AppBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextMuted
                    )
                )

                Text(
                    text = "Origine & Compatibilità Store",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Store Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameStoreType.values().forEach { type ->
                        val isSelected = storeType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { storeType = type },
                            label = {
                                Text(
                                    text = when (type) {
                                        GameStoreType.STANDALONE -> "Esterno"
                                        GameStoreType.STEAM -> "Steam"
                                        GameStoreType.EPIC_GAMES -> "Epic Games"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppSurfaceElevated,
                                selectedLabelColor = TextPrimary,
                                containerColor = AppBackground,
                                labelColor = TextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = AppBorder,
                                selectedBorderColor = AccentBlue
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).testTag("store_selector_${type.name}")
                        )
                    }
                }

                // If standalone / external game: option to bridge with shared Steam
                if (storeType == GameStoreType.STANDALONE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { linkSharedSteamIpc = !linkSharedSteamIpc }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Collega al client Steam condiviso",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Consente al gioco di comunicare con le librerie e il runtime di Steam nel container.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Switch(
                            checked = linkSharedSteamIpc,
                            onCheckedChange = { linkSharedSteamIpc = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = AccentBlue,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = AppSurfaceElevated
                            )
                        )
                    }
                }

                if (storeType == GameStoreType.STEAM) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "L'emulatore Goldberg Steamworks intercetta steam_api64.dll e comunica con il client Steam condiviso.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = steamAppId,
                            onValueChange = { steamAppId = it.filter { c -> c.isDigit() } },
                            label = { Text("Steam App ID (facoltativo)") },
                            placeholder = { Text("es. 480, 220, 1091500") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("steam_appid_input"),
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = AppBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                } else if (storeType == GameStoreType.EPIC_GAMES) {
                    Text(
                        text = "I token dummy EOS vengono iniettati per consentire l'esecuzione offline e l'integrazione con il launcher Epic.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = launchArgs,
                    onValueChange = { launchArgs = it },
                    label = { Text("Parametri di avvio (facoltativi)") },
                    placeholder = { Text("es. -dx11 -windowed") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("launch_args_input"),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = AppBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Text(
                    text = "Limite FPS & Refresh Rate",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        120 to "120 FPS",
                        0 to "Sbloccato",
                        90 to "90 FPS",
                        60 to "60 FPS"
                    ).forEach { (fpsVal, label) ->
                        val isSelected = selectedTargetFps == fpsVal
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTargetFps = fpsVal },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlue,
                                selectedLabelColor = TextPrimary,
                                containerColor = AppBackground,
                                labelColor = TextMuted
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).testTag("fps_chip_$fpsVal")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.trim().ifBlank {
                        if (exeName.isNotBlank()) exeName.substringBeforeLast(".exe") else "Mio Gioco PC"
                    }
                    val finalExeName = exeName.trim().ifBlank {
                        exePath.substringAfterLast("/").substringAfterLast("\\").ifBlank { "game.exe" }
                    }
                    val finalExePath = exePath.trim().ifBlank { finalExeName }
                    val finalAppId = if (storeType == GameStoreType.STEAM && steamAppId.isBlank()) "480" else steamAppId
                    val newGame = GameEntry(
                        id = "game_${System.currentTimeMillis()}",
                        title = finalTitle,
                        exePath = finalExePath,
                        exeName = finalExeName,
                        gameDirectory = gameDirectory.ifBlank { finalExePath.substringBeforeLast("/") },
                        workingDirectory = gameDirectory.ifBlank { finalExePath.substringBeforeLast("/") },
                        folderFileCount = if (folderFileCount > 0) folderFileCount else 1,
                        folderSizeBytes = folderSizeBytes,
                        detectedDlls = detectedDlls,
                        storeType = storeType,
                        steamAppId = finalAppId,
                        launchArgs = launchArgs.trim(),
                        installSizeMb = if (folderSizeBytes > 0) (folderSizeBytes / (1024 * 1024)).toInt().coerceAtLeast(10) else (800..5000).random(),
                        targetFps = selectedTargetFps,
                        recommendedDriver = selectedDriver,
                        dynarecPreset = selectedDynarec,
                        enableSteamStub = (storeType == GameStoreType.STEAM),
                        enableEpicStub = (storeType == GameStoreType.EPIC_GAMES),
                        linkSharedSteamIpc = (storeType == GameStoreType.STEAM || linkSharedSteamIpc)
                    )
                    onAdd(newGame)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("confirm_add_game_btn")
            ) {
                Text("Aggiungi alla Libreria", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_add_game_btn")
            ) {
                Text("Annulla", color = TextSecondary, fontSize = 13.sp)
            }
        },
        containerColor = AppSurface,
        shape = RoundedCornerShape(12.dp)
    )

    if (showStorageBrowser) {
        GameStorageBrowserDialog(
            initialPath = gameDirectory,
            onDismiss = { showStorageBrowser = false },
            onFolderSelected = { scan, exe ->
                applyScanResult(scan, exe)
                showStorageBrowser = false
            },
            onOpenSystemFolderPicker = {
                showStorageBrowser = false
                folderPickerLauncher.launch(null)
            },
            onOpenSystemFilePicker = {
                showStorageBrowser = false
                filePickerLauncher.launch(arrayOf("*/*"))
            }
        )
    }
}
