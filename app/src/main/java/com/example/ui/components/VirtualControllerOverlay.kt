package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.NativeBridge
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualControllerOverlay(
    modifier: Modifier = Modifier,
    alpha: Float = 0.8f,
    onButtonPress: (String) -> Unit = {},
    onJoystickMove: ((Int, Float, Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                ?: context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerHaptic(durationMs: Long = 15) {
        try {
            if (vibrator is Vibrator) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .padding(12.dp)
    ) {
        // TOP ROW: Bumpers & Triggers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TriggerButton(label = "L2") {
                    triggerHaptic(20)
                    NativeBridge.dispatchInput(1, 102)
                    onButtonPress("L2")
                }
                TriggerButton(label = "L1") {
                    triggerHaptic(12)
                    NativeBridge.dispatchInput(1, 100)
                    onButtonPress("L1")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillButton(label = "SEL") {
                    triggerHaptic(12)
                    NativeBridge.dispatchInput(1, 109)
                    onButtonPress("SELECT")
                }
                PillButton(label = "START") {
                    triggerHaptic(12)
                    NativeBridge.dispatchInput(1, 108)
                    onButtonPress("START")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TriggerButton(label = "R1") {
                    triggerHaptic(12)
                    NativeBridge.dispatchInput(1, 101)
                    onButtonPress("R1")
                }
                TriggerButton(label = "R2") {
                    triggerHaptic(20)
                    NativeBridge.dispatchInput(1, 103)
                    onButtonPress("R2")
                }
            }
        }

        // BOTTOM ROW: Sticks & Face Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VirtualThumbstick(
                    label = "L",
                    onVectorChanged = { x, y ->
                        NativeBridge.dispatchInput(2, 0, x, y)
                        onJoystickMove?.invoke(0, x, y)
                    }
                )
                VirtualDPad(
                    onDirection = { dir ->
                        triggerHaptic(10)
                        NativeBridge.dispatchInput(1, dir)
                        onButtonPress("DPAD_$dir")
                    }
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActionDiamond(
                    onPress = { btn, code ->
                        triggerHaptic(15)
                        NativeBridge.dispatchInput(1, code)
                        onButtonPress(btn)
                    }
                )
                VirtualThumbstick(
                    label = "R",
                    onVectorChanged = { x, y ->
                        NativeBridge.dispatchInput(2, 1, x, y)
                        onJoystickMove?.invoke(1, x, y)
                    }
                )
            }
        }
    }
}

@Composable
fun VirtualThumbstick(
    label: String,
    onVectorChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var stickOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 42f

    Box(
        modifier = modifier
            .size(105.dp)
            .clip(CircleShape)
            .background(Color(0x551E222B))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        stickOffset = Offset.Zero
                        onVectorChanged(0f, 0f)
                    },
                    onDragCancel = {
                        stickOffset = Offset.Zero
                        onVectorChanged(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = stickOffset + dragAmount
                        val distance = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        stickOffset = if (distance > maxRadius) {
                            val angle = kotlin.math.atan2(newOffset.y, newOffset.x)
                            Offset(cos(angle) * maxRadius, sin(angle) * maxRadius)
                        } else {
                            newOffset
                        }
                        val normalizedX = stickOffset.x / maxRadius
                        val normalizedY = stickOffset.y / maxRadius
                        onVectorChanged(normalizedX, normalizedY)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0x33FFFFFF),
                radius = size.width / 2.2f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }

        Box(
            modifier = Modifier
                .offset(x = (stickOffset.x / 2.5f).dp, y = (stickOffset.y / 2.5f).dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x992B313E)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun VirtualDPad(onDirection: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        DPadButton(Modifier.align(Alignment.TopCenter), "▲", 19) { onDirection(19) }
        DPadButton(Modifier.align(Alignment.BottomCenter), "▼", 20) { onDirection(20) }
        DPadButton(Modifier.align(Alignment.CenterStart), "◀", 21) { onDirection(21) }
        DPadButton(Modifier.align(Alignment.CenterEnd), "▶", 22) { onDirection(22) }

        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color(0x44262B35), CircleShape)
        )
    }
}

@Composable
fun DPadButton(
    modifier: Modifier,
    symbol: String,
    code: Int,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .size(32.dp)
            .testTag("dpad_btn_$code"),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0x771E222B),
            contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(text = symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActionDiamond(onPress: (String, Int) -> Unit) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        ActionCleanButton(Modifier.align(Alignment.TopCenter), "Y") { onPress("Y", 100) }
        ActionCleanButton(Modifier.align(Alignment.BottomCenter), "A") { onPress("A", 96) }
        ActionCleanButton(Modifier.align(Alignment.CenterStart), "X") { onPress("X", 99) }
        ActionCleanButton(Modifier.align(Alignment.CenterEnd), "B") { onPress("B", 97) }
    }
}

@Composable
fun ActionCleanButton(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .testTag("action_btn_$label"),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0x88262B35),
            contentColor = TextPrimary
        ),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
fun TriggerButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 32.dp)
            .testTag("trigger_btn_$label"),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0x771E222B),
            contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PillButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(minWidth = 54.dp, minHeight = 28.dp)
            .testTag("pill_btn_$label"),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0x551E222B),
            contentColor = TextSecondary
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}
