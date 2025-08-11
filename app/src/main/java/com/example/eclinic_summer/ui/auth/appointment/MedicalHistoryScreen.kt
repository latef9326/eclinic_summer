package com.example.eclinic_summer.ui.auth.appointment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eclinic_summer.viewmodel.MedicalHistoryItem
import com.example.eclinic_summer.viewmodel.MedicalHistoryViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(
    navController: NavController,
    patientId: String,
    viewModel: MedicalHistoryViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedAppointment by viewModel.selectedAppointment.collectAsState()

    var currentFilter by remember { mutableStateOf("all") }
    var currentSort by remember { mutableStateOf("newest") }
    var searchQuery by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(patientId) {
        viewModel.loadMedicalHistory(patientId)
    }

    LaunchedEffect(searchQuery) {
        viewModel.setSearchQuery(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (selectedAppointment != null) {
            AppointmentDetailScreen(
                item = selectedAppointment!!,
                onBack = { viewModel.clearSelection() },
                onDownloadDocument = { url ->
                    // TODO: Implement document download
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Search and Filter UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search") },
                        leadingIcon = { Icon(Icons.Filled.Search, "Search") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterMenu(
                        currentFilter = currentFilter,
                        currentSort = currentSort,
                        onFilterSelected = { filter ->
                            currentFilter = filter
                            viewModel.setFilter(filter)
                        },
                        onSortSelected = { sort ->
                            currentSort = sort
                            viewModel.setSortOrder(sort)
                        }
                    )
                }

                // Content
                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }

                    error != null -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { Text(error!!, color = MaterialTheme.colorScheme.error) }

                    appointments.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { Text("No medical history found") }

                    else -> MedicalHistoryList(
                        appointments = appointments,
                        onItemClick = viewModel::selectAppointment
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicalHistoryList(
    appointments: List<MedicalHistoryItem>,
    onItemClick: (MedicalHistoryItem) -> Unit
) {
    // POPRAWIONA LAZYCOLUMN - UŻYCIE verticalArrangement DLA ODSTĘPÓW
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(appointments) { item ->
            MedicalHistoryItemCard(item, onItemClick)
        }
    }
}

@Composable
private fun MedicalHistoryItemCard(
    item: MedicalHistoryItem,
    onClick: (MedicalHistoryItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.date} • ${item.time}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = when (item.status) {
                        "completed" -> "Completed"
                        "cancelled" -> "Cancelled"
                        else -> item.status
                    },
                    color = when (item.status) {
                        "completed" -> MaterialTheme.colorScheme.primary
                        "cancelled" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.doctorName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = item.doctorSpecialization,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Type: ${item.consultationType}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AppointmentDetailScreen(
    item: MedicalHistoryItem,
    onBack: () -> Unit,
    onDownloadDocument: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, "Back")
        }

        Text(
            text = "Consultation Details",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Appointment Info
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${item.date} • ${item.time}",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.doctorName,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = item.doctorSpecialization,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Status: ${item.status.replaceFirstChar { it.uppercase() }}",
                    color = when (item.status) {
                        "completed" -> MaterialTheme.colorScheme.primary
                        "cancelled" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = "Type: ${item.consultationType}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Doctor Notes
        Text(
            text = "Doctor's Notes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.doctorNotes ?: "No notes provided",
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Documents Section
        Text(
            text = "Documents",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column {
            item.prescriptionUrl?.let { url ->
                DocumentItem("Prescription", url, onDownloadDocument)
            }

            item.testResultsUrl?.let { url ->
                DocumentItem("Test Results", url, onDownloadDocument)
            }

            if (item.prescriptionUrl == null && item.testResultsUrl == null) {
                Text("No documents available")
            }
        }
    }
}

@Composable
private fun DocumentItem(
    name: String,
    url: String,
    onDownload: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name)
            Button(onClick = { onDownload(url) }) {
                Text("Download")
            }
        }
    }
}

@Composable
private fun FilterMenu(
    currentFilter: String,
    currentSort: String,
    onFilterSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Filter and sort"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Filter section
            DropdownMenuItem(
                text = { Text("All Appointments") },
                onClick = {
                    onFilterSelected("all")
                    expanded = false
                },
                enabled = currentFilter != "all"
            )
            DropdownMenuItem(
                text = { Text("Completed") },
                onClick = {
                    onFilterSelected("completed")
                    expanded = false
                },
                enabled = currentFilter != "completed"
            )
            DropdownMenuItem(
                text = { Text("Cancelled") },
                onClick = {
                    onFilterSelected("cancelled")
                    expanded = false
                },
                enabled = currentFilter != "cancelled"
            )
            DropdownMenuItem(
                text = { Text("E-Consultations") },
                onClick = {
                    onFilterSelected("e-consultation")
                    expanded = false
                },
                enabled = currentFilter != "e-consultation"
            )
            DropdownMenuItem(
                text = { Text("In-Person") },
                onClick = {
                    onFilterSelected("in-person")
                    expanded = false
                },
                enabled = currentFilter != "in-person"
            )

            Divider()

            // Sort section
            DropdownMenuItem(
                text = { Text("Sort: Newest First") },
                onClick = {
                    onSortSelected("newest")
                    expanded = false
                },
                enabled = currentSort != "newest"
            )
            DropdownMenuItem(
                text = { Text("Sort: Oldest First") },
                onClick = {
                    onSortSelected("oldest")
                    expanded = false
                },
                enabled = currentSort != "oldest"
            )
        }
    }
}