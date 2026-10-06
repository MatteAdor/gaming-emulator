package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun ContainerConfigScreen(
    profile: ContainerProfile,
    onProfileChanged: (ContainerProfile) -> Unit,
    onResetPrefix: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Store DRM & Client Integration
        item {
            ConfigSectionHeader("Integrazione Store")

            ConfigSwitchRow(
                title = "Emulatore Steamworks (Goldberg)",
                description = "Intercetta steam_api64.dll e genera steam_appid.txt offline per bypassare il client Steam.",
                checked = profile.enableSteamClientStub,
                onCheckedChange = { onProfileChanged(profile.copy(enableSteamClientStub = it)) }
            )

            HorizontalDivider(color = AppBorderSubtle)

            ConfigSwitchRow(
                title = "Stub Epic Online Services (EOS)",
                description = "Inietta credenziali dummy offline ed esegue l'override di EOSSDK-Win64-Shipping.dll.",
                checked = profile.enableEpicEosStub,
                onCheckedChange = { onProfileChanged(profile.copy(enableEpicEosStub = it)) }
            )
        }

        // Section: Graphics Drivers
        item {
            ConfigSectionHeader("Driver Grafico Adreno")
            Text(
                text = "Mesa Turnip accede direttamente a /dev/kgsl-3d0 bypassando il driver Vulkan stock.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            GraphicsDriver.values().forEach { driver ->
                val isSelected = profile.graphicsDriver == driver
                ConfigRadioRow(
                    title = driver.label,
                    description = driver.version,
                    selected = isSelected,
                    onClick = { onProfileChanged(profile.copy(graphicsDriver = driver)) },
                    testTag = "driver_option_${driver.name}"
                )
            }
        }

        // Section: Box64 Dynarec Preset
        item {
            ConfigSectionHeader("Traduzione Box64 JIT")

            DynarecPreset.values().forEach { preset ->
                val isSelected = profile.dynarecPreset == preset
                ConfigRadioRow(
                    title = preset.label,
                    description = preset.description,
                    selected = isSelected,
                    onClick = { onProfileChanged(profile.copy(dynarecPreset = preset)) },
                    testTag = "dynarec_option_${preset.name}"
                )
            }
        }

        // Section: DXVK / D3D Translation
        item {
            ConfigSectionHeader("Traduzione Direct3D (DXVK)")

            DxvkVersion.values().forEach { ver ->
                val isSelected = profile.dxvkVersion == ver
                ConfigRadioRow(
                    title = ver.label,
                    description = ver.description,
                    selected = isSelected,
                    onClick = { onProfileChanged(profile.copy(dxvkVersion = ver)) },
                    testTag = "dxvk_option_${ver.name}"
                )
            }
        }

        // Section: FPS Target & Refresh Rate (Snapdragon 120Hz)
        item {
            ConfigSectionHeader("Frequenza di Aggiornamento & Limite FPS")
            Text(
                text = "Configura il refresh rate per il display a 120Hz (Snapdragon 8 Gen 3 / Galaxy S24 Ultra).",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FpsTargetMode.values().forEach { mode ->
                val isSelected = profile.targetFps == mode.fps
                ConfigRadioRow(
                    title = mode.label,
                    description = mode.description,
                    selected = isSelected,
                    onClick = { onProfileChanged(profile.copy(targetFps = mode.fps)) },
                    testTag = "fps_option_${mode.name}"
                )
            }

            HorizontalDivider(color = AppBorderSubtle, modifier = Modifier.padding(top = 8.dp))

            ConfigSwitchRow(
                title = "Bypass VSync (Present Mode Mailbox)",
                description = "Disabilita il blocco FIFO a 60Hz per consentire la presentazione dei frame a 120Hz con input lag ridotto.",
                checked = profile.bypassVsync,
                onCheckedChange = { onProfileChanged(profile.copy(bypassVsync = it)) }
            )
        }

        // Section: Kernel & Synchronization Toggles
        item {
            ConfigSectionHeader("Sincronizzazione & Kernel")

            ConfigSwitchRow(
                title = "Wine ESYNC (eventfd)",
                description = "Sincronizzazione IPC rapida basata su descrittori eventfd invece del wineserver.",
                checked = profile.enableEsync,
                onCheckedChange = { onProfileChanged(profile.copy(enableEsync = it)) }
            )

            HorizontalDivider(color = AppBorderSubtle)

            ConfigSwitchRow(
                title = "Box64 FastNaN Mode",
                description = "Elaborazione floating point IEEE-754 a bassa latenza.",
                checked = profile.fastNan,
                onCheckedChange = { onProfileChanged(profile.copy(fastNan = it)) }
            )
        }

        // Reset Action
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onResetPrefix,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_prefix_btn")
            ) {
                Text("Ripristina Wineprefix predefinito", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ConfigSectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ConfigSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = description, color = TextMuted, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = AccentBlue,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = AppSurfaceElevated
            )
        )
    }
}

@Composable
private fun ConfigRadioRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                color = if (selected) TextPrimary else TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AccentBlue,
                unselectedColor = TextMuted
            )
        )
    }
}
