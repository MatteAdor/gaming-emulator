package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CracrabracBox", appName)
  }

  @Test
  fun `verify game folder scanner preserves companion files and steamworks`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sampleDir = com.example.core.GameFolderScanner.createSampleGameFolder(context)
    val result = com.example.core.GameFolderScanner.scanDirectory(sampleDir)

    assertEquals(sampleDir.absolutePath, result.directoryPath)
    org.junit.Assert.assertTrue("Should find hl2.exe", result.executables.any { it.name == "hl2.exe" })
    org.junit.Assert.assertTrue("Should detect steam_api64.dll", result.detectedDlls.contains("steam_api64.dll"))
    assertEquals(com.example.model.GameStoreType.STEAM, result.autoDetectedStoreType)
    assertEquals("220", result.detectedSteamAppId)
    org.junit.Assert.assertTrue("Companion file count should be greater than 1", result.totalFilesCount >= 5)
  }

  @Test
  fun `verify games repository persists games across recreation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.core.GamesRepository(context)

    val game = com.example.model.GameEntry(
      id = "test_game_1",
      title = "Cyber Game",
      exePath = "/sdcard/Download/CyberGame/game.exe",
      exeName = "game.exe",
      gameDirectory = "/sdcard/Download/CyberGame",
      folderFileCount = 35
    )

    repo.saveGames(listOf(game))
    val loaded = repo.loadGames()

    assertEquals(1, loaded.size)
    assertEquals("Cyber Game", loaded[0].title)
    assertEquals("/sdcard/Download/CyberGame", loaded[0].gameDirectory)
    assertEquals(35, loaded[0].folderFileCount)
  }

  @Test
  fun `verify performance manager supports 120 FPS target and calculates 8ms frametime`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val perf = com.example.core.PerformanceManager(context)

    perf.setTargetFps(120)
    val telemetry = perf.telemetry.value

    org.junit.Assert.assertTrue("FPS should be around 120", telemetry.fps >= 100f)
    org.junit.Assert.assertTrue("Frame time should be around 8ms", telemetry.frameTimeMs <= 10.0f)
  }
}
