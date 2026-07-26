package id.alpine12.vehiclecare.ui.screens.vehicle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.alpine12.vehiclecare.domain.model.Reminder
import id.alpine12.vehiclecare.domain.model.ServiceRecord
import id.alpine12.vehiclecare.domain.model.Vehicle
import id.alpine12.vehiclecare.domain.model.VehicleType
import id.alpine12.vehiclecare.ui.components.DeleteConfirmDialog
import id.alpine12.vehiclecare.ui.components.ErrorState
import id.alpine12.vehiclecare.ui.components.LoadingIndicator
import id.alpine12.vehiclecare.ui.theme.ErrorRed
import id.alpine12.vehiclecare.ui.theme.SuccessGreen
import id.alpine12.vehiclecare.ui.theme.WarningAmber
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
    onNavigateToHistory: () -> Unit = {},
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddService,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Catat Servis")
            }
        }
    ) { padding ->
        VehicleDetailContent(
            uiState = uiState,
            onNavigateToAddService = onNavigateToAddService,
            onNavigateToAddReminder = onNavigateToAddReminder,
            onNavigateToHistory = onNavigateToHistory,
            onMarkReminderCompleted = viewModel::markReminderCompleted,
            onDeleteService = { serviceToDelete = it },
            onDeleteReminder = { reminderToDelete = it },
            onRetry = { viewModel.loadVehicle(vehicleId) },
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
    onNavigateToHistory: () -> Unit,
    onMarkReminderCompleted: (Long) -> Unit,
    onDeleteService: (Long) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onRetry: () -> Unit,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> LoadingIndicator(modifier = modifier)
        uiState.errorMessage != null && uiState.vehicle == null -> ErrorState(
            message = uiState.errorMessage ?: "Terjadi kesalahan",
            onRetry = onRetry,
            modifier = modifier
        )
        uiState.vehicle == null -> ErrorState(
            message = "Kendaraan tidak ditemukan",
            modifier = modifier
        )
        else -> {
            val vehicle = uiState.vehicle!!
            val displayServices = uiState.serviceRecords.take(3)

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Vehicle Header Card with Gradient
                item { VehicleHeaderCard(vehicle, numberFormat, dateFormat) }

                // Service Progress Card
                item {
                    ServiceProgressCard(
                        vehicle = vehicle,
                        lastOilServiceKm = uiState.lastOilServiceKm,
                        lastOilServiceDate = uiState.lastOilServiceDate,
                        numberFormat = numberFormat,
                        dateFormat = dateFormat
                    )
                }

                // Quick Stats Row
                item {
                    QuickStatsRow(
                        totalServices = uiState.totalServiceCount,
                        totalCost = uiState.totalServiceCost,
                        numberFormat = numberFormat
                    )
                }

                // Reminder Button
                item {
                    OutlinedButton(
                        onClick = onNavigateToAddReminder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buat Pengingat", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Active Reminders Section
                val activeReminders = uiState.reminders.filter { it.isActive && !it.isCompleted }
                if (activeReminders.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Pengingat Aktif",
                            count = activeReminders.size
                        )
                    }
                    items(
                        items = activeReminders,
                        key = { "reminder_${it.id}" }
                    ) { reminder ->
                        ReminderCardModern(
                            reminder = reminder,
                            dateFormat = dateFormat,
                            numberFormat = numberFormat,
                            onMarkCompleted = { onMarkReminderCompleted(reminder.id) },
                            onDelete = { onDeleteReminder(reminder.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Service History Section
                if (uiState.serviceRecords.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Riwayat Servis",
                            count = uiState.serviceRecords.size,
                            onClick = onNavigateToHistory
                        )
                    }

                    items(
                        items = displayServices,
                        key = { "service_${it.id}" }
                    ) { service ->
                        ServiceCardModern(
                            service = service,
                            dateFormat = dateFormat,
                            numberFormat = numberFormat,
                            onDelete = { onDeleteService(service.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    if (uiState.serviceRecords.size > 3) {
                        item {
                            FilledTonalButton(
                                onClick = onNavigateToHistory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lihat semua ${uiState.serviceRecords.size} servis")
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
private fun SectionHeader(title: String, count: Int, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Lihat semua",
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { scaleX = -1f },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VehicleHeaderCard(
    vehicle: Vehicle,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    )
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
                            .size(72.dp)
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
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vehicle.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${vehicle.brand} ${vehicle.model} • ${vehicle.year}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        if (vehicle.licensePlate.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = vehicle.licensePlate,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current KM Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Kilometer Saat Ini",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
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
        }
    }
}

@Composable
private fun ServiceProgressCard(
    vehicle: Vehicle,
    lastOilServiceKm: Long?,
    lastOilServiceDate: Long?,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    val lastKm = lastOilServiceKm ?: vehicle.currentKm.toLong()
    val oilInterval = vehicle.oilIntervalKm.toLong()
    val nextServiceKm = lastKm + oilInterval
    val kmTraveledSinceOil = vehicle.currentKm.toLong() - lastKm
    val progress = (kmTraveledSinceOil.toFloat() / oilInterval).coerceIn(0f, 1f)
    val remainingKm = (nextServiceKm - vehicle.currentKm.toLong()).coerceAtLeast(0L)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    // Determine status
    val (statusText, statusColor, statusBgColor) = when {
        progress >= 1f -> Triple("LEWAT!", ErrorRed, ErrorRed.copy(alpha = 0.1f))
        progress >= 0.9f -> Triple("Segera Servis!", WarningAmber, WarningAmber.copy(alpha = 0.1f))
        progress >= 0.7f -> Triple("Perlu Perhatian", WarningAmber.copy(alpha = 0.8f), WarningAmber.copy(alpha = 0.08f))
        else -> Triple("Baik", SuccessGreen, SuccessGreen.copy(alpha = 0.1f))
    }

    val progressColor by animateColorAsState(
        targetValue = when {
            progress >= 0.9f -> ErrorRed
            progress >= 0.7f -> WarningAmber
            else -> SuccessGreen
        },
        animationSpec = tween(500),
        label = "progressColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Build,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Status Servis Oli",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBgColor
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${numberFormat.format(kmTraveledSinceOil)} km",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${numberFormat.format(oilInterval)} km",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "Sisa KM",
                    value = "${numberFormat.format(remainingKm)} km",
                    icon = Icons.Outlined.Speed,
                    valueColor = progressColor
                )
                InfoItem(
                    label = "Km Servis Berikutnya",
                    value = "${numberFormat.format(nextServiceKm)}",
                    icon = Icons.Outlined.Build,
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }

            lastOilServiceDate?.let { date ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Terakhir ganti oli: ${dateFormat.format(Date(date))} (${numberFormat.format(lastOilServiceKm)} km)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = valueColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun QuickStatsRow(
    totalServices: Int,
    totalCost: Long,
    numberFormat: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Build,
            label = "Total Servis",
            value = totalServices.toString(),
            color = MaterialTheme.colorScheme.primary
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Warning,
            label = "Total Biaya",
            value = "Rp ${numberFormat.format(totalCost)}",
            color = SuccessGreen
        )
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ReminderCardModern(
    reminder: Reminder,
    dateFormat: SimpleDateFormat,
    numberFormat: NumberFormat,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarningAmber.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(WarningAmber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = WarningAmber
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    reminder.targetDate?.let {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateFormat.format(Date(it)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    reminder.targetKm?.let {
                        Icon(
                            imageVector = Icons.Outlined.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${numberFormat.format(it)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            FilledTonalButton(
                onClick = onMarkCompleted,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = ErrorRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ServiceCardModern(
    service: ServiceRecord,
    dateFormat: SimpleDateFormat,
    numberFormat: NumberFormat,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val serviceColor = getServiceTypeColor(service.type)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Service type indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(serviceColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getServiceTypeIcon(service.type),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = serviceColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.type.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(service.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${numberFormat.format(service.kilometer)} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (service.workshop.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = service.workshop,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                service.cost?.let { cost ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rp ${numberFormat.format(cost)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = serviceColor
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun getServiceTypeColor(type: id.alpine12.vehiclecare.domain.model.ServiceType): Color {
    return when (type) {
        id.alpine12.vehiclecare.domain.model.ServiceType.GANTI_OLI -> Color(0xFF3B82F6) // Blue
        id.alpine12.vehiclecare.domain.model.ServiceType.SERVIS -> Color(0xFF10B981) // Green
        id.alpine12.vehiclecare.domain.model.ServiceType.TAMBAH_ANGIN -> Color(0xFF8B5CF6) // Purple
        id.alpine12.vehiclecare.domain.model.ServiceType.GANTI_AIR_RADIATOR -> Color(0xFF06B6D4) // Cyan
        id.alpine12.vehiclecare.domain.model.ServiceType.LAINNYA -> Color(0xFF6B7280) // Gray
    }
}

private fun getServiceTypeIcon(type: id.alpine12.vehiclecare.domain.model.ServiceType): ImageVector {
    return when (type) {
        id.alpine12.vehiclecare.domain.model.ServiceType.GANTI_OLI -> Icons.Outlined.Build
        id.alpine12.vehiclecare.domain.model.ServiceType.SERVIS -> Icons.Outlined.Build
        id.alpine12.vehiclecare.domain.model.ServiceType.TAMBAH_ANGIN -> Icons.Outlined.Speed
        id.alpine12.vehiclecare.domain.model.ServiceType.GANTI_AIR_RADIATOR -> Icons.Outlined.WaterDrop
        id.alpine12.vehiclecare.domain.model.ServiceType.LAINNYA -> Icons.Outlined.MoreHoriz
    }
}
