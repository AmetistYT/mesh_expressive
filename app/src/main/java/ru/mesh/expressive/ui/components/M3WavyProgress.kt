package ru.mesh.expressive.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Wavy / Snake Animated Progress Indicator (Material Design 3 Expressive)
 */
@Composable
fun M3WavyProgressIndicator(
    progress: Float, // 0.0f .. 1.0f
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 3.5.dp,
    amplitude: Float = 2.8f,
    wavelength: Float = 36f,
    isAnimated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WavyProgress")
    val phase by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 250f),
        label = "animatedProgress"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val strokePx = strokeWidth.toPx()
        val ampPx = (amplitude.dp.toPx()).coerceAtMost(midY - strokePx / 2f)
        val waveLenPx = wavelength.dp.toPx()

        fun waveY(x: Float): Float {
            return midY + ampPx * sin(2f * PI.toFloat() * (x / waveLenPx) - phase)
        }

        // Draw track wavy line with dense 1px steps for a perfectly smooth curve
        val trackPath = Path().apply {
            moveTo(0f, waveY(0f))
            var x = 1f
            while (x <= width) {
                lineTo(x, waveY(x))
                x += 1f
            }
            lineTo(width, waveY(width))
        }
        drawPath(
            path = trackPath,
            color = trackColor.copy(alpha = 0.45f),
            style = Stroke(width = strokePx * 0.75f, cap = StrokeCap.Round)
        )

        // Draw active wavy snake indicator
        val activeWidth = (width * animatedProgress).coerceAtLeast(0f)
        if (activeWidth > 1f) {
            val activePath = Path().apply {
                moveTo(0f, waveY(0f))
                var x = 1f
                while (x <= activeWidth) {
                    lineTo(x, waveY(x))
                    x += 1f
                }
                lineTo(activeWidth, waveY(activeWidth))
            }
            drawPath(
                path = activePath,
                color = color,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Circular Expressive Wavy Loader
 */
@Composable
fun M3CircularWavyLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    size: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CircularWavy")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val morph by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morph"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation }
    ) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val strokePx = 4.dp.toPx()
        val baseR = (minOf(cx, cy) - strokePx * 1.5f).coerceAtLeast(4f)
        val numLobes = 7
        val lobeDepth = 3.dp.toPx()

        // Background circular track
        drawCircle(
            color = trackColor.copy(alpha = 0.35f),
            radius = baseR,
            center = androidx.compose.ui.geometry.Offset(cx, cy),
            style = Stroke(width = strokePx * 0.5f, cap = StrokeCap.Round)
        )

        // Scalloped wavy path around circle
        val path = Path()
        for (i in 0..360 step 4) {
            val rad = i * PI.toFloat() / 180f
            val currentR = baseR + lobeDepth * sin(numLobes * rad + morph)
            val px = cx + currentR * cos(rad)
            val py = cy + currentR * sin(rad)

            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

val LocalSpringPhysicsEnabled = androidx.compose.runtime.compositionLocalOf { true }
val LocalHapticFeedbackEnabled = androidx.compose.runtime.compositionLocalOf { true }

/**
 * Expressive Tactile Spring Bounce Click Modifier
 */
fun Modifier.expressiveBounceClick(
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier = composed {
    val isSpringEnabled = LocalSpringPhysicsEnabled.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isSpringEnabled) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
        .then(
            if (isSpringEnabled) {
                Modifier.pointerInput(Unit) {
                    while (true) {
                        awaitPointerEventScope {
                            awaitFirstDown(requireUnconsumed = false)
                            isPressed = true
                            waitForUpOrCancellation()
                            isPressed = false
                        }
                    }
                }
            } else Modifier
        )
}
