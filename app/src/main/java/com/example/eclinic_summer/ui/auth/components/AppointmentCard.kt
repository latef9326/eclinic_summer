package com.example.eclinic_summer.ui.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eclinic_summer.data.model.repository.Availability

/**
 * Composable showing a single appointment slot with a book button.
 */
@Composable
fun AppointmentCard(
    slot: Availability,
    onBook: (Availability) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Display dayOfWeek and time range
            Text("${slot.dayOfWeek}: ${slot.startTime} - ${slot.endTime}")
            Button(onClick = { onBook(slot) }) {
                Text("Book")
            }
        }
    }
}
