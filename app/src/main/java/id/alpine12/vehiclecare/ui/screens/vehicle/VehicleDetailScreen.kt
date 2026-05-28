package id.alpine12.vehiclecare.ui.screens.vehicle

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import id.alpine12.vehiclecare.domain.model.Reminder
import id.alpine12.vehiclecare.domain.model.ServiceRecord
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.model.VehicleType
import id.alpine12.vehiclecare.ui.components.ConfirmDialog
import id.alpine12.vehiclecare.ui.components.DeleteConfirmDialog
import id.alpine12.vehiclecare.ui.components.ErrorState
import id.alpine12.vehiclecare.ui.components.LoadingIndicator
import id.alpine12.vehiclecare.ui.viewmodel.VehicleDetailUiState
import id.alpine12.vehiclecare.ui.viewmodel.VehicleDetailViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditVehicle: () -> Unit,
    onNavigateToAddService: () -> Unit,
    onNavigateToAddReminder: () -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteVehicleDialog by remember { mutableStateOf(false) }
    var serviceToDelete by remember { mutableStateOf<Long?>(null) }
    var reminderToDelete by remember { mutableStateOf<Long?>(null) }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    LaunchedEffect(vehicleId) {
        viewModel.loadVehicle(vehicleId)
    }

    // Confirmation dialogs
    if (showDeleteVehicleDialog) {
        DeleteConfirmDialog(
            itemName = uiState.vehicle?.name ?: "kendaraan ini",
            onConfirm = {
                viewModel.deleteVehicle(onNavigateBack)
                showDeleteVehicleDialog = false
            },
            onDismiss = { showDeleteVehicleDialog = false }
        )
    }

    serviceToDelete?.let { id ->
        DeleteConfirmDialog(
            itemName = "riwayat servis ini",
            onConfirm = {
                viewModel.deleteServiceRecord(id)
                serviceToDelete = null
            },
            onDismiss = { serviceToDelete = null }
        )
    }

    reminderToDelete?.let { id ->
        DeleteConfirmDialog(
            itemName = "reminder ini",
            onConfirm = {
                viewModel.deleteReminder(id)
                reminderToDelete = null
            },
            onDismiss = { reminderToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.vehicle?.name ?: "Detail Kendaraan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEditVehicle) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteVehicleDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        VehicleDetailContent(
            uiState = uiState,
            onNavigateToAddService = onNavigateToAddService,
            onNavigateToAddReminder = onNavigateToAddReminder,
            onToggleShowAllServices = viewModel::toggleShowAllServices,
            onMarkReminderCompleted = viewModel::markReminderCompleted,
            onDeleteService = { serviceToDelete = it },
            onDeleteReminder = { reminderToDelete = it },
            onRetry = { viewModel.loadVehicle(vehicleId) },
            onClearError = viewModel::clearError,
            numberFormat = numberFormat,
            dateFormat = dateFormat,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun VehicleDetailContent(
    uiState: VehicleDetailUiState,
    onNavigateToAddService: () -> Unit,
    onNavigateToAddReminder: () -> Unit,
    onToggleShowAllServices: () -> Unit,
    onMarkReminderCompleted: (Long) -> Unit,
    onDeleteService: (Long) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            LoadingIndicator(modifier = modifier)
        }

        uiState.errorMessage != null && uiState.vehicle == null -> {
            ErrorState(
                message = uiState.errorMessage ?: "Terjadi kesalahan",
                onRetry = onRetry,
                modifier = modifier
            )
        }

        uiState.vehicle == null -> {
            ErrorState(
                message = "Kendaraan tidak ditemukan",
                modifier = modifier
            )
        }

        else -> {
            val vehicle = uiState.vehicle!!
            val displayServices = if (uiState.showAllServices) {
                uiState.serviceRecords
            } else {
                uiState.serviceRecords.take(5)
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Vehicle Info Card
                item { VehicleInfoCard(vehicle, numberFormat, dateFormat) }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToAddService,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Servis")
                        }
                        OutlinedButton(
                            onClick = onNavigateToAddReminder,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reminder")
                        }
                    }
                }

                // Active Reminders Section
                val activeReminders = uiState.reminders.filter { it.isActive && !it.isCompleted }
                if (activeReminders.isNotEmpty()) {
                    item {
                        Text(
                            text = "Pengingat Aktif",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(
                        items = activeReminders,
                        key = { "reminder_${it.id}" }
                    ) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            dateFormat = dateFormat,
                            numberFormat = numberFormat,
                            onMarkCompleted = { onMarkReminderCompleted(reminder.id) },
                            onDelete = { onDeleteReminder(reminder.id) }
                        )
                    }
                }

                // Service History Section
                if (uiState.serviceRecords.isNotEmpty()) {
                    item {
                        Text(
                            text = "Riwayat Servis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(
                        items = displayServices,
                        key = { "service_${it.id}" }
                    ) { service ->
                        ServiceCard(
                            service = service,
                            dateFormat = dateFormat,
                            numberFormat = numberFormat,
                            onDelete = { onDeleteService(service.id) }
                        )
                    }

                    // Show More/Less Button
                    if (uiState.serviceRecords.size > 5) {
                        item {
                            TextButton(
                                onClick = onToggleShowAllServices,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (uiState.showAllServices)
                                        "Tampilkan lebih sedikit"
                                    else
                                        "Lihat semua ${uiState.serviceRecords.size} servis"
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun VehicleInfoCard(
    vehicle: Vehicle,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (vehicle.type == VehicleType.MOTOR)
                            Icons.Outlined.TwoWheeler
                        else
                            Icons.Outlined.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${vehicle.brand} ${vehicle.model}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${vehicle.type.displayName} • ${vehicle.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    if (vehicle.licensePlate.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = vehicle.licensePlate,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${numberFormat.format(vehicle.currentKm)} km",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    dateFormat: SimpleDateFormat,
    numberFormat: NumberFormat,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))

                reminder.targetDate?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateFormat.format(Date(it)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                reminder.targetKm?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${numberFormat.format(it)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            TextButton(onClick = onMarkCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Selesai")
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: ServiceRecord,
    dateFormat: SimpleDateFormat,
    numberFormat: NumberFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = service.type.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(service.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${numberFormat.format(service.kilometer)} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (service.workshop.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = service.workshop,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                service.cost?.let { cost ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rp ${numberFormat.format(cost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
