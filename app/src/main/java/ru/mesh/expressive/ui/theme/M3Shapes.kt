package ru.mesh.expressive.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Perfectly symmetrical Scalloped Cookie Shape (Signature Material Design 3 Expressive geometry)
 */
class M3Cookie7Shape(private val points: Int = 8) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = minOf(cx, cy)
        val amp = maxR * 0.085f
        val baseR = maxR - amp

        val totalSteps = 120
        val dTheta = (2.0 * Math.PI) / totalSteps
        val startTheta = -Math.PI / 2.0

        for (step in 0 until totalSteps) {
            val theta = startTheta + step * dTheta
            val r = (baseR + amp * cos(points * (theta - startTheta))).toFloat()
            val x = cx + r * cos(theta).toFloat()
            val y = cy + r * sin(theta).toFloat()
            if (step == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

val ExpressiveCardShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 16.dp,
    bottomEnd = 28.dp,
    bottomStart = 16.dp
)

val ExpressiveHeroShape = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 20.dp,
    bottomEnd = 32.dp,
    bottomStart = 20.dp
)

val PillShape = RoundedCornerShape(percent = 50)
val ExpressivePillButtonShape = RoundedCornerShape(24.dp)
val ExpressiveBadgeShape = RoundedCornerShape(12.dp)
