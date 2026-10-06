package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Disciplined, low-glare engineering surfaces (SteamOS / Linux terminal inspired)
val AppBackground = Color(0xFF0D0F12)
val AppSurface = Color(0xFF14171D)
val AppSurfaceElevated = Color(0xFF1C2028)
val AppBorder = Color(0xFF262B35)
val AppBorderSubtle = Color(0xFF1B1F27)

// Typographic tokens
val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

// Single owned functional accent
val AccentBlue = Color(0xFF3B82F6)
val AccentBlueSubtle = Color(0x1F3B82F6)

// Functional semantic tokens
val StatusActive = Color(0xFF10B981)
val StatusWarning = Color(0xFFF59E0B)
val StatusDanger = Color(0xFFEF4444)
val StatusSteam = Color(0xFF38BDF8)
val StatusEpic = Color(0xFFA855F7)

// Legacy aliases for backward compatibility if needed
val CyanNeon = AccentBlue
val ElectricViolet = AccentBlue
val NeonGreen = StatusActive
val AmberWarning = StatusWarning
val CrimsonAlert = StatusDanger
val DarkBackground = AppBackground
val DarkSurface = AppSurface
val DarkSurfaceVariant = AppSurfaceElevated
val DarkSurfaceCard = AppSurface
val DarkBorder = AppBorder
val SnapDragonRed = StatusDanger
val AdrenoGold = StatusWarning
val MesaTurnipCyan = AccentBlue
