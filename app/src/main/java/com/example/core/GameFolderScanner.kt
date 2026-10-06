package com.example.core

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.example.model.GameStoreType
import java.io.File

data class ExecutableInfo(
    val name: String,
    val relativePath: String,
    val absolutePath: String,
    val sizeBytes: Long,
    val isRecommendedMain: Boolean
)

data class FolderScanResult(
    val directoryPath: String,
    val directoryName: String,
    val executables: List<ExecutableInfo>,
    val totalFilesCount: Int,
    val totalSizeBytes: Long,
    val detectedDlls: List<String>,
    val detectedStoreDependencies: List<String>,
    val autoDetectedStoreType: GameStoreType,
    val detectedSteamAppId: String,
    val hasDirectX: Boolean,
    val companionSummary: String
)

data class StorageLocation(
    val title: String,
    val path: String,
    val description: String,
    val exists: Boolean
)

object GameFolderScanner {

    private val IGNORED_EXE_SUBSTRINGS = listOf(
        "unins", "setup", "dxsetup", "vcredist", "crashreporter",
        "update", "install", "launcher_helper", "redist"
    )

    fun getQuickAccessLocations(context: Context): List<StorageLocation> {
        val list = mutableListOf<StorageLocation>()

        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        list.add(
            StorageLocation(
                title = "Download",
                path = downloads.absolutePath,
                description = "Cartella download predefinita",
                exists = downloads.exists()
            )
        )

        val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        list.add(
            StorageLocation(
                title = "Documenti",
                path = documents.absolutePath,
                description = "Documenti e salvataggi",
                exists = documents.exists()
            )
        )

        val sdcard = Environment.getExternalStorageDirectory()
        list.add(
            StorageLocation(
                title = "Memoria interna",
                path = sdcard.absolutePath,
                description = "Root memoria (/storage/emulated/0)",
                exists = sdcard.exists()
            )
        )

        val gamesDir = File(sdcard, "Games")
        list.add(
            StorageLocation(
                title = "Cartella Giochi (/Games)",
                path = gamesDir.absolutePath,
                description = "Cartella standard per giochi PC",
                exists = gamesDir.exists()
            )
        )

        val appDir = context.getExternalFilesDir(null) ?: context.filesDir
        list.add(
            StorageLocation(
                title = "Spazio App CracrabracBox",
                path = appDir.absolutePath,
                description = "Archiviazione interna dell'applicazione",
                exists = appDir.exists()
            )
        )

        return list
    }

    fun scanDirectory(dir: File): FolderScanResult {
        if (!dir.exists() || !dir.isDirectory) {
            return FolderScanResult(
                directoryPath = dir.absolutePath,
                directoryName = dir.name,
                executables = emptyList(),
                totalFilesCount = 0,
                totalSizeBytes = 0L,
                detectedDlls = emptyList(),
                detectedStoreDependencies = emptyList(),
                autoDetectedStoreType = GameStoreType.STANDALONE,
                detectedSteamAppId = "",
                hasDirectX = false,
                companionSummary = "Cartella vuota o inaccessibile"
            )
        }

        val allFiles = mutableListOf<File>()
        collectFiles(dir, allFiles, maxDepth = 4, currentDepth = 0)

        val executables = mutableListOf<ExecutableInfo>()
        val detectedDlls = mutableListOf<String>()
        val detectedStoreDeps = mutableListOf<String>()
        var detectedSteamAppId = ""
        var autoStoreType = GameStoreType.STANDALONE
        var hasDirectX = false
        var totalSizeBytes = 0L

        for (file in allFiles) {
            totalSizeBytes += file.length()
            val fileName = file.name
            val lower = fileName.lowercase()

            if (lower.endsWith(".exe")) {
                val relPath = file.relativeToOrNull(dir)?.path ?: fileName
                val isIgnored = IGNORED_EXE_SUBSTRINGS.any { lower.contains(it) }
                val isPrimary = !isIgnored && (
                    lower.contains(dir.name.lowercase().take(4)) ||
                    !lower.contains("helper")
                )
                executables.add(
                    ExecutableInfo(
                        name = fileName,
                        relativePath = relPath,
                        absolutePath = file.absolutePath,
                        sizeBytes = file.length(),
                        isRecommendedMain = isPrimary
                    )
                )
            } else if (lower.endsWith(".dll")) {
                if (lower == "steam_api64.dll" || lower == "steam_api.dll") {
                    detectedDlls.add(fileName)
                    detectedStoreDeps.add("Steamworks API ($fileName)")
                    autoStoreType = GameStoreType.STEAM
                } else if (lower.contains("eossdk") || lower.contains("epic")) {
                    detectedDlls.add(fileName)
                    detectedStoreDeps.add("Epic Online Services ($fileName)")
                    if (autoStoreType == GameStoreType.STANDALONE) {
                        autoStoreType = GameStoreType.EPIC_GAMES
                    }
                } else if (lower == "dxgi.dll" || lower == "d3d11.dll" || lower == "d3d9.dll" || lower == "d3d12.dll") {
                    detectedDlls.add(fileName)
                    hasDirectX = true
                }
            } else if (lower == "steam_appid.txt") {
                try {
                    val content = file.readText().trim()
                    val id = content.lines().firstOrNull()?.trim() ?: ""
                    if (id.all { it.isDigit() } && id.isNotEmpty()) {
                        detectedSteamAppId = id
                        autoStoreType = GameStoreType.STEAM
                        detectedStoreDeps.add("Steam AppID $id da steam_appid.txt")
                    }
                } catch (_: Exception) {}
            }
        }

        // Sort executables so recommended ones come first
        executables.sortWith(compareByDescending<ExecutableInfo> { it.isRecommendedMain }.thenBy { it.name })

        val totalFiles = allFiles.size
        val sizeMb = totalSizeBytes / (1024 * 1024)
        val summary = buildString {
            append("$totalFiles file e cartelle ($sizeMb MB)")
            if (detectedDlls.isNotEmpty()) {
                append(" • ${detectedDlls.size} DLL di sistema rilevate")
            }
            append(" • L'intera cartella è preservata come Working Directory")
        }

        return FolderScanResult(
            directoryPath = dir.absolutePath,
            directoryName = dir.name,
            executables = executables,
            totalFilesCount = totalFiles,
            totalSizeBytes = totalSizeBytes,
            detectedDlls = detectedDlls.distinct(),
            detectedStoreDependencies = detectedStoreDeps.distinct(),
            autoDetectedStoreType = autoStoreType,
            detectedSteamAppId = detectedSteamAppId,
            hasDirectX = hasDirectX,
            companionSummary = summary
        )
    }

    fun scanSingleExeFile(exeFile: File): FolderScanResult {
        val parent = exeFile.parentFile ?: exeFile
        val folderResult = scanDirectory(parent)
        // Mark this exe as recommended
        val updatedExes = folderResult.executables.map {
            if (it.absolutePath == exeFile.absolutePath) {
                it.copy(isRecommendedMain = true)
            } else {
                it.copy(isRecommendedMain = false)
            }
        }.sortedByDescending { it.isRecommendedMain }

        return folderResult.copy(executables = updatedExes)
    }

    fun scanDocumentTree(context: Context, treeUri: Uri): FolderScanResult {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
        val dirName = rootDoc?.name ?: "Cartella selezionata"

        val executables = mutableListOf<ExecutableInfo>()
        val detectedDlls = mutableListOf<String>()
        val detectedStoreDeps = mutableListOf<String>()
        var detectedSteamAppId = ""
        var autoStoreType = GameStoreType.STANDALONE
        var hasDirectX = false
        var totalCount = 0
        var totalSizeBytes = 0L

        if (rootDoc != null && rootDoc.isDirectory) {
            val docsToScan = mutableListOf<Pair<DocumentFile, String>>()
            docsToScan.add(Pair(rootDoc, ""))

            while (docsToScan.isNotEmpty() && totalCount < 1000) {
                val (currentDoc, prefix) = docsToScan.removeAt(0)
                val children = currentDoc.listFiles()
                for (child in children) {
                    totalCount++
                    val name = child.name ?: continue
                    val lower = name.lowercase()
                    val relPath = if (prefix.isEmpty()) name else "$prefix/$name"

                    if (child.isDirectory) {
                        if (docsToScan.size < 50) {
                            docsToScan.add(Pair(child, relPath))
                        }
                    } else {
                        val len = child.length()
                        totalSizeBytes += len
                        if (lower.endsWith(".exe")) {
                            val isIgnored = IGNORED_EXE_SUBSTRINGS.any { lower.contains(it) }
                            executables.add(
                                ExecutableInfo(
                                    name = name,
                                    relativePath = relPath,
                                    absolutePath = child.uri.toString(),
                                    sizeBytes = len,
                                    isRecommendedMain = !isIgnored
                                )
                            )
                        } else if (lower.endsWith(".dll")) {
                            if (lower == "steam_api64.dll" || lower == "steam_api.dll") {
                                detectedDlls.add(name)
                                detectedStoreDeps.add("Steamworks API ($name)")
                                autoStoreType = GameStoreType.STEAM
                            } else if (lower.contains("eossdk") || lower.contains("epic")) {
                                detectedDlls.add(name)
                                detectedStoreDeps.add("Epic Online Services ($name)")
                                if (autoStoreType == GameStoreType.STANDALONE) {
                                    autoStoreType = GameStoreType.EPIC_GAMES
                                }
                            } else if (lower == "dxgi.dll" || lower == "d3d11.dll") {
                                detectedDlls.add(name)
                                hasDirectX = true
                            }
                        } else if (lower == "steam_appid.txt") {
                            try {
                                context.contentResolver.openInputStream(child.uri)?.use { stream ->
                                    val txt = stream.bufferedReader().readText().trim()
                                    val firstLine = txt.lines().firstOrNull()?.trim() ?: ""
                                    if (firstLine.all { it.isDigit() } && firstLine.isNotEmpty()) {
                                        detectedSteamAppId = firstLine
                                        autoStoreType = GameStoreType.STEAM
                                        detectedStoreDeps.add("Steam AppID $firstLine")
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        }

        executables.sortWith(compareByDescending<ExecutableInfo> { it.isRecommendedMain }.thenBy { it.name })

        val sizeMb = totalSizeBytes / (1024 * 1024)
        val summary = "$totalCount file rilevati ($sizeMb MB) • Tutta la cartella è montata per Wine"

        return FolderScanResult(
            directoryPath = treeUri.toString(),
            directoryName = dirName,
            executables = executables,
            totalFilesCount = totalCount,
            totalSizeBytes = totalSizeBytes,
            detectedDlls = detectedDlls.distinct(),
            detectedStoreDependencies = detectedStoreDeps.distinct(),
            autoDetectedStoreType = autoStoreType,
            detectedSteamAppId = detectedSteamAppId,
            hasDirectX = hasDirectX,
            companionSummary = summary
        )
    }

    private fun collectFiles(dir: File, result: MutableList<File>, maxDepth: Int, currentDepth: Int) {
        if (currentDepth > maxDepth || result.size > 2000) return
        val files = dir.listFiles() ?: return
        for (f in files) {
            result.add(f)
            if (f.isDirectory) {
                collectFiles(f, result, maxDepth, currentDepth + 1)
            }
        }
    }

    fun createSampleGameFolder(context: Context): File {
        val baseDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "Games/HalfLife2")
        baseDir.mkdirs()

        // Create main executable
        val exe = File(baseDir, "hl2.exe")
        if (!exe.exists()) {
            exe.writeBytes(ByteArray(1024 * 128)) // 128 KB placeholder executable
        }

        // Create Steamworks library
        val steamDll = File(baseDir, "steam_api64.dll")
        if (!steamDll.exists()) {
            steamDll.writeBytes(ByteArray(1024 * 64))
        }

        // Create steam_appid.txt with Half-Life 2 AppID (220)
        val appIdFile = File(baseDir, "steam_appid.txt")
        if (!appIdFile.exists()) {
            appIdFile.writeText("220\n")
        }

        // Create DirectX DXVK helper dll
        val dxgiDll = File(baseDir, "dxgi.dll")
        if (!dxgiDll.exists()) {
            dxgiDll.writeBytes(ByteArray(1024 * 32))
        }

        // Create game asset folders and pack files
        val binDir = File(baseDir, "bin")
        binDir.mkdirs()
        val engineDll = File(binDir, "engine.dll")
        if (!engineDll.exists()) {
            engineDll.writeBytes(ByteArray(1024 * 256))
        }

        val dataPak = File(baseDir, "hl2_textures.pak")
        if (!dataPak.exists()) {
            dataPak.writeBytes(ByteArray(1024 * 512))
        }

        val config = File(baseDir, "game_settings.ini")
        if (!config.exists()) {
            config.writeText("[Graphics]\nWidth=1920\nHeight=1080\nVulkan=1\nTurnip=1\n")
        }

        return baseDir
    }
}
