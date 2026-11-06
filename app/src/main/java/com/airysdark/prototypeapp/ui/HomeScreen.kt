package com.airysdark.prototypeapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airysdark.prototypeapp.data.Profile
import com.airysdark.prototypeapp.networking.NearbyPeer

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

    Scaffold(topBar = { TopAppBar(title = { Text("Prototypeapp") }) }) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Meet on your terms", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Text("A local-first, pseudonymous space for consenting adults.")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Privacy by design", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Text("Your profile is encrypted and saved locally. Nearby discovery broadcasts only a rotating random token, never your profile.")
                    Text("Local ID: ${pseudonymousId.take(8)}…")
                }
            }
            HorizontalDivider()
            Text("Your profile", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = name, onValueChange = { name = it.take(32) },
                label = { Text("Display name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ageText, onValueChange = { ageText = it.filter(Char::isDigit).take(3) },
                label = { Text("Age (18+)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it.take(240) },
                label = { Text("Bio") },
                supportingText = { Text("${bio.length}/240 characters") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = lookingFor, onValueChange = { lookingFor = it.take(80) },
                label = { Text("Looking for") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Profile preview", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Text(name.trim().ifEmpty { "Your display name" }, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                    Text(ageText.toIntOrNull()?.takeIf { it in 18..120 }?.let { "Age $it" } ?: "Age not valid yet")
                    Text(bio.trim().ifEmpty { "Your bio will appear here." })
                    Text("Looking for: ${lookingFor.trim().ifEmpty { "Not specified" }}")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = discoverable, onCheckedChange = {
                    discoverable = it
                    if (!it) {
                        // Hiding is immediate: stop radio discovery and persist the opt-out
                        // without requiring the user to press Save separately.
                        onStopDiscovery()
                        val safeAge = ageText.toIntOrNull()?.coerceIn(18, 120) ?: initialProfile.age
                        onSaveProfile(Profile(name.trim(), safeAge, bio.trim(), lookingFor.trim(), false))
                        status = "Nearby visibility disabled and saved."
                    }
                })
                Column {
                    Text("Opt into nearby discovery")
                    Text("Your device is not discoverable unless you opt in and start it.")
                }
            }
            Button(onClick = {
                val age = ageText.toIntOrNull()
                when {
                    name.trim().isEmpty() -> status = "Choose a display name before saving your profile."
                    age == null || age < 18 || age > 120 -> status = "Enter a valid age between 18 and 120."
                    else -> {
                        onSaveProfile(Profile(name.trim(), age, bio.trim(), lookingFor.trim(), discoverable))
                        status = "Profile saved encrypted on this device."
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
            Text(status)
            TextButton(onClick = { showClearDialog = true }) {
                Text("Delete local profile")
            }
            HorizontalDivider()
            Text("Nearby discovery", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Text("BLE presence only. This does not yet exchange profiles, authenticate peers, or provide chat.")
            Button(
                onClick = onStartDiscovery,
                enabled = discoverable,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Start nearby discovery") }
            Button(onClick = onStopDiscovery, modifier = Modifier.fillMaxWidth()) {
                Text("Stop discovery")
            }
            Text(discoveryStatus)
            if (nearbyPeers.isEmpty()) {
                Text("No nearby members detected.")
            } else {
                Text("Nearby devices (${nearbyPeers.size})", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                nearbyPeers.forEach { peer ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(peer.displayName)
                            Text("Temporary ID: ${peer.ephemeralId.take(8)}…")
                            Text(peer.transport)
                        }
                    }
                }
            }
            HorizontalDivider()
            Text("Next: authenticated peer handshakes, private messaging, block/report, and consent-based photo sharing.")
        }
    }
}
