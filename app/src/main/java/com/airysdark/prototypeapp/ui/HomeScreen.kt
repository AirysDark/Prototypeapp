package com.airysdark.prototypeapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airysdark.prototypeapp.data.Profile
import com.airysdark.prototypeapp.networking.NearbyPeer

private enum class AppSection(val label: String) {
    Discover("Discover"),
    Chats("Chats"),
    Requests("Requests"),
    Profile("Profile"),
    Settings("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    initialProfile: Profile,
    pseudonymousId: String,
    nearbyPeers: List<NearbyPeer>,
    discoveryStatus: String,
    onSaveProfile: (Profile) -> Unit,
    onClearProfile: () -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(initialProfile.pseudonym) }
    var ageText by remember { mutableStateOf(initialProfile.age.toString()) }
    var bio by remember { mutableStateOf(initialProfile.bio) }
    var lookingFor by remember { mutableStateOf(initialProfile.lookingFor) }
    var discoverable by remember { mutableStateOf(initialProfile.discoverable) }
    var status by remember { mutableStateOf("Profile is stored on this device.") }
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Delete local profile?") },
            text = { Text("This deletes the encrypted profile stored by this app. Your separate local pseudonymous ID remains on this device.") },
            confirmButton = {
                TextButton(onClick = {
                    onStopDiscovery()
                    onClearProfile()
                    name = ""
                    ageText = "18"
                    bio = ""
                    lookingFor = "Chat and meet"
                    discoverable = false
                    status = "Local profile deleted."
                    showClearDialog = false
                }) { Text("Delete profile") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }

    val section = AppSection.entries[selectedSection]
    Scaffold(
        topBar = { TopAppBar(title = { Text("Prototypeapp · ${section.label}") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.Explore, contentDescription = null) },
                    label = { Text("Discover") }
                )
                NavigationBarItem(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.Forum, contentDescription = null) },
                    label = { Text("Chats") }
                )
                NavigationBarItem(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.Favorite, contentDescription = null) },
                    label = { Text("Requests") }
                )
                NavigationBarItem(
                    selected = selectedSection == 3,
                    onClick = { selectedSection = 3 },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
                NavigationBarItem(
                    selected = selectedSection == 4,
                    onClick = { selectedSection = 4 },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (section) {
                AppSection.Discover -> {
                    Text("Meet on your terms", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                    Text("Discover nearby adults without publishing your real identity.")
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Nearby discovery", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                            Text("Discovery is opt-in. The current prototype detects temporary Bluetooth presence only; it does not yet receive profiles or support messaging.")
                            Text(discoveryStatus)
                            Button(
                                onClick = onStartDiscovery,
                                enabled = discoverable,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Start discovery") }
                            TextButton(onClick = onStopDiscovery, modifier = Modifier.fillMaxWidth()) {
                                Text("Stop discovery")
                            }
                        }
                    }
                    if (nearbyPeers.isEmpty()) {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("No nearby profiles yet", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                                Text("Turn on visibility in Profile, then start discovery. Nearby devices currently appear as temporary device entries, not completed dating profiles.")
                            }
                        }
                    } else {
                        Text("Nearby devices · ${nearbyPeers.size}", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                        nearbyPeers.forEach { peer ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(peer.displayName, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                                    Text("Temporary ID: ${peer.ephemeralId.take(8)}…")
                                    Text(peer.transport)
                                    Text("Profile exchange is not implemented yet.")
                                }
                            }
                        }
                    }
                }

                AppSection.Chats -> {
                    Text("Chats", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                    Text("Your private conversations will live here.")
                    EmptyFeatureCard(
                        title = "No conversations yet",
                        body = "Messaging is not connected yet. The next step is a consent-based peer connection, followed by encrypted local chat. No fake conversations are shown."
                    )
                }

                AppSection.Requests -> {
                    Text("Connection requests", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                    Text("Both people must agree before profile details are exchanged.")
                    EmptyFeatureCard(
                        title = "No requests yet",
                        body = "Incoming and outgoing request handling is not implemented yet. This screen is the home for accept, decline, cancel, and timeout states."
                    )
                }

                AppSection.Profile -> {
                    Text("Your profile", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                    Text("Use a pseudonym. Only share details you want other people to see.")
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(32) },
                        label = { Text("Display name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it.filter(Char::isDigit).take(3) },
                        label = { Text("Age (18+)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it.take(240) },
                        label = { Text("Bio") },
                        supportingText = { Text("${bio.length}/240 characters") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lookingFor,
                        onValueChange = { lookingFor = it.take(80) },
                        label = { Text("Looking for") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Preview", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                            Text(name.trim().ifEmpty { "Your display name" }, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                            Text(ageText.toIntOrNull()?.takeIf { it in 18..120 }?.let { "Age $it" } ?: "Enter a valid age")
                            Text(bio.trim().ifEmpty { "Your bio will appear here." })
                            Text("Looking for: ${lookingFor.trim().ifEmpty { "Not specified" }}")
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = discoverable, onCheckedChange = {
                            discoverable = it
                            if (!it) {
                                onStopDiscovery()
                                val safeAge = ageText.toIntOrNull()?.coerceIn(18, 120) ?: initialProfile.age
                                onSaveProfile(Profile(name.trim(), safeAge, bio.trim(), lookingFor.trim(), false))
                                status = "Nearby visibility disabled and saved."
                            }
                        })
                        Column {
                            Text("Allow nearby discovery")
                            Text("Off by default. Turning this off stops discovery.")
                        }
                    }
                    Button(onClick = {
                        val age = ageText.toIntOrNull()
                        when {
                            name.trim().isEmpty() -> status = "Choose a display name before saving."
                            age == null || age !in 18..120 -> status = "Enter an age between 18 and 120."
                            else -> {
                                onSaveProfile(Profile(name.trim(), age, bio.trim(), lookingFor.trim(), discoverable))
                                status = "Profile saved encrypted on this device."
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
                    Text(status)
                }

                AppSection.Settings -> {
                    Text("Privacy & settings", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Local identity", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                            Text("Pseudonymous ID: $pseudonymousId")
                            Text("Your profile is stored encrypted on this device.")
                        }
                    }
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Privacy controls", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                            Text("Nearby visibility: ${if (discoverable) "On" else "Off"}")
                            Text("Block/report, data export, and conversation deletion controls still need implementation.")
                            Button(onClick = {
                                discoverable = false
                                onStopDiscovery()
                                val safeAge = ageText.toIntOrNull()?.coerceIn(18, 120) ?: initialProfile.age
                                onSaveProfile(Profile(name.trim(), safeAge, bio.trim(), lookingFor.trim(), false))
                                status = "Nearby visibility disabled and saved."
                            }, modifier = Modifier.fillMaxWidth()) { Text("Turn off nearby visibility") }
                        }
                    }
                    HorizontalDivider()
                    Text("Local profile management", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Text("Local ID prefix: ${pseudonymousId.take(8)}…")
                    TextButton(onClick = { showClearDialog = true }) { Text("Delete local profile") }
                    Text(status)
                }
            }
        }
    }
}

@Composable
private fun EmptyFeatureCard(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text(body)
        }
    }
}
