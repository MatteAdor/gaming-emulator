package com.example.model

enum class ThermalLevel(val label: String, val colorHex: Long) {
    NONE("Normale (Cool)", 0xFF00E676),
    LIGHT("Moderato (Tiepido)", 0xFF00E5FF),
    MODERATE("Elevato (Caldo)", 0xFFFFB300),
    SEVERE("Thermal Throttling Attivo", 0xFFFF5722),
    CRITICAL("Critico (Protezione Hardware)", 0xFFFF1744)
}

data class TelemetryData(
    val fps: Float = 60.0f,
    val frameTimeMs: Float = 16.6f,
    val cpuUsagePercent: Int = 38,
    val primeCoreFreqGhz: Float = 3.19f,
    val midCoresFreqGhz: Float = 2.80f,
    val littleCoresFreqGhz: Float = 2.02f,
    val gpuClockMhz: Int = 770,
    val gpuUsagePercent: Int = 64,
    val ramUsedMb: Long = 4120,
    val ramTotalMb: Long = 12288,
    val zRamUsageMb: Long = 640,
    val batteryTempC: Float = 34.5f,
    val thermalLevel: ThermalLevel = ThermalLevel.NONE,
    val activeDriver: String = "Turnip Freedreno v24.3",
    val activeDynarecBlock: String = "BigBlock 2 (JIT Active)",
    val inputLatencyMs: Float = 4.2f
)
