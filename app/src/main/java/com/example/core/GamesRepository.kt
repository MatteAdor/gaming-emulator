package com.example.core

import android.content.Context
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GamesRepository(context: Context) {
    private val storageFile = File(context.filesDir, "cracrabrac_saved_games.json")

    fun loadGames(): List<GameEntry> {
        if (!storageFile.exists()) return emptyList()
        return try {
            val jsonStr = storageFile.readText()
            val array = JSONArray(jsonStr)
            val list = mutableListOf<GameEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val storeTypeName = obj.optString("storeType", "STANDALONE")
                val storeType = runCatching { GameStoreType.valueOf(storeTypeName) }.getOrDefault(GameStoreType.STANDALONE)

                val driverName = obj.optString("recommendedDriver", "TURNIP_V24_3_0")
                val driver = runCatching { GraphicsDriver.valueOf(driverName) }.getOrDefault(GraphicsDriver.TURNIP_V24_3_0)

                val dynarecName = obj.optString("dynarecPreset", "AGGRESSIVE")
                val dynarec = runCatching { DynarecPreset.valueOf(dynarecName) }.getOrDefault(DynarecPreset.AGGRESSIVE)

                list.add(
                    GameEntry(
                        id = obj.optString("id", "game_${System.currentTimeMillis()}_$i"),
                        title = obj.optString("title", "Senza Titolo"),
                        exePath = obj.optString("exePath", ""),
                        exeName = obj.optString("exeName", "game.exe"),
                        gameDirectory = obj.optString("gameDirectory", ""),
                        workingDirectory = obj.optString("workingDirectory", ""),
                        folderFileCount = obj.optInt("folderFileCount", 1),
                        folderSizeBytes = obj.optLong("folderSizeBytes", 0L),
                        detectedDlls = parseStringList(obj.optJSONArray("detectedDlls")),
                        folderTreeUri = if (obj.has("folderTreeUri") && obj.getString("folderTreeUri").isNotBlank()) obj.getString("folderTreeUri") else null,
                        storeType = storeType,
                        steamAppId = obj.optString("steamAppId", ""),
                        epicAppName = obj.optString("epicAppName", ""),
                        launchArgs = obj.optString("launchArgs", ""),
                        installSizeMb = obj.optInt("installSizeMb", 1000),
                        targetFps = obj.optInt("targetFps", 120),
                        recommendedDriver = driver,
                        dynarecPreset = dynarec,
                        enableSteamStub = obj.optBoolean("enableSteamStub", true),
                        enableEpicStub = obj.optBoolean("enableEpicStub", false),
                        linkSharedSteamIpc = obj.optBoolean("linkSharedSteamIpc", true),
                        isStoreClient = obj.optBoolean("isStoreClient", false),
                        playTimeMinutes = obj.optInt("playTimeMinutes", 0),
                        lastPlayed = obj.optString("lastPlayed", "Non avviato")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveGames(games: List<GameEntry>) {
        try {
            val array = JSONArray()
            for (g in games) {
                val obj = JSONObject()
                obj.put("id", g.id)
                obj.put("title", g.title)
                obj.put("exePath", g.exePath)
                obj.put("exeName", g.exeName)
                obj.put("gameDirectory", g.gameDirectory)
                obj.put("workingDirectory", g.workingDirectory)
                obj.put("folderFileCount", g.folderFileCount)
                obj.put("folderSizeBytes", g.folderSizeBytes)

                val dllsArr = JSONArray()
                g.detectedDlls.forEach { dllsArr.put(it) }
                obj.put("detectedDlls", dllsArr)

                obj.put("folderTreeUri", g.folderTreeUri ?: "")
                obj.put("storeType", g.storeType.name)
                obj.put("steamAppId", g.steamAppId)
                obj.put("epicAppName", g.epicAppName)
                obj.put("launchArgs", g.launchArgs)
                obj.put("installSizeMb", g.installSizeMb)
                obj.put("targetFps", g.targetFps)
                obj.put("recommendedDriver", g.recommendedDriver.name)
                obj.put("dynarecPreset", g.dynarecPreset.name)
                obj.put("enableSteamStub", g.enableSteamStub)
                obj.put("enableEpicStub", g.enableEpicStub)
                obj.put("linkSharedSteamIpc", g.linkSharedSteamIpc)
                obj.put("isStoreClient", g.isStoreClient)
                obj.put("playTimeMinutes", g.playTimeMinutes)
                obj.put("lastPlayed", g.lastPlayed)
                array.put(obj)
            }
            storageFile.writeText(array.toString(2))
        } catch (_: Exception) {}
    }

    private fun parseStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.getString(i))
        }
        return list
    }
}
