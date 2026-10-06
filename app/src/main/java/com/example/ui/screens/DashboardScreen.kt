package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun DashboardScreen(
    containerProfile: ContainerProfile,
    telemetry: TelemetryData,
    games: List<GameEntry>,
    activeSessionPid: Int?,
    onLaunchGame: (GameEntry) -> Unit,
    onStopSession: () -> Unit,
    onDeleteGame: (GameEntry) -> Unit,
    onOpenConfig: () -> Unit,
    onAddGame: () -> Unit
) {
    var showSteamTroubleshoot by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        // 1. Direct Engine Status & Diagnostics
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (activeSessionPid != null) StatusActive else TextMuted)
                    )
                    Text(
                        text = if (activeSessionPid != null) "Sessione attiva (PID $activeSessionPid)" else "Container pronto",
                        color = if (activeSessionPid != null) StatusActive else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = "•", color = TextMuted, fontSize = 12.sp)
                    Text(
                        text = containerProfile.graphicsDriver.label.substringBefore(" ("),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                if (activeSessionPid != null) {
                    TextButton(
                        onClick = onStopSession,
                        colors = ButtonDefaults.textButtonColors(contentColor = StatusDanger),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("terminate_session_btn")
                    ) {
                        Text("Arresta", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    TextButton(
                        onClick = onOpenConfig,
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("quick_config_btn")
                    ) {
                        Text("Configura", fontSize = 12.sp)
                    }
                }
            }

            HorizontalDivider(color = AppBorderSubtle)
        }

        // 2. Telemetry Inline Data Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryMetricItem("FPS", "${telemetry.fps.toInt()}", if (telemetry.fps > 45f) StatusActive else StatusWarning)
                TelemetryMetricItem("Frame Time", "${telemetry.frameTimeMs} ms", TextPrimary)
                TelemetryMetricItem("GPU", "${telemetry.gpuClockMhz} MHz", TextPrimary)
                TelemetryMetricItem("Batteria", "${telemetry.batteryTempC}°C", if (telemetry.batteryTempC > 40f) StatusWarning else TextPrimary)
            }

            HorizontalDivider(color = AppBorderSubtle)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. STORE & CLIENT PREINSTALLATI (Steam & Epic Games)
        item {
            Text(
                text = "Store e Client Ufficiali",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Steam Client Entry
            StoreClientRow(
                title = "Steam",
                description = "Scarica, sincronizza ed esegui i tuoi giochi Steam ufficiali",
                badgeText = "Client Ufficiale",
                badgeColor = StatusSteam,
                onOpen = { onLaunchGame(DefaultStoreClients.SteamClient) },
                onTroubleshoot = { showSteamTroubleshoot = true },
                testTag = "open_steam_client_btn"
            )

            HorizontalDivider(color = AppBorderSubtle)

            // Epic Games Store Entry
            StoreClientRow(
                title = "Epic Games Store",
                description = "Accedi al catalogo Epic Games, scarica e gioca con supporto EOS",
                badgeText = "Client Ufficiale",
                badgeColor = StatusEpic,
                onOpen = { onLaunchGame(DefaultStoreClients.EpicGamesClient) },
                testTag = "open_epic_client_btn"
            )

            HorizontalDivider(color = AppBorderSubtle)

            // Inter-Process Bridge Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SyncAlt, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                Text(
                    text = "Ponte Steamworks & EOS condiviso: i giochi esterni possono comunicare con Steam e usare le sue API.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Section Header: Library Tools
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "I tuoi giochi (.exe)",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${games.size} giochi aggiunti manualmente",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onAddGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_game_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aggiungi .exe", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // 5. Content Model: Clean vertical list or genuine empty state
        if (games.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nessun gioco esterno aggiunto",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Puoi sfogliare la memoria del telefono per selezionare le cartelle dei tuoi giochi PC. Tutti i file accessori (.dll, asset, cartelle dati) vengono preservati e configurati nella working directory per Wine.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth(0.92f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onAddGame,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        modifier = Modifier.testTag("empty_add_game_btn")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sfoglia cartelle o .exe sul telefono", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        } else {
            items(games, key = { it.id }) { game ->
                CleanGameRow(
                    game = game,
                    onLaunch = { onLaunchGame(game) },
                    onDelete = { onDeleteGame(game) }
                )
                HorizontalDivider(color = AppBorderSubtle)
            }
        }
    }

    if (showSteamTroubleshoot) {
        com.example.ui.components.SteamTroubleshootDialog(
            onDismiss = { showSteamTroubleshoot = false },
            onLaunchFastSteam = {
                onLaunchGame(DefaultStoreClients.SteamClient)
            }
        )
    }
}

@Composable
private fun StoreClientRow(
    title: String,
    description: String,
    badgeText: String,
    badgeColor: Color,
    onOpen: () -> Unit,
    onTroubleshoot: (() -> Unit)? = null,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• $badgeText",
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (onTroubleshoot != null) {
                TextButton(
                    onClick = onTroubleshoot,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Guida", fontSize = 11.sp, color = TextMuted)
                }
            }

            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppSurfaceElevated,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag(testTag)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = badgeColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Entra", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun TelemetryMetricItem(label: String, value: String, valueColor: Color) {
    Column {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun CleanGameRow(
    game: GameEntry,
    onLaunch: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("game_card_${game.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = game.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 3.dp)
            ) {
                Text(
                    text = game.exeName,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                if (game.folderFileCount > 1) {
                    Text(text = "•", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "${game.folderFileCount} file nel folder",
                        color = StatusActive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (game.storeType != GameStoreType.STANDALONE) {
                    Text(text = "•", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = when (game.storeType) {
                            GameStoreType.STEAM -> if (game.steamAppId.isNotBlank()) "Steam (ID ${game.steamAppId})" else "Steam"
                            GameStoreType.EPIC_GAMES -> "Epic EOS"
                            else -> ""
                        },
                        color = when (game.storeType) {
                            GameStoreType.STEAM -> StatusSteam
                            GameStoreType.EPIC_GAMES -> StatusEpic
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (game.linkSharedSteamIpc && game.storeType != GameStoreType.STEAM) {
                    Text(text = "•", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "Ponte Steam",
                        color = StatusSteam,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (game.gameDirectory.isNotBlank()) {
                Text(
                    text = "📁 ${game.gameDirectory}",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp).testTag("delete_game_${game.id}")
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Rimuovi",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("launch_game_${game.id}")
            ) {
                Text("Avvia", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
