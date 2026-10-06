package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DefaultStoreClients
import com.example.model.GameEntry
import com.example.ui.theme.*

@Composable
fun SteamTroubleshootDialog(
    onDismiss: () -> Unit,
    onLaunchFastSteam: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Perché Steam non si avvia?",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Diagnostica e soluzioni per Android (Wine + Box64)",
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
                Text(
                    text = "1. La causa del blocco (steamwebhelper.exe):",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Il client desktop di Steam integra un browser Chromium a 6 processi con sandbox GPU. Su Android, Chromium tenta di creare Job Objects di Windows e socket IPC che mandano Wine in un loop infinito (schermo nero o caricamento perenne).",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                HorizontalDivider(color = AppBorderSubtle)

                Text(
                    text = "Soluzione 1: Modalità No-Browser (Consigliata)",
                    color = StatusSteam,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Disabilita steamwebhelper e avvia direttamente l'interfaccia Win32 leggera (Small Mode):",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AppSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "steam.exe -no-browser -no-cef-sandbox +open steam://open/minigameslist",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                HorizontalDivider(color = AppBorderSubtle)

                Text(
                    text = "Soluzione 2: Avvia direttamente i giochi!",
                    color = StatusActive,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Non hai bisogno di aprire l'interfaccia pesante di Steam per giocare. Copia la cartella del tuo gioco sul telefono e aggiungi il file .exe dalla Home: CracrabracBox intercetta steam_api64.dll con il ponte Goldberg e avvia il gioco in 5 secondi senza consumare 2GB di RAM per il client Steam.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onLaunchFastSteam()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("launch_fast_steam_btn")
            ) {
                Text("Avvia Steam No-Browser", color = TextPrimary, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi", color = TextSecondary, fontSize = 12.sp)
            }
        },
        containerColor = AppSurface,
        shape = RoundedCornerShape(12.dp)
    )
}
