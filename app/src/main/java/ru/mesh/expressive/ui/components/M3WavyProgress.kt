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
    strokeWidth: Dp = 6.dp,
    amplitude: Float = 6f,
    wavelength: Float = 48f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WavyProgress")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "animatedProgress"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val strokePx = strokeWidth.toPx()

        // Draw track wavy line
        val trackPath = Path().apply {
            moveTo(0f, midY)
            var x = 0f
            while (x <= width) {
                val y = midY + amplitude * sin((x / wavelength) * 2 * PI.toFloat())
                lineTo(x, y)
                x += 4f
            }
        }
        drawPath(
            path = trackPath,
            color = trackColor,
            style = Stroke(width = strokePx * 0.75f, cap = StrokeCap.Round)
        )

        // Draw active wavy snake indicator
        val activeWidth = width * animatedProgress
        if (activeWidth > 2f) {
            val activePath = Path().apply {
                moveTo(0f, midY)
                var x = 0f
                while (x <= activeWidth) {
                    val y = midY + amplitude * sin((x / wavelength) * 2 * PI.toFloat() + phase)
                    lineTo(x, y)
                    x += 3f
                }
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
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val morph by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
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
        val r = (minOf(cx, cy) - 6.dp.toPx()) * morph

        // Scalloped wavy path around circle
        val path = Path()
        val numLobes = 7
        val lobeDepth = 4.dp.toPx()

        for (i in 0..360 step 5) {
            val rad = i * PI.toFloat() / 180f
            val currentR = r + lobeDepth * sin(numLobes * rad)
            val px = cx + currentR * cos(rad)
            val py = cy + currentR * sin(rad)

            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Expressive Tactile Spring Bounce Click Modifier
 */
fun Modifier.expressiveBounceClick(
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
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
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}
