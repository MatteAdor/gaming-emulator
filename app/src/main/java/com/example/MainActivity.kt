package com.example

import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ControllerManager
import com.example.core.NativeBridge
import com.example.core.PerformanceManager
import com.example.model.*
import com.example.ui.components.AddGameDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LIBRARY("Libreria", Icons.Default.SportsEsports),
    CONTAINER("Container", Icons.Default.Tune),
    TELEMETRY("Telemetria", Icons.Default.Speed),
    WORKBENCH("Architettura NDK", Icons.Default.Code)
}

class MainActivity : ComponentActivity() {

    private lateinit var performanceManager: PerformanceManager
    private lateinit var controllerManager: ControllerManager
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Unlock 120Hz display refresh rate for Snapdragon / Galaxy S24 Ultra
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                val modes = display?.supportedModes ?: emptyArray()
                val mode120 = modes.firstOrNull { it.refreshRate >= 119f }
                if (mode120 != null) {
                    val lp = window.attributes
                    lp.preferredDisplayModeId = mode120.modeId
                    window.attributes = lp
                }
            } catch (_: Exception) {}
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                val lp = window.attributes
                lp.preferredRefreshRate = 120f
                window.attributes = lp
            } catch (_: Exception) {}
        }

        performanceManager = PerformanceManager(this)
        controllerManager = ControllerManager(this)
        controllerManager.start()

        setContent {
            MyApplicationTheme {
                MainAppRoot(
                    performanceManager = performanceManager,
                    controllerManager = controllerManager,
                    gameViewModel = gameViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerManager.stop()
        performanceManager.stopMonitoring()
        if (isFinishing) {
            NativeBridge.stopSession()
        }
    }

    // Forward physical gamepad button presses to Native Core
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val source = event.source
        if ((source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) ||
            (source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)) {
            val eventType = if (event.action == KeyEvent.ACTION_DOWN) 1 else 0
            NativeBridge.dispatchInput(eventType, event.keyCode)
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    // Forward physical joystick & trigger axes to Native Core
    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        val source = event.source
        if ((source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) ||
            (source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD)) {
            val axisX = event.getAxisValue(MotionEvent.AXIS_X)
            val axisY = event.getAxisValue(MotionEvent.AXIS_Y)
            NativeBridge.dispatchInput(2, 0, axisX, axisY)

            val axisZ = event.getAxisValue(MotionEvent.AXIS_Z)
            val axisRZ = event.getAxisValue(MotionEvent.AXIS_RZ)
            if (axisZ != 0f || axisRZ != 0f) {
                NativeBridge.dispatchInput(2, 1, axisZ, axisRZ)
            }
            return true
        }
        return super.dispatchGenericMotionEvent(event)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppRoot(
    performanceManager: PerformanceManager,
    controllerManager: ControllerManager,
    gameViewModel: GameViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        performanceManager.startMonitoring(coroutineScope)
    }

    val telemetry by performanceManager.telemetry.collectAsStateWithLifecycle()
    val isControllerConnected by controllerManager.isControllerConnected.collectAsStateWithLifecycle()
    val connectedControllerName by controllerManager.connectedControllerName.collectAsStateWithLifecycle()
    val socInfo = remember { performanceManager.getDeviceSocInfo() }

    val games by gameViewModel.games.collectAsStateWithLifecycle()
    val selectedTab by gameViewModel.selectedTab.collectAsStateWithLifecycle()
    val containerProfile by gameViewModel.containerProfile.collectAsStateWithLifecycle()
    val activeSessionPid by gameViewModel.activeSessionPid.collectAsStateWithLifecycle()
    val activeRunningGame by gameViewModel.activeRunningGame.collectAsStateWithLifecycle()

    var showAddGameDialog by rememberSaveable { mutableStateOf(false) }

    fun launchGame(game: GameEntry) {
        val target = if (game.targetFps > 0) game.targetFps else containerProfile.targetFps
        performanceManager.setTargetFps(target)
        val pid = gameViewModel.launchGame(game)
        Toast.makeText(context, "Avvio ${game.title} (PID: $pid • ${if (target == 0) "Sbloccato" else "${target}Hz"})", Toast.LENGTH_SHORT).show()
    }

    fun stopSession() {
        gameViewModel.stopSession()
        Toast.makeText(context, "Sessione terminata", Toast.LENGTH_SHORT).show()
    }

    // Full Screen Game Viewport when running
    if (activeRunningGame != null && activeSessionPid != null) {
        GameRunningSessionScreen(
            game = activeRunningGame!!,
            sessionPid = activeSessionPid!!,
            telemetry = telemetry,
            isPhysicalControllerConnected = isControllerConnected,
            connectedControllerName = connectedControllerName,
            onFpsTargetChange = { newFps ->
                performanceManager.setTargetFps(newFps)
            },
            onStopSession = { stopSession() }
        )
        return
    }

    if (selectedTab != AppTab.LIBRARY) {
        BackHandler {
            gameViewModel.setSelectedTab(AppTab.LIBRARY)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "CracrabracBox",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                            if (isControllerConnected) {
                                Text(
                                    text = "• Gamepad collegato",
                                    color = StatusActive,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            text = if (activeSessionPid != null) "Sessione attiva • PID $activeSessionPid" else "Qualcomm Snapdragon ARM64",
                            color = if (activeSessionPid != null) StatusActive else TextMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    if (activeSessionPid != null) {
                        TextButton(
                            onClick = { stopSession() },
                            colors = ButtonDefaults.textButtonColors(contentColor = StatusDanger),
                            modifier = Modifier.testTag("topbar_stop_session")
                        ) {
                            Text("Arresta", fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AppSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { gameViewModel.setSelectedTab(tab) },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) AccentBlue else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) TextPrimary else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AccentBlueSubtle
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name}")
                    )
                }
            }
        },
        containerColor = AppBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.LIBRARY -> DashboardScreen(
                    containerProfile = containerProfile,
                    telemetry = telemetry,
                    games = games,
                    activeSessionPid = activeSessionPid,
                    onLaunchGame = { launchGame(it) },
                    onStopSession = { stopSession() },
                    onDeleteGame = { gameToRemove ->
                        gameViewModel.removeGame(gameToRemove)
                        Toast.makeText(context, "Rimosso ${gameToRemove.title}", Toast.LENGTH_SHORT).show()
                    },
                    onOpenConfig = { gameViewModel.setSelectedTab(AppTab.CONTAINER) },
                    onAddGame = { showAddGameDialog = true }
                )

                AppTab.CONTAINER -> ContainerConfigScreen(
                    profile = containerProfile,
                    onProfileChanged = { gameViewModel.updateContainerProfile(it) },
                    onResetPrefix = {
                        Toast.makeText(context, "Wineprefix ripristinato", Toast.LENGTH_SHORT).show()
                    }
                )

                AppTab.TELEMETRY -> TelemetryHubScreen(
                    telemetry = telemetry,
                    socInfo = socInfo
                )

                AppTab.WORKBENCH -> WorkbenchScreen()
            }

            if (showAddGameDialog) {
                AddGameDialog(
                    onDismiss = { showAddGameDialog = false },
                    onAdd = { newGame ->
                        gameViewModel.addGame(newGame)
                        showAddGameDialog = false
                    }
                )
            }
        }
    }
}
