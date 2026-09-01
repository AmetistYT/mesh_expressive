package ru.mesh.expressive.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mesh.expressive.ui.theme.PillShape

enum class ArrowDirection {
    TOP_LEFT, TOP_RIGHT, BOTTOM_CENTER
}

class SpeechBubbleShape(
    private val arrowDirection: ArrowDirection,
    private val arrowCenterXPx: Float = -1f,
    private val cornerRadius: Float = 48f,
    private val arrowWidth: Float = 36f,
    private val arrowHeight: Float = 24f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val width = size.width
        val height = size.height

        val computedArrowX = when {
            arrowCenterXPx > 0f -> arrowCenterXPx.coerceIn(cornerRadius + arrowWidth, width - cornerRadius - arrowWidth)
            arrowDirection == ArrowDirection.TOP_LEFT -> 48f
            arrowDirection == ArrowDirection.TOP_RIGHT -> width - 48f
            else -> width / 2f
        }

        when (arrowDirection) {
            ArrowDirection.TOP_LEFT, ArrowDirection.TOP_RIGHT -> {
                path.moveTo(cornerRadius, arrowHeight)
                path.lineTo(computedArrowX - arrowWidth / 2, arrowHeight)
                path.lineTo(computedArrowX, 0f)
                path.lineTo(computedArrowX + arrowWidth / 2, arrowHeight)
                path.lineTo(width - cornerRadius, arrowHeight)
                path.arcTo(
                    Rect(width - 2 * cornerRadius, arrowHeight, width, arrowHeight + 2 * cornerRadius),
                    270f, 90f, false
                )
                path.lineTo(width, height - cornerRadius)
                path.arcTo(
                    Rect(width - 2 * cornerRadius, height - 2 * cornerRadius, width, height),
                    0f, 90f, false
                )
                path.lineTo(cornerRadius, height)
                path.arcTo(
                    Rect(0f, height - 2 * cornerRadius, 2 * cornerRadius, height),
                    90f, 90f, false
                )
                path.lineTo(0f, arrowHeight + cornerRadius)
                path.arcTo(
                    Rect(0f, arrowHeight, 2 * cornerRadius, arrowHeight + 2 * cornerRadius),
                    180f, 90f, false
                )
                path.close()
            }
            ArrowDirection.BOTTOM_CENTER -> {
                val bodyHeight = height - arrowHeight
                path.moveTo(cornerRadius, 0f)
                path.lineTo(width - cornerRadius, 0f)
                path.arcTo(
                    Rect(width - 2 * cornerRadius, 0f, width, 2 * cornerRadius),
                    270f, 90f, false
                )
                path.lineTo(width, bodyHeight - cornerRadius)
                path.arcTo(
                    Rect(width - 2 * cornerRadius, bodyHeight - 2 * cornerRadius, width, bodyHeight),
                    0f, 90f, false
                )
                path.lineTo(computedArrowX + arrowWidth / 2, bodyHeight)
                path.lineTo(computedArrowX, height)
                path.lineTo(computedArrowX - arrowWidth / 2, bodyHeight)
                path.lineTo(cornerRadius, bodyHeight)
                path.arcTo(
                    Rect(0f, bodyHeight - 2 * cornerRadius, 2 * cornerRadius, bodyHeight),
                    90f, 90f, false
                )
                path.lineTo(0f, cornerRadius)
                path.arcTo(
                    Rect(0f, 0f, 2 * cornerRadius, 2 * cornerRadius),
                    180f, 90f, false
                )
                path.close()
            }
        }
        return Outline.Generic(path)
    }
}

data class SpotlightGuideStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val arrowDirection: ArrowDirection
)

@Composable
fun OnboardingGuideOverlay(
    menuRect: Rect? = null,
    profileRect: Rect? = null,
    dockRect: Rect? = null,
    onDismiss: () -> Unit
) {
    val steps = remember {
        listOf(
            SpotlightGuideStep(
                title = "Боковое меню",
                description = "Нажмите на иконку меню в левом верхнем углу, чтобы открыть все разделы: Рейтинг параллели, Мой класс, Посещаемость, Москвёнок (питание) и Настройки.",
                icon = Icons.Default.Menu,
                arrowDirection = ArrowDirection.TOP_LEFT
            ),
            SpotlightGuideStep(
                title = "Профиль и успеваемость",
                description = "В правом верхнем углу находится ваш профиль. Здесь можно посмотреть карточку учащегося, средний балл и перейти в настройки.",
                icon = Icons.Default.AccountCircle,
                arrowDirection = ArrowDirection.TOP_RIGHT
            ),
            SpotlightGuideStep(
                title = "Плавающий Док навигации",
                description = "Внизу экрана расположен удобный док для быстрого переключения в 1 касание между Главной, Расписанием, ДЗ, Оценками и Подарками.",
                icon = Icons.Default.Dashboard,
                arrowDirection = ArrowDirection.BOTTOM_CENTER
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]
    val isLastStep = currentStepIndex == steps.size - 1

    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary

    // Pulse animation for glowing ring around spotlight target
    val infiniteTransition = rememberInfiniteTransition(label = "spotlightPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseRadiusExtra by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadiusExtra"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (isLastStep) onDismiss() else currentStepIndex++
                }
            )
    ) {
        // Spotlight Canvas Dim + Cutout
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
        ) {
            // 1. Fill entire screen with deep dark dim
            drawRect(color = Color(0xDC0B0F19))

            // 2. Cutout transparent spotlight hole based on exact measured target bounds
            when (currentStepIndex) {
                0 -> {
                    // Top-Left Menu button
                    val center = menuRect?.center ?: Offset(with(density) { 36.dp.toPx() }, with(density) { 68.dp.toPx() })
                    val radius = (menuRect?.let { maxOf(it.width, it.height) / 2f + with(density) { 6.dp.toPx() } })
                        ?: with(density) { 26.dp.toPx() }

                    drawCircle(
                        color = Color.Transparent,
                        radius = radius,
                        center = center,
                        blendMode = BlendMode.Clear
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = pulseAlpha),
                        radius = radius + with(density) { pulseRadiusExtra.dp.toPx() },
                        center = center,
                        style = Stroke(width = with(density) { 3.dp.toPx() })
                    )
                }
                1 -> {
                    // Top-Right Profile button
                    val center = profileRect?.center ?: Offset(size.width - with(density) { 36.dp.toPx() }, with(density) { 68.dp.toPx() })
                    val radius = (profileRect?.let { maxOf(it.width, it.height) / 2f + with(density) { 6.dp.toPx() } })
                        ?: with(density) { 26.dp.toPx() }

                    drawCircle(
                        color = Color.Transparent,
                        radius = radius,
                        center = center,
                        blendMode = BlendMode.Clear
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = pulseAlpha),
                        radius = radius + with(density) { pulseRadiusExtra.dp.toPx() },
                        center = center,
                        style = Stroke(width = with(density) { 3.dp.toPx() })
                    )
                }
                2 -> {
                    // Bottom Navigation Dock
                    val left = (dockRect?.left ?: with(density) { 16.dp.toPx() }) - with(density) { 4.dp.toPx() }
                    val top = (dockRect?.top ?: (size.height - with(density) { 110.dp.toPx() })) - with(density) { 4.dp.toPx() }
                    val width = (dockRect?.width ?: (size.width - with(density) { 32.dp.toPx() })) + with(density) { 8.dp.toPx() }
                    val height = (dockRect?.height ?: with(density) { 60.dp.toPx() }) + with(density) { 8.dp.toPx() }
                    val cornerRadius = CornerRadius(height / 2f, height / 2f)

                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = cornerRadius,
                        blendMode = BlendMode.Clear
                    )
                    drawRoundRect(
                        color = primaryColor.copy(alpha = pulseAlpha),
                        topLeft = Offset(left - with(density) { (pulseRadiusExtra / 2).dp.toPx() }, top - with(density) { (pulseRadiusExtra / 2).dp.toPx() }),
                        size = Size(width + with(density) { pulseRadiusExtra.dp.toPx() }, height + with(density) { pulseRadiusExtra.dp.toPx() }),
                        cornerRadius = CornerRadius((height + pulseRadiusExtra) / 2f, (height + pulseRadiusExtra) / 2f),
                        style = Stroke(width = with(density) { 3.dp.toPx() })
                    )
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenHeightPx = with(density) { maxHeight.toPx() }
            val screenWidthPx = with(density) { maxWidth.toPx() }

            val targetCenterX = when (currentStepIndex) {
                0 -> menuRect?.center?.x ?: with(density) { 36.dp.toPx() }
                1 -> profileRect?.center?.x ?: (screenWidthPx - with(density) { 36.dp.toPx() })
                else -> dockRect?.center?.x ?: (screenWidthPx / 2f)
            }

            val bubbleShape = remember(currentStep.arrowDirection, targetCenterX) {
                val arrowXInCard = targetCenterX - with(density) { 20.dp.toPx() }
                SpeechBubbleShape(
                    arrowDirection = currentStep.arrowDirection,
                    arrowCenterXPx = arrowXInCard
                )
            }

            val topCardPadding = when (currentStepIndex) {
                0 -> menuRect?.let { with(density) { (it.bottom + 14.dp.toPx()).toDp() } } ?: 110.dp
                1 -> profileRect?.let { with(density) { (it.bottom + 14.dp.toPx()).toDp() } } ?: 110.dp
                else -> 0.dp
            }

            val bottomCardPadding = if (currentStep.arrowDirection == ArrowDirection.BOTTOM_CENTER) {
                dockRect?.let { with(density) { (screenHeightPx - it.top + 16.dp.toPx()).toDp() } } ?: 130.dp
            } else {
                0.dp
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = if (currentStep.arrowDirection == ArrowDirection.BOTTOM_CENTER) Alignment.BottomCenter else Alignment.TopCenter
            ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = topCardPadding,
                        bottom = bottomCardPadding
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Intercept clicks inside card
                    ),
                shape = bubbleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 22.dp,
                            end = 22.dp,
                            top = if (currentStep.arrowDirection != ArrowDirection.BOTTOM_CENTER) 26.dp else 20.dp,
                            bottom = if (currentStep.arrowDirection == ArrowDirection.BOTTOM_CENTER) 26.dp else 20.dp
                        )
                ) {
                    // Header Row: Badge & Skip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Шаг ${currentStepIndex + 1} из ${steps.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Пропустить",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title with Icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = currentStep.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = currentStep.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description text
                    Text(
                        text = currentStep.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Step Dots Indicator and Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dots
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            steps.indices.forEach { idx ->
                                val isSelected = idx == currentStepIndex
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant
                                        )
                                )
                            }
                        }

                        // Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (currentStepIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentStepIndex-- },
                                    shape = PillShape,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Назад", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            Button(
                                onClick = {
                                    if (isLastStep) {
                                        onDismiss()
                                    } else {
                                        currentStepIndex++
                                    }
                                },
                                shape = PillShape,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(if (isLastStep) "Понятно!" else "Далее", style = MaterialTheme.typography.labelMedium)
                                if (!isLastStep) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
