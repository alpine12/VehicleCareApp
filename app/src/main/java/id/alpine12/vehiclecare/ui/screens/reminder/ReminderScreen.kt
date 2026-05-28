package id.alpine12.vehiclecare.ui.screens.reminder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.alpine12.vehiclecare.ui.components.DeleteConfirmDialog
import id.alpine12.vehiclecare.ui.components.EmptyState
import id.alpine12.vehiclecare.ui.components.ErrorState
import id.alpine12.vehiclecare.ui.components.LoadingList
import id.alpine12.vehiclecare.ui.viewmodel.ReminderUiState
import id.alpine12.vehiclecare.ui.viewmodel.ReminderViewModel
import id.alpine12.vehiclecare.ui.viewmodel.ReminderWithVehicle
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onNavigateToSelectVehicle: () -> Unit,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var reminderToDelete by remember { mutableStateOf<Long?>(null) }
    var showCompletedSection by remember { mutableStateOf(true) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    // Delete confirmation dialog
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
                    Column {
                        Text(
                            text = "Pengingat",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.pendingReminders.isNotEmpty()) {
                            Text(
                                text = "${uiState.pendingReminders.size} aktif",
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
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onNavigateToSelectVehicle,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Reminder",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        ReminderContent(
            uiState = uiState,
            showCompletedSection = showCompletedSection,
            onToggleCompletedSection = { showCompletedSection = !showCompletedSection },
            onRefresh = viewModel::refresh,
            onMarkCompleted = viewModel::markAsCompleted,
            onDelete = { reminderToDelete = it },
            onRetry = viewModel::refresh,
            dateFormat = dateFormat,
            numberFormat = numberFormat,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderContent(
    uiState: ReminderUiState,
    showCompletedSection: Boolean,
    onToggleCompletedSection: () -> Unit,
    onRefresh: () -> Unit,
    onMarkCompleted: (Long) -> Unit,
    onDelete: (Long) -> Unit,
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
                    itemCount = 4,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            uiState.errorMessage != null && uiState.pendingReminders.isEmpty() && uiState.completedReminders.isEmpty() -> {
                ErrorState(
                    message = uiState.errorMessage ?: "Terjadi kesalahan",
                    onRetry = onRetry
                )
            }

            uiState.pendingReminders.isEmpty() && uiState.completedReminders.isEmpty() -> {
                EmptyState(
                    title = "Belum Ada Pengingat",
                    message = "Tekan tombol + untuk mengatur\npengingat servis kendaraanmu",
                    icon = Icons.Outlined.Notifications
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

                    // Pending Reminders
                    if (uiState.pendingReminders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Akan Datang",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        items(
                            items = uiState.pendingReminders,
                            key = { it.reminder.id }
                        ) { item ->
                            ReminderCard(
                                item = item,
                                dateFormat = dateFormat,
                                numberFormat = numberFormat,
                                onMarkCompleted = { onMarkCompleted(item.reminder.id) },
                                onDelete = { onDelete(item.reminder.id) }
                            )
                        }
                    }

                    // Completed Reminders
                    if (uiState.completedReminders.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selesai (${uiState.completedReminders.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = onToggleCompletedSection) {
                                    Text(
                                        text = if (showCompletedSection) "Sembunyikan" else "Tampilkan",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        if (showCompletedSection) {
                            items(
                                items = uiState.completedReminders,
                                key = { "completed_${it.reminder.id}" }
                            ) { item ->
                                ReminderCard(
                                    item = item,
                                    dateFormat = dateFormat,
                                    numberFormat = numberFormat,
                                    onMarkCompleted = null,
                                    onDelete = { onDelete(item.reminder.id) },
                                    isCompleted = true
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(88.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    item: ReminderWithVehicle,
    dateFormat: SimpleDateFormat,
    numberFormat: NumberFormat,
    onMarkCompleted: (() -> Unit)?,
    onDelete: () -> Unit,
    isCompleted: Boolean = false
) {
    val reminder = item.reminder
    val vehicle = item.vehicle

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.secondaryContainer
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    vehicle?.let {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Row {
                    if (onMarkCompleted != null) {
                        TextButton(onClick = onMarkCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Selesai")
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                reminder.targetDate?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateFormat.format(Date(it)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                reminder.targetKm?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${numberFormat.format(it)} km",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (reminder.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}
