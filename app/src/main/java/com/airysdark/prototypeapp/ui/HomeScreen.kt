package com.airysdark.prototypeapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(initialProfile: Profile, pseudonymousId: String, onSaveProfile: (Profile) -> Unit) {
    var name by remember { mutableStateOf(initialProfile.pseudonym) }
    var ageText by remember { mutableStateOf(initialProfile.age.toString()) }
    var bio by remember { mutableStateOf(initialProfile.bio) }
    var lookingFor by remember { mutableStateOf(initialProfile.lookingFor) }
    var discoverable by remember { mutableStateOf(initialProfile.discoverable) }
    var status by remember { mutableStateOf("Profile is stored on this device.") }

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
                    Text("Your profile is encrypted and saved locally. Nearby discovery is not active in this prototype.")
                    Text("Local ID: ${pseudonymousId.take(8)}…")
                }
            }
            HorizontalDivider()
            Text("Your profile", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = name, onValueChange = { name = it.take(32) },
                label = { Text("Display name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ageText, onValueChange = { ageText = it.filter(Char::isDigit).take(3) },
                label = { Text("Age (18+)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = bio, onValueChange = { bio = it.take(240) },
                label = { Text("Bio") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lookingFor, onValueChange = { lookingFor = it.take(80) },
                label = { Text("Looking for") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = discoverable, onCheckedChange = { discoverable = it })
                Column {
                    Text("Opt into discovery")
                    Text("Preference only; broadcasting is not implemented yet.")
                }
            }
            Button(onClick = {
                val age = ageText.toIntOrNull()
                if (age == null || age < 18 || age > 120) {
                    status = "Enter a valid age of 18 or older."
                } else {
                    onSaveProfile(Profile(name.trim(), age, bio.trim(), lookingFor.trim(), discoverable))
                    status = "Profile saved encrypted on this device."
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
            Text(status)
            HorizontalDivider()
            Text("Next: BLE discovery, peer connections, chat, block/report, and consent-based photo sharing.")
        }
    }
}
