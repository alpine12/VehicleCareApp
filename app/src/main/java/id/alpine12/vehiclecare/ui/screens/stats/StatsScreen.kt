package id.alpine12.vehiclecare.ui.screens.stats

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.alpine12.vehiclecare.domain.model.ServiceType
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.model.VehicleType
import id.alpine12.vehiclecare.ui.components.EmptyState
import id.alpine12.vehiclecare.ui.components.ErrorState
import id.alpine12.vehiclecare.ui.components.LoadingList
import id.alpine12.vehiclecare.ui.viewmodel.StatsUiState
import id.alpine12.vehiclecare.ui.viewmodel.StatsViewModel
import id.alpine12.vehiclecare.ui.viewmodel.VehicleStats
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Statistik",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.totalServices > 0) {
                            Text(
                                text = "${uiState.totalServices} servis dilakukan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        StatsContent(
            uiState = uiState,
            onRefresh = viewModel::refresh,
            onSelectVehicle = viewModel::selectVehicle,
            onRetry = viewModel::refresh,
            numberFormat = numberFormat,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsContent(
    uiState: StatsUiState,
    onRefresh: () -> Unit,
    onSelectVehicle: (Long?) -> Unit,
    onRetry: () -> Unit,
    numberFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                LoadingList(
                    itemCount = 4,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            uiState.errorMessage != null && uiState.vehicles.isEmpty() -> {
                ErrorState(
                    message = uiState.errorMessage ?: "Terjadi kesalahan",
                    onRetry = onRetry
                )
            }

            uiState.vehicles.isEmpty() -> {
                EmptyState(
                    title = "Belum Ada Data",
                    message = "Mulai tambahkan kendaraan dan\ncatat setiap servis untuk melihat statistik",
                    icon = Icons.Outlined.DirectionsCar
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Summary Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.DirectionsCar,
                                value = uiState.totalVehicles.toString(),
                                label = "Kendaraan",
                                color = MaterialTheme.colorScheme.primary
                            )
                            SummaryStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Build,
                                value = uiState.totalServices.toString(),
                                label = "Total Servis",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Total Cost Card
                    item {
                        CostSummaryCard(
                            totalCost = uiState.totalCost,
                            numberFormat = numberFormat
                        )
                    }

                    // Vehicle Filter
                    if (uiState.vehicles.size > 1) {
                        item {
                            VehicleFilterRow(
                                vehicles = uiState.vehicles,
                                selectedVehicleId = uiState.selectedVehicleId,
                                onSelectVehicle = onSelectVehicle
                            )
                        }
                    }

                    // Per-Vehicle Stats
                    if (uiState.vehicleStats.isNotEmpty()) {
                        item {
                            SectionTitle(text = "Per Kendaraan")
                        }

                        val filteredStats = if (uiState.selectedVehicleId != null) {
                            uiState.vehicleStats.filter { it.vehicle.id == uiState.selectedVehicleId }
                        } else {
                            uiState.vehicleStats
                        }

                        items(filteredStats) { stat ->
                            VehicleStatsCard(
                                vehicleStats = stat,
                                numberFormat = numberFormat
                            )
                        }
                    }

                    // Top Service Types Chart
                    if (uiState.topServiceTypes.isNotEmpty()) {
                        item {
                            SectionTitle(text = "Servis Terpopuler")
                        }

                        item {
                            ServiceTypeChart(
                                serviceTypes = uiState.topServiceTypes,
                                totalServices = uiState.totalServices
                            )
                        }

                        items(uiState.topServiceTypes) { (type, count) ->
                            ServiceTypeRow(
                                serviceType = type,
                                count = count,
                                totalServices = uiState.totalServices,
                                numberFormat = numberFormat
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CostSummaryCard(
    totalCost: Long,
    numberFormat: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Pengeluaran",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp ${numberFormat.format(totalCost)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Icon(
                imageVector = Icons.Outlined.AttachMoney,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun VehicleFilterRow(
    vehicles: List<Vehicle>,
    selectedVehicleId: Long?,
    onSelectVehicle: (Long?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedVehicleId == null,
            onClick = { onSelectVehicle(null) },
            label = { Text("Semua") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        vehicles.forEach { vehicle ->
            FilterChip(
                selected = selectedVehicleId == vehicle.id,
                onClick = { onSelectVehicle(vehicle.id) },
                label = { Text(vehicle.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun VehicleStatsCard(
    vehicleStats: VehicleStats,
    numberFormat: NumberFormat
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = (RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (vehicleStats.vehicle.type == VehicleType.MOTOR)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (vehicleStats.vehicle.type == VehicleType.MOTOR)
                                Icons.Outlined.TwoWheeler
                            else
                                Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = vehicleStats.vehicle.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${vehicleStats.vehicle.brand} ${vehicleStats.vehicle.model}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Sembunyikan" else "Tampilkan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${vehicleStats.serviceCount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Servis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Rp ${numberFormat.format(vehicleStats.totalCost)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Total Biaya",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceTypeChart(
    serviceTypes: List<Pair<ServiceType, Int>>,
    totalServices: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val teriaryColor = MaterialTheme.colorScheme.tertiary
    val colors = listOf(primaryColor, secondaryColor, teriaryColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = (RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
            ) {
                var startAngle = -90f
                serviceTypes.forEachIndexed { index, (_, count) ->
                    val sweepAngle = (count.toFloat() / totalServices) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(size.width, size.height)
                    )
                    startAngle += sweepAngle
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                serviceTypes.forEachIndexed { index, (type, _) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colors[index % colors.size])
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceTypeRow(
    serviceType: ServiceType,
    count: Int,
    totalServices: Int,
    numberFormat: NumberFormat
) {
    val percentage = (count.toFloat() / totalServices * 100).toInt()
    val maxBarWidth = 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        shape = (RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = serviceType.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$count kali",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((count.toFloat() / totalServices).coerceAtMost(maxBarWidth))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
