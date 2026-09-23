package ru.mesh.expressive.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import kotlinx.coroutines.launch
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
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex = items.indexOf(selectedItem).coerceIn(0, (items.size - 1).coerceAtLeast(0))

    // Map of item index to Pair(leftPx, widthPx)
    val itemBounds = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }
    var containerHeightPx by remember { mutableFloatStateOf(0f) }

    val currentBounds = itemBounds[selectedIndex]
    val targetLeft = currentBounds?.first ?: 0f
    val targetWidth = currentBounds?.second ?: 0f

    val animLeft = remember { Animatable(targetLeft) }
    val animWidth = remember { Animatable(targetWidth) }
    var isInitialized by remember { mutableStateOf(false) }

    // Interactive dragging state
    var isDragging by remember { mutableStateOf(false) }
    var dragLeftPx by remember { mutableFloatStateOf(0f) }
    var hoveredIndex by remember { mutableIntStateOf(selectedIndex) }

    LaunchedEffect(targetLeft, targetWidth) {
        if (targetWidth > 0f) {
            if (!isInitialized) {
                animLeft.snapTo(targetLeft)
                animWidth.snapTo(targetWidth)
                isInitialized = true
            } else if (!isDragging) {
                launch {
                    animLeft.animateTo(
                        targetValue = targetLeft,
                        animationSpec = spring(dampingRatio = 0.78f, stiffness = 450f)
                    )
                }
                launch {
                    animWidth.animateTo(
                        targetValue = targetWidth,
                        animationSpec = spring(dampingRatio = 0.78f, stiffness = 450f)
                    )
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(items, selectedIndex) {
                detectHorizontalDragGestures(
                    onDragStart = { _ ->
                        isDragging = true
                        dragLeftPx = animLeft.value
                        hoveredIndex = selectedIndex
                    },
                    onDragEnd = {
                        val finalIndex = hoveredIndex
                        isDragging = false
                        if (finalIndex != selectedIndex && finalIndex in items.indices) {
                            coroutineScope.launch {
                                animLeft.snapTo(dragLeftPx)
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemSelected(items[finalIndex])
                        } else {
                            coroutineScope.launch {
                                animLeft.animateTo(
                                    targetValue = targetLeft,
                                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 450f)
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        coroutineScope.launch {
                            animLeft.animateTo(
                                targetValue = targetLeft,
                                animationSpec = spring(dampingRatio = 0.78f, stiffness = 450f)
                            )
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val minLeft = itemBounds[0]?.first ?: 0f
                        val maxLeft = itemBounds[items.size - 1]?.first ?: 0f
                        dragLeftPx = (dragLeftPx + dragAmount).coerceIn(minLeft, maxLeft)

                        val curW = animWidth.value
                        val center = dragLeftPx + curW / 2f
                        val closest = itemBounds.minByOrNull { (_, b) ->
                            val itemCenter = b.first + b.second / 2f
                            kotlin.math.abs(center - itemCenter)
                        }?.key ?: selectedIndex

                        if (closest != hoveredIndex && closest in items.indices) {
                            hoveredIndex = closest
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val targetW = itemBounds[closest]?.second ?: targetWidth
                            coroutineScope.launch {
                                animWidth.animateTo(
                                    targetValue = targetW,
                                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 450f)
                                )
                            }
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { containerHeightPx = it.size.height.toFloat() }
                .padding(3.dp)
        ) {
            val displayLeft = if (isDragging) dragLeftPx else animLeft.value
            val activeVisualIndex = if (isDragging) hoveredIndex else selectedIndex

            // Smooth sliding chip indicator (measured to fit exact text width!)
            if (animWidth.value > 0f) {
                val hDp = if (containerHeightPx > 0f) density.run { (containerHeightPx - 6.dp.toPx()).toDp() } else 32.dp
                Box(
                    modifier = Modifier
                        .offset { IntOffset(displayLeft.roundToInt(), 0) }
                        .size(
                            width = density.run { animWidth.value.toDp() },
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
                    val isSelected = index == activeVisualIndex
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
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                if (selectedIndex != index) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onItemSelected(item)
                                }
                            }
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
