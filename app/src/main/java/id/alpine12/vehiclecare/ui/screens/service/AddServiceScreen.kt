package id.alpine12.vehiclecare.ui.screens.service

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.FilterChip
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
import id.alpine12.vehiclecare.domain.model.ServiceType
import id.alpine12.vehiclecare.ui.viewmodel.ServiceFormViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    vehicleId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ServiceFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    LaunchedEffect(vehicleId) { viewModel.initialize(vehicleId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }
    LaunchedEffect(state.errorMessage) { state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() } }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { viewModel.updateDate(it) }; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Servis", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Kendaraan: ${state.vehicleName}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text("Jenis Servis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ServiceType.entries) { type -> FilterChip(selected = state.type == type, onClick = { viewModel.updateType(type) }, label = { Text(type.displayName) }) }
            }
            OutlinedTextField(
                value = dateFormat.format(Date(state.date)),
                onValueChange = {},
                label = { Text("Tanggal") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                enabled = true,
                trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
            )
            OutlinedTextField(value = state.kilometer, onValueChange = viewModel::updateKilometer, label = { Text("Kilometer *") }, placeholder = { Text("Contoh: 25000") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text("km") }, singleLine = true)
            OutlinedTextField(value = state.productBrand, onValueChange = viewModel::updateProductBrand, label = { Text("Merk Oli/Part (Opsional)") }, placeholder = { Text("Contoh: Pertamax Enduro 4T") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = state.cost, onValueChange = viewModel::updateCost, label = { Text("Biaya (Opsional)") }, placeholder = { Text("Contoh: 75000") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), prefix = { Text("Rp ") }, singleLine = true)
            OutlinedTextField(value = state.workshop, onValueChange = viewModel::updateWorkshop, label = { Text("Nama Bengkel (Opsional)") }, placeholder = { Text("Contoh: AHASS Maju") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = state.notes, onValueChange = viewModel::updateNotes, label = { Text("Catatan (Opsional)") }, placeholder = { Text("Tambahkan catatan") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 5)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.saveService() }, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading) {
                Text(if (state.isLoading) "Menyimpan..." else "Simpan Servis", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}