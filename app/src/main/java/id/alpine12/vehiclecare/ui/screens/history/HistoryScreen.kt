package id.alpine12.vehiclecare.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.model.VehicleType
import id.alpine12.vehiclecare.ui.components.EmptyState
import id.alpine12.vehiclecare.ui.components.ErrorState
import id.alpine12.vehiclecare.ui.components.LoadingList
import id.alpine12.vehiclecare.ui.viewmodel.HistoryUiState
import id.alpine12.vehiclecare.ui.viewmodel.HistoryViewModel
import id.alpine12.vehiclecare.ui.viewmodel.ServiceWithVehicle
import id.alpine12.vehiclecare.ui.viewmodel.SortOrder
import java.text.NumberFormat
import java.text.SimpleDateFormat
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Vehicle filter chips
                    if (uiState.vehicles.isNotEmpty()) {
                        item {
                            VehicleFilterChips(
                                vehicles = uiState.vehicles,
                                selectedVehicleId = uiState.selectedVehicleId,
                                onSelectVehicle = onSelectVehicle
                            )
                        }
                    }

                    // Show filtered count if filtering
                    if (uiState.selectedVehicleId != null) {
                        item {
                            Text(
                                text = "Menampilkan ${uiState.filteredServices.size} dari ${uiState.allServices.size} catatan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    items(
                        items = uiState.filteredServices,
                        key = { it.service.id }
                    ) { item ->
                        HistoryCard(
                            item = item,
                            dateFormat = dateFormat,
                            numberFormat = numberFormat
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun VehicleFilterChips(
    vehicles: List<Vehicle>,
    selectedVehicleId: Long?,
    onSelectVehicle: (Long?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
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

@Composable
private fun HistoryCard(
    item: ServiceWithVehicle,
    dateFormat: SimpleDateFormat,
    numberFormat: NumberFormat
) {
    val service = item.service
    val vehicle = item.vehicle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Vehicle name badge
            vehicle?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = if (it.type == VehicleType.MOTOR)
                            Icons.Outlined.TwoWheeler
                        else
                            Icons.Outlined.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Service type and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = service.type.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = dateFormat.format(Date(service.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Kilometer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${numberFormat.format(service.kilometer)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (service.workshop.isNotBlank()) {
                    Column {
                        Text(
                            text = "Bengkel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = service.workshop,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Cost
            service.cost?.let { cost ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Biaya: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rp ${numberFormat.format(cost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Notes
            if (service.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = service.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
