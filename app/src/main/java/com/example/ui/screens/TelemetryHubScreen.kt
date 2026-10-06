package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TelemetryHubScreen(
    telemetry: TelemetryData,
    socInfo: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hardware Profile Header
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp).testTag("soc_info_card")) {
                Text(
                    text = "Hardware Target",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = socInfo,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Vulkan ICD: ${telemetry.activeDriver}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            HorizontalDivider(color = AppBorderSubtle)
        }

        // 1. Rendering Performance
        item {
            Text(text = "Prestazioni di Rendering", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryStatColumn("FPS Istantanei", "${telemetry.fps.toInt()}", if (telemetry.fps > 45f) StatusActive else StatusWarning)
                TelemetryStatColumn("Frame Time", "${telemetry.frameTimeMs} ms", TextPrimary)
                TelemetryStatColumn("Latenza Input", "${telemetry.inputLatencyMs} ms", TextPrimary)
            }

            LinearProgressIndicator(
                progress = { (telemetry.fps / 60.0f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = AccentBlue,
                trackColor = AppSurfaceElevated
            )

            HorizontalDivider(color = AppBorderSubtle, modifier = Modifier.padding(top = 16.dp))
        }

        // 2. CPU Clusters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "CPU Core Frequenze", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Carico: ${telemetry.cpuUsagePercent}%",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            CpuClusterRow("Prime Core (Cortex-X / Oryon)", "${telemetry.primeCoreFreqGhz} GHz", 0.85f)
            CpuClusterRow("Gold Cores (Performance)", "${telemetry.midCoresFreqGhz} GHz", 0.65f)
            CpuClusterRow("Silver Cores (Efficiency)", "${telemetry.littleCoresFreqGhz} GHz", 0.40f)

            HorizontalDivider(color = AppBorderSubtle, modifier = Modifier.padding(top = 10.dp))
        }

        // 3. GPU & Memory
        item {
            Text(text = "GPU & Memoria", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryStatColumn("GPU Clock", "${telemetry.gpuClockMhz} MHz", TextPrimary)
                TelemetryStatColumn("Carico GPU", "${telemetry.gpuUsagePercent}%", TextPrimary)
                TelemetryStatColumn("zRAM Swap", "${telemetry.zRamUsageMb} MB", TextPrimary)
            }

            Column(modifier = Modifier.padding(top = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "RAM Sistema", color = TextMuted, fontSize = 12.sp)
                    Text(
                        text = "${telemetry.ramUsedMb} / ${telemetry.ramTotalMb} MB",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (telemetry.ramUsedMb.toFloat() / telemetry.ramTotalMb).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = AccentBlue,
                    trackColor = AppSurfaceElevated
                )
            }

            HorizontalDivider(color = AppBorderSubtle, modifier = Modifier.padding(top = 16.dp))
        }

        // 4. Thermal Throttling Monitor
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Stato Termico ADPF", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Temperatura Batteria: ${telemetry.batteryTempC}°C",
                        color = if (telemetry.batteryTempC > 40f) StatusWarning else TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Text(
                    text = telemetry.thermalLevel.label,
                    color = if (telemetry.thermalLevel == ThermalLevel.NONE) StatusActive else StatusWarning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TelemetryStatColumn(label: String, value: String, valueColor: Color) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun CpuClusterRow(name: String, freq: String, ratio: Float) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = TextSecondary, fontSize = 12.sp)
            Text(text = freq, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp)),
            color = AccentBlue,
            trackColor = AppSurfaceElevated
        )
    }
}
