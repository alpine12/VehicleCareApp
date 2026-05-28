package id.alpine12.vehiclecare.ui.screens.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.alpine12.vehiclecare.domain.model.VehicleType
import id.alpine12.vehiclecare.ui.viewmodel.VehicleFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    onNavigateBack: () -> Unit,
    viewModel: VehicleFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }
    LaunchedEffect(state.errorMessage) { state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() } }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Kendaraan", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = state.name, onValueChange = viewModel::updateName, label = { Text("Nama Kendaraan *") }, placeholder = { Text("Contoh: Motor Harian") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Text("Tipe Kendaraan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Row(modifier = Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VehicleType.entries.forEach { type ->
                    Row(modifier = Modifier.selectable(selected = state.type == type, onClick = { viewModel.updateType(type) }, role = Role.RadioButton), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = state.type == type, onClick = null)
                        Text(type.displayName, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            OutlinedTextField(value = state.brand, onValueChange = viewModel::updateBrand, label = { Text("Merk *") }, placeholder = { Text("Contoh: Honda, Toyota") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = state.model, onValueChange = viewModel::updateModel, label = { Text("Model *") }, placeholder = { Text("Contoh: Vario, Agya") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = state.year, onValueChange = viewModel::updateYear, label = { Text("Tahun *") }, placeholder = { Text("Contoh: 2023") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            OutlinedTextField(value = state.licensePlate, onValueChange = viewModel::updateLicensePlate, label = { Text("Plat Nomor (Opsional)") }, placeholder = { Text("Contoh: B 1234 ABC") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = state.currentKm, onValueChange = viewModel::updateCurrentKm, label = { Text("Kilometer Saat Ini *") }, placeholder = { Text("Contoh: 15000") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text("km") }, singleLine = true)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.saveVehicle() }, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading) {
                Text(if (state.isLoading) "Menyimpan..." else "Simpan Kendaraan", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}