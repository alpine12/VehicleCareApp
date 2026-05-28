package id.alpine12.vehiclecare.ui.screens.reminder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.alpine12.vehiclecare.ui.viewmodel.ReminderFormViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    vehicleId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ReminderFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    LaunchedEffect(vehicleId) { viewModel.initialize(vehicleId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }
    LaunchedEffect(state.errorMessage) { state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() } }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.targetDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { viewModel.updateTargetDate(it) }; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Tambah Reminder", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Kendaraan: ${state.vehicleName}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(value = state.title, onValueChange = viewModel::updateTitle, label = { Text("Judul Reminder *") }, placeholder = { Text("Contoh: Ganti Oli Berikutnya") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = state.description, onValueChange = viewModel::updateDescription, label = { Text("Deskripsi (Opsional)") }, placeholder = { Text("Tambahkan deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
            Text("Setel minimal satu pengingat:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = state.targetDate?.let { dateFormat.format(Date(it)) } ?: "",
                onValueChange = {},
                label = { Text("Tanggal Target (Opsional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                enabled = true,
                trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
            )
            OutlinedTextField(value = state.targetKm, onValueChange = viewModel::updateTargetKm, label = { Text("Kilometer Target (Opsional)") }, placeholder = { Text("Contoh: 30000") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text("km") }, singleLine = true)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.saveReminder() }, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading) { Text(if (state.isLoading) "Menyimpan..." else "Simpan Reminder", modifier = Modifier.padding(vertical = 8.dp)) }
        }
    }
}