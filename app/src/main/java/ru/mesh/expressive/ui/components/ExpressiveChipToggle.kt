package ru.mesh.expressive.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun <T> ExpressiveChipSegmentedToggle(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String,
    icon: ((T) -> ImageVector)? = null
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val selectedIndex = items.indexOf(selectedItem).coerceIn(0, (items.size - 1).coerceAtLeast(0))

    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val onItemSelectedState by rememberUpdatedState(onItemSelected)

    // Map of item index to Pair(leftPx, widthPx)
    val itemBounds = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }
    var containerHeightPx by remember { mutableFloatStateOf(0f) }

    val currentBounds = itemBounds[selectedIndex]
    val targetLeft = currentBounds?.first ?: 0f
    val targetWidth = currentBounds?.second ?: 0f

    val animLeft by animateFloatAsState(
        targetValue = targetLeft,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 450f),
        label = "chipLeft"
    )
    val animWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 450f),
        label = "chipWidth"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downX = down.position.x
                        var hasMoved = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                // Finger lifted: if it was a tap (not dragged)
                                if (!hasMoved) {
                                    val tappedIndex = itemBounds.entries.firstOrNull { (_, b) ->
                                        downX >= b.first && downX <= b.first + b.second
                                    }?.key
                                    if (tappedIndex != null && tappedIndex in items.indices && tappedIndex != currentSelectedIndex) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onItemSelectedState(items[tappedIndex])
                                    }
                                }
                                break
                            }

                            val currentX = change.position.x
                            val totalDeltaFromStart = currentX - downX

                            if (kotlin.math.abs(totalDeltaFromStart.toDouble()) > 10.0) {
                                hasMoved = true
                                change.consume()

                                // Find which item index is closest to current finger X
                                val closestIndex = itemBounds.minByOrNull { (_, b) ->
                                    val itemCenter = b.first + b.second / 2f
                                    kotlin.math.abs((currentX - itemCenter).toDouble())
                                }?.key

                                if (closestIndex != null && closestIndex != currentSelectedIndex && closestIndex in items.indices) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onItemSelectedState(items[closestIndex])
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { containerHeightPx = it.size.height.toFloat() }
                .padding(3.dp)
        ) {
            // Smooth sliding chip indicator (measured to fit exact text width!)
            if (animWidth > 0f) {
                val hDp = if (containerHeightPx > 0f) density.run { (containerHeightPx - 6.dp.toPx()).toDp() } else 32.dp
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animLeft.roundToInt(), 0) }
                        .size(
                            width = density.run { animWidth.toDp() },
                            height = hDp
                        )
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            // Options Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = item == selectedItem
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                        label = "chipTextColor"
                    )

                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                val x = coords.positionInParent().x
                                val w = coords.size.width.toFloat()
                                if (itemBounds[index]?.first != x || itemBounds[index]?.second != w) {
                                    itemBounds[index] = Pair(x, w)
                                }
                            }
                            .clip(RoundedCornerShape(9.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (icon != null) {
                                val itemIcon = icon(item)
                                Icon(
                                    imageVector = itemIcon,
                                    contentDescription = null,
                                    tint = textColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = label(item),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
