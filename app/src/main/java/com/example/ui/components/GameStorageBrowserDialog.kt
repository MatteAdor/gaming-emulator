package com.example.ui.components

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.ExecutableInfo
import com.example.core.FolderScanResult
import com.example.core.GameFolderScanner
import com.example.core.StorageLocation
import com.example.ui.theme.*
import java.io.File

@Composable
fun GameStorageBrowserDialog(
    initialPath: String = "",
    onDismiss: () -> Unit,
    onFolderSelected: (FolderScanResult, ExecutableInfo?) -> Unit,
    onOpenSystemFolderPicker: () -> Unit,
    onOpenSystemFilePicker: () -> Unit
) {
    val context = LocalContext.current
    val quickLocations = remember { GameFolderScanner.getQuickAccessLocations(context) }

    // Start in Downloads or provided initialPath
    var currentDir by remember {
        mutableStateOf(
            if (initialPath.isNotBlank() && File(initialPath).exists()) {
                File(initialPath)
            } else {
                val dl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (dl.exists()) dl else (context.getExternalFilesDir(null) ?: context.filesDir)
            }
        )
    }

    var selectedExeForFolder by remember { mutableStateOf<ExecutableInfo?>(null) }
    var previewScanResult by remember { mutableStateOf<FolderScanResult?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    // Update preview scan whenever currentDir changes
    LaunchedEffect(currentDir) {
        isScanning = true
        previewScanResult = GameFolderScanner.scanDirectory(currentDir)
        selectedExeForFolder = previewScanResult?.executables?.firstOrNull()
        isScanning = false
    }

    val dirFiles = remember(currentDir) {
        try {
            val list = currentDir.listFiles()?.toList() ?: emptyList()
            // Folders first, then .exe, then others
            list.sortedWith(
                compareBy<File> { !it.isDirectory }
                    .thenBy { !it.name.lowercase().endsWith(".exe") }
                    .thenBy { it.name.lowercase() }
            )
        } catch (_: Exception) {
            emptyList()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, AppBorder, RoundedCornerShape(12.dp)),
            color = AppSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Esplora Memoria Telefono",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Seleziona la cartella del gioco o il file .exe",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_file_browser_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // System SAF Picker Quick Launch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenSystemFolderPicker,
                        modifier = Modifier.weight(1f).testTag("system_folder_picker_btn"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cartella da Sistema", fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = onOpenSystemFilePicker,
                        modifier = Modifier.weight(1f).testTag("system_file_picker_btn"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("File .exe da Sistema", fontSize = 11.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Locations Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickLocations.forEach { loc ->
                        val isCurrent = currentDir.absolutePath == loc.path
                        AssistChip(
                            onClick = {
                                val target = File(loc.path)
                                if (target.exists()) {
                                    currentDir = target
                                } else {
                                    target.mkdirs()
                                    currentDir = target
                                }
                            },
                            label = { Text(loc.title, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isCurrent) AccentBlue else TextMuted)
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isCurrent) AppSurfaceElevated else AppBackground,
                                labelColor = if (isCurrent) TextPrimary else TextSecondary
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = if (isCurrent) AccentBlue else AppBorder
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Breadcrumb and "Up" directory navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppBackground, RoundedCornerShape(6.dp))
                        .border(1.dp, AppBorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val parentFile = currentDir.parentFile
                    IconButton(
                        onClick = {
                            if (parentFile != null && parentFile.canRead()) {
                                currentDir = parentFile
                            }
                        },
                        enabled = parentFile != null,
                        modifier = Modifier.size(28.dp).testTag("folder_up_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna su",
                            tint = if (parentFile != null) TextPrimary else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = currentDir.absolutePath,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Directory Contents List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(AppBackground, RoundedCornerShape(6.dp))
                        .border(1.dp, AppBorder, RoundedCornerShape(6.dp))
                ) {
                    if (dirFiles.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Nessun file o cartella accessibile qui",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val demoDir = GameFolderScanner.createSampleGameFolder(context)
                                    currentDir = demoDir
                                    Toast.makeText(context, "Creata cartella di prova con hl2.exe e steam_api64.dll!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("create_demo_game_folder_btn")
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Crea cartella di test di esempio", fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(dirFiles, key = { it.absolutePath }) { file ->
                                val isDirectory = file.isDirectory
                                val isExe = file.name.lowercase().endsWith(".exe")
                                val isDll = file.name.lowercase().endsWith(".dll")

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            if (isDirectory) {
                                                currentDir = file
                                            } else if (isExe) {
                                                // Scan the containing directory and pick this exe
                                                val scanRes = GameFolderScanner.scanSingleExeFile(file)
                                                val exeInfo = scanRes.executables.find { it.absolutePath == file.absolutePath }
                                                onFolderSelected(scanRes, exeInfo)
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isDirectory -> Icons.Default.Folder
                                            isExe -> Icons.Default.SportsEsports
                                            isDll -> Icons.Default.Settings
                                            else -> Icons.Default.InsertDriveFile
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            isDirectory -> AccentBlue
                                            isExe -> StatusActive
                                            isDll -> StatusSteam
                                            else -> TextMuted
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            color = if (isExe) TextPrimary else if (isDirectory) TextPrimary else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isExe || isDirectory) FontWeight.Medium else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!isDirectory) {
                                            val sizeKb = file.length() / 1024
                                            val sizeStr = if (sizeKb > 1024) "${sizeKb / 1024} MB" else "$sizeKb KB"
                                            Text(
                                                text = sizeStr,
                                                color = TextMuted,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    if (isExe) {
                                        Surface(
                                            color = AccentBlueSubtle,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ESEGUIBILE",
                                                color = StatusActive,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (isDll) {
                                        Surface(
                                            color = AppSurfaceElevated,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "DLL",
                                                color = TextMuted,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (isDirectory) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(color = AppBorderSubtle, thickness = 0.5.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Panel: Current Folder Summary & Confirm Action
                previewScanResult?.let { scan ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppSurfaceElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Cartella: ${scan.directoryName}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${scan.totalFilesCount} file rilevati (dll, dati, asset inclusi)",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                if (scan.detectedDlls.isNotEmpty()) {
                                    Text(
                                        text = "Librerie: ${scan.detectedDlls.joinToString(", ")}",
                                        color = StatusSteam,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onFolderSelected(scan, selectedExeForFolder)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("select_current_folder_btn")
                            ) {
                                Text("Seleziona questa cartella", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        // If multiple executables found in this folder, allow picking which one
                        if (scan.executables.size > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Eseguibile principale (${scan.executables.size} trovati):",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                scan.executables.forEach { exe ->
                                    val isSelected = (selectedExeForFolder?.absolutePath == exe.absolutePath)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedExeForFolder = exe },
                                        label = { Text(exe.name, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AccentBlue,
                                            selectedLabelColor = TextPrimary,
                                            containerColor = AppBackground,
                                            labelColor = TextMuted
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
