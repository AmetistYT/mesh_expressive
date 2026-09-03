package ru.mesh.expressive.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.mesh.expressive.data.model.VacationPeriodInfo
import ru.mesh.expressive.ui.theme.ExpressiveCardShape
import ru.mesh.expressive.ui.theme.PillShape
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VacationsCard(
    viewModel: MeshMainViewModel,
    modifier: Modifier = Modifier
) {
    val upcomingVacation by viewModel.upcomingVacation.collectAsState()
    val allVacations by viewModel.vacationPeriods.collectAsState()
    var showAllVacationsSheet by remember { mutableStateOf(false) }

    if (upcomingVacation != null || allVacations.isNotEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { showAllVacationsSheet = true },
            shape = ExpressiveCardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        val vac = upcomingVacation ?: allVacations.firstOrNull()
                        val title = vac?.title ?: "Каникулы"
                        val dateRange = if (vac != null) formatVacationDates(vac.startDate, vac.endDate) else ""

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (dateRange.isNotBlank()) dateRange else "Расписание четвертей и каникул",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                val daysLeft = upcomingVacation?.daysUntilStart
                if (daysLeft != null && daysLeft > 0) {
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "$daysLeft дн.",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (upcomingVacation?.isCurrent == true) {
                    Surface(
                        shape = PillShape,
                        color = Color(0xFF2E7D32)
                    ) {
                        Text(
                            text = "Сейчас",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    if (showAllVacationsSheet) {
        VacationsBottomSheet(
            vacations = allVacations,
            onDismiss = { showAllVacationsSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacationsBottomSheet(
    vacations: List<VacationPeriodInfo>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        windowInsets = WindowInsets(0, 0, 0, 0),
        shape = ExpressiveCardShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Каникулы учебного года",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (vacations.isEmpty()) {
                Text(
                    text = "Расписание каникул загружается...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                vacations.forEach { vac ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (vac.isCurrent)
                                MaterialTheme.colorScheme.primaryContainer
                            else if (vac.isUpcoming)
                                MaterialTheme.colorScheme.surfaceContainerLow
                            else
                                MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = vac.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (vac.isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatVacationDates(vac.startDate, vac.endDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (vac.isCurrent) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                if (vac.isCurrent) {
                                    Surface(shape = PillShape, color = Color(0xFF2E7D32)) {
                                        Text(
                                            text = "Сейчас",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                } else if (vac.daysUntilStart != null) {
                                    Text(
                                        text = "через ${vac.daysUntilStart} дн.",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${vac.daysTotal} дн.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatVacationDates(startIso: String, endIso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val d1 = parser.parse(startIso)
        val d2 = parser.parse(endIso)
        if (d1 != null && d2 != null) {
            val f1 = SimpleDateFormat("d MMMM", Locale("ru")).format(d1)
            val f2 = SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(d2)
            "$f1 — $f2"
        } else {
            "$startIso — $endIso"
        }
    } catch (_: Exception) {
        "$startIso — $endIso"
    }
}
