package ru.mesh.expressive.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 7-Sided Scalloped Cookie Shape (Signature Material Design 3 Expressive geometry)
 */
class M3Cookie7Shape(private val points: Int = 7) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerRadius = minOf(cx, cy)
        val innerRadius = outerRadius * 0.82f

        val step = (2.0 * Math.PI) / (points * 2)
        var angle = -Math.PI / 2.0

        for (i in 0 until (points * 2)) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val prevAngle = angle - step
                val prevR = if ((i - 1) % 2 == 0) outerRadius else innerRadius
                val prevX = (cx + prevR * cos(prevAngle)).toFloat()
                val prevY = (cy + prevR * sin(prevAngle)).toFloat()

                val midAngle = angle - step / 2.0
                val cpR = (outerRadius + innerRadius) / 2f * 1.08f
                val cpx = (cx + cpR * cos(midAngle)).toFloat()
                val cpy = (cy + cpR * sin(midAngle)).toFloat()

                path.quadraticBezierTo(cpx, cpy, x, y)
            }
            angle += step
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
    topStart = 36.dp,
    topEnd = 20.dp,
    bottomEnd = 36.dp,
    bottomStart = 20.dp
)

val PillShape = RoundedCornerShape(percent = 50)
val ExpressivePillButtonShape = RoundedCornerShape(24.dp)
val ExpressiveBadgeShape = RoundedCornerShape(12.dp)
