package com.example.model

enum class DynarecPreset(val label: String, val description: String, val bigBlock: Int, val strongMem: Int) {
    AGGRESSIVE(
        "Aggressive BigBlock 2",
        "Massime prestazioni per giochi 3D pesanti. Rischio minimo di desync.",
        bigBlock = 2,
        strongMem = 0
    ),
    BALANCED(
        "Balanced BigBlock 1",
        "Compromesso ottimale tra FPS e stabilità di emulazione.",
        bigBlock = 1,
        strongMem = 1
    ),
    SAFE(
        "Safe Mode (No BigBlock)",
        "Massima compatibilità per giochi con crash improvvisi o DRM.",
        bigBlock = 0,
        strongMem = 2
    )
}

enum class GraphicsDriver(val id: String, val label: String, val version: String, val isTurnip: Boolean) {
    TURNIP_V24_3_0(
        "turnip_24_3",
        "Turnip Mesa v24.3.0 (KGSL)",
        "Freedreno Vulkan 1.3 - Bypass driver Adreno stock",
        isTurnip = true
    ),
    TURNIP_V25_0_DEV(
        "turnip_25_0_dev",
        "Turnip Bleeding Edge v25.0-dev",
        "Mesa Git snapshot con estensioni raytracing VK",
        isTurnip = true
    ),
    ZINK_GL_VULKAN(
        "zink_opengl",
        "Zink (OpenGL 4.6 su Vulkan)",
        "Ottimizzato per giochi PC DirectX 9 e vecchi motori OGL",
        isTurnip = false
    ),
    STOCK_SYSTEM(
        "qualcomm_stock",
        "Qualcomm Stock Adreno Vulkan",
        "Driver OEM proprietario del produttore del dispositivo",
        isTurnip = false
    )
}

enum class DxvkVersion(val label: String, val description: String) {
    DXVK_2_4("DXVK 2.4 (Async + D8VK)", "D3D8, D3D9, D3D10, D3D11 to Vulkan con compilazione shader asincrona"),
    DXVK_2_3_1("DXVK 2.3.1 Stable", "Massima stabilità con giochi Direct3D 11"),
    VKD3D_PROTON_2_13("VKD3D-Proton 2.13", "Direct3D 12 to Vulkan translation")
}

enum class FpsTargetMode(val fps: Int, val label: String, val description: String) {
    FPS_120(120, "120 FPS", "Massima fluidità display 120Hz (Snapdragon S24 Ultra) • 8.3ms frametime"),
    UNCAPPED(0, "Sbloccato / Illimitato", "Nessun cap di frame rate • Present mode Mailbox a bassissima latenza"),
    FPS_90(90, "90 FPS", "Elevata fluidità con temperature e consumi moderati"),
    FPS_60(60, "60 FPS", "VSync standard per risparmio energetico")
}

data class ContainerProfile(
    val id: String = "default_snapdragon",
    val name: String = "Snapdragon 8 Gen 3 / S24 Ultra Profile",
    val wineVersion: String = "Proton-GE 9.11-arm64",
    val dynarecPreset: DynarecPreset = DynarecPreset.AGGRESSIVE,
    val graphicsDriver: GraphicsDriver = GraphicsDriver.TURNIP_V24_3_0,
    val dxvkVersion: DxvkVersion = DxvkVersion.DXVK_2_4,
    val screenResolution: String = "1280x720 (16:9)",
    val targetFps: Int = 120,
    val unlockFps: Boolean = true,
    val bypassVsync: Boolean = true,
    val enableEsync: Boolean = true,
    val enableFsync: Boolean = true,
    val fastNan: Boolean = true,
    val audioLatencyMs: Int = 40,
    val zRamSwappiness: Int = 60,
    val winePrefixPath: String = "/data/data/com.example/files/wineprefix",
    // Steam & Epic Games Store integration layers
    val enableSteamClientStub: Boolean = true,
    val enableEpicEosStub: Boolean = true,
    val steamAccountName: String = "AdrenoPlayer",
    val epicAccountName: String = "EpicUser"
)
