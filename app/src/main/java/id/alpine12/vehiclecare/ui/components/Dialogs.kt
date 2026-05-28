package id.alpine12.vehiclecare.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Confirmation dialog for destructive actions
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Ya",
    dismissText: String = "Batal",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Loading dialog for async operations
 */
@Composable
fun LoadingDialog(
    message: String = "Memuat...",
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss during loading */ },
        modifier = modifier,
        confirmButton = {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

/**
 * Delete confirmation dialog with preset styling
 */
@Composable
fun DeleteConfirmDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConfirmDialog(
        title = "Hapus $itemName?",
        message = "Tindakan ini tidak bisa dibatalkan. Data yang dihapus tidak akan bisa dikembalikan.",
        confirmText = "Hapus",
        dismissText = "Batal",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isDestructive = true,
        modifier = modifier
    )
}

/**
 * Success dialog
 */
@Composable
fun SuccessDialog(
    title: String = "Berhasil",
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
