package id.alpine12.vehiclecare.ui.screens.history

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import id.alpine12.vehiclecare.ui.theme.PrimaryBlue
import id.alpine12.vehiclecare.ui.theme.SuccessGreen
import id.alpine12.vehiclecare.ui.theme.WarningAmber
import id.alpine12.vehiclecare.ui.viewmodel.HistoryUiState
import id.alpine12.vehiclecare.ui.viewmodel.HistoryViewModel
import id.alpine12.vehiclecare.ui.viewmodel.ServiceWithVehicle
import id.alpine12.vehiclecare.ui.viewmodel.SortOrder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("id", "ID")) }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Riwayat Servis",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.filteredServices.isNotEmpty()) {
                            Text(
                                text = "${uiState.filteredServices.size} catatan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Urutkan"
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = order.displayName,
                                        fontWeight = if (uiState.sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.setSortOrder(order)
                                    showSortMenu = false
                                }
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
        HistoryContent(
            uiState = uiState,
            onRefresh = viewModel::refresh,
            onSelectVehicle = viewModel::selectVehicle,
            onRetry = viewModel::refresh,
            dateFormat = dateFormat,
            monthYearFormat = monthYearFormat,
            numberFormat = numberFormat,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryContent(
    uiState: HistoryUiState,
    onRefresh: () -> Unit,
    onSelectVehicle: (Long?) -> Unit,
    onRetry: () -> Unit,
    dateFormat: SimpleDateFormat,
    monthYearFormat: SimpleDateFormat,
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
                    itemCount = 5,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            uiState.errorMessage != null && uiState.allServices.isEmpty() -> {
                ErrorState(
                    message = uiState.errorMessage ?: "Terjadi kesalahan",
                    onRetry = onRetry
                )
            }

            uiState.allServices.isEmpty() -> {
                EmptyState(
                    title = "Belum Ada Riwayat",
                    message = "Catat servis kendaraanmu untuk\nmelihat riwayat di sini",
                    icon = Icons.Outlined.History
                )
            }

            else -> {
                // Group services by month
                val groupedServices = remember(uiState.filteredServices) {
                    uiState.filteredServices.groupBy { service ->
                        val cal = Calendar.getInstance().apply { timeInMillis = service.service.date }
                        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
                    }.toSortedMap(compareByDescending { it })
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Vehicle filter chips
                    if (uiState.vehicles.isNotEmpty()) {
                        item {
                            VehicleFilterChips(
                                vehicles = uiState.vehicles,
                                selectedVehicleId = uiState.selectedVehicleId,
                                onSelectVehicle = onSelectVehicle,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    // Grouped Timeline
                    groupedServices.forEach { (monthKey, services) ->
                        val firstServiceDate = services.first().service.date
                        val monthTitle = monthYearFormat.format(Date(firstServiceDate))
                        val totalCost = services.sumOf { it.service.cost?.toLong() ?: 0L }

                        // Month Header
                        item(key = "header_$monthKey") {
                            MonthHeader(
                                monthTitle = monthTitle,
                                serviceCount = services.size,
                                totalCost = totalCost,
                                numberFormat = numberFormat
                            )
                        }

                        // Timeline items with connecting line
                        items(
                            items = services,
                            key = { it.service.id }
                        ) { item ->
                            TimelineItem(
                                item = item,
                                dateFormat = dateFormat,
                                numberFormat = numberFormat,
                                isFirst = services.first() == item,
                                isLast = services.last() == item
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
private fun MonthHeader(
    monthTitle: String,
    serviceCount: Int,
    totalCost: Long,
    numberFormat: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$serviceCount servis",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (totalCost > 0) {
            Box(
                modifier = Modifier
                    .background(
                        color = SuccessGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Rp ${numberFormat.format(totalCost)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    item: ServiceWithVehicle,
    dateFormat: SimpleDateFormat,
    numberFormat: NumberFormat,
    isFirst: Boolean,
    isLast: Boolean
) {
    val service = item.service
    val vehicle = item.vehicle

    // Service type colors
    val serviceColor = when (service.type) {
        ServiceType.GANTI_OLI -> PrimaryBlue
        ServiceType.SERVIS -> SuccessGreen
        ServiceType.TAMBAH_ANGIN -> MaterialTheme.colorScheme.tertiary
        ServiceType.GANTI_AIR_RADIATOR -> Color(0xFF06B6D4)
        ServiceType.LAINNYA -> MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Top line
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Circle indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(serviceColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.White
                )
            }

            // Bottom line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Card content
        HistoryCardModern(
            item = item,
            dateFormat = dateFormat,
            numberFormat = numberFormat,
            serviceColor = serviceColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HistoryCardModern(
    item: ServiceWithVehicle,
    dateFormat: SimpleDateFormat,
    numberFormat: NumberFormat,
    serviceColor: Color,
    modifier: Modifier = Modifier
) {
    val service = item.service
    val vehicle = item.vehicle

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(serviceColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Build,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = serviceColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = service.type.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        vehicle?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (it.type == VehicleType.MOTOR)
                                        Icons.Outlined.TwoWheeler
                                    else
                                        Icons.Outlined.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = dateFormat.format(Date(service.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Km info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${numberFormat.format(service.kilometer)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (service.workshop.isNotBlank()) {
                        Text(
                            text = " • ${service.workshop}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Cost badge
                service.cost?.let { cost ->
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        SuccessGreen.copy(alpha = 0.1f),
                                        SuccessGreen.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Rp ${numberFormat.format(cost)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            // Notes
            if (service.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = service.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun VehicleFilterChips(
    vehicles: List<Vehicle>,
    selectedVehicleId: Long?,
    onSelectVehicle: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
                leadingIcon = if (selectedVehicleId == vehicle.id) {
                    {
                        Icon(
                            imageVector = if (vehicle.type == VehicleType.MOTOR)
                                Icons.Outlined.TwoWheeler
                            else
                                Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
