package id.alpine12.vehiclecare.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.alpine12.vehiclecare.R


@Composable
fun DashboardScreen() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            text = "Welcome back, Alex",
            modifier = Modifier,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface)

        Text(text = "Your garage is currently in peak condition",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_airport_shuttle_24),
                        contentDescription = "Vehicle Icon",
                        modifier = Modifier.background(color = MaterialTheme.colorScheme.primary).padding(4.dp).size(24.dp),
                    )

                    Text(text = "Inventory",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Preview(
    name = "DashboardScreen", showSystemUi = true, showBackground = false,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun DashboardScreenPreview() {
//    DashboardScreen()
}