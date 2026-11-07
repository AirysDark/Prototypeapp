package com.airysdark.prototypeapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.airysdark.prototypeapp.data.LocalProfileStore
import com.airysdark.prototypeapp.discovery.BleNearbyDiscovery
import com.airysdark.prototypeapp.identity.LocalIdentity
import com.airysdark.prototypeapp.ui.HomeScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var discovery: BleNearbyDiscovery
    private val discoveryStatus = mutableStateOf("Nearby discovery is off.")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileStore = LocalProfileStore(applicationContext)
        val identity = LocalIdentity(applicationContext)
        discovery = BleNearbyDiscovery(applicationContext)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            if (grants.values.all { it }) {
                startDiscovery()
            } else {
                discoveryStatus.value = "Bluetooth permission denied. Nearby discovery remains off."
            }
        }

        setContent {
            PrototypeApp {
                HomeScreen(
                    initialProfile = profileStore.load(),
                    pseudonymousId = identity.getOrCreateId(),
                    discoveryStatus = discoveryStatus.value,
                    onSaveProfile = profileStore::save,
                    onClearProfile = profileStore::clear,
                    onStartDiscovery = {
                        val required = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_ADVERTISE,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        } else {
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        val missing = required.any {
                            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
                        }
                        if (missing) permissionLauncher.launch(required) else startDiscovery()
                    },
                    onStopDiscovery = {
                        lifecycleScope.launch {
                            discovery.stop()
                            discoveryStatus.value = "Nearby discovery is off."
                        }
                    }
                )
            }
        }
    }

    private fun startDiscovery() {
        lifecycleScope.launch {
            val result = discovery.start()
            discoveryStatus.value = result.fold(
                onSuccess = { "Scanning and advertising BLE presence. Token rotates every 15 minutes." },
                onFailure = { "Could not start discovery: ${it.message ?: "unknown error"}" }
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if (::discovery.isInitialized) {
            lifecycleScope.launch {
                discovery.stop()
                discoveryStatus.value = "Discovery stopped when the app left the foreground."
            }
        }
    }

    override fun onDestroy() {
        if (::discovery.isInitialized) discovery.close()
        super.onDestroy()
    }
}
