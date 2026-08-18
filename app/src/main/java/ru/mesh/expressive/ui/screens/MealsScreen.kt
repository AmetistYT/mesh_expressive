package ru.mesh.expressive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mesh.expressive.ui.theme.*
import ru.mesh.expressive.ui.viewmodel.MeshMainViewModel

@Composable
fun MealsScreen(viewModel: MeshMainViewModel) {
    val meals by viewModel.mealsBalance.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Hero Moskvionok Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveHeroShape,
                colors = CardDefaults.cardColors(
                    containerColor = MoskvionokBlueContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Карта «Москвёнок»",
                                style = MaterialTheme.typography.labelLarge,
                                color = MoskvionokBlue
                            )
                            Text(
                                text = "${String.format("%.2f", meals.clientBalanceRub)} ₽",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF01579B)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MoskvionokBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Лицевой счет: ${meals.cardId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0277BD)
                        )
                        Text(
                            text = "Лимит: ${if (meals.dailyLimitRub != null && meals.dailyLimitRub!! > 0) "${meals.dailyLimitRub!!.toInt()} ₽/день" else "Не установлен"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0277BD)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { /* Open MosPay TopUp */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MoskvionokBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Пополнить через MosPay (СБП)")
                    }
                }
            }
        }

        // Canteen Status Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = if (meals.hotMealSubscribed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Горячее школьное питание",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (meals.hotMealSubscribed) "Комплексный обед подключен" else "Комплексный обед не подключен",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (meals.hotMealSubscribed) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = PillShape,
                        color = if (meals.hotMealSubscribed) ScoreGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (meals.hotMealSubscribed) "Активно" else "Не активно",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (meals.hotMealSubscribed) ScoreGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Transactions History
        item {
            Text(
                text = "История питания и покупок",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (meals.transactions.isEmpty()) {
            item {
                ru.mesh.expressive.ui.components.ExpressiveEmptyState(
                    title = "Здесь ничего нет",
                    subtitle = "История покупок и операций пуста",
                    icon = Icons.Default.Restaurant
                )
            }
        } else {
            items(meals.transactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                                text = tx.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tx.timestamp,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = if (tx.isDebit) String.format(java.util.Locale.getDefault(), "-%.2f ₽", tx.amountRub) else String.format(java.util.Locale.getDefault(), "+%.2f ₽", tx.amountRub),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.isDebit) MaterialTheme.colorScheme.onSurface else ScoreGreen
                        )
                    }
                }
            }
        }
    }
}
