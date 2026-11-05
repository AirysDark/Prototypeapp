package com.airysdark.prototypeapp.discovery

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import com.airysdark.prototypeapp.networking.NearbyPeer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.UUID

/**
 * BLE presence only. Advertisements contain an app service-data UUID and an 8-byte random,
 * rotating token; they never contain profile data, a permanent identity, or GPS.
 * This is discovery, not authentication or a private messaging channel.
 */
class BleNearbyDiscovery(context: Context) : NearbyDiscovery {
    private val appContext = context.applicationContext
    private val adapter: BluetoothAdapter? =
        appContext.getSystemService(BluetoothManager::class.java)?.adapter
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val random = SecureRandom()
    private val serviceUuid = ParcelUuid(UUID.fromString("a28f3b21-5d6e-4a70-9b31-7c4a2f8e6101"))
    private val mutablePeers = MutableStateFlow<List<NearbyPeer>>(emptyList())
    val nearbyPeers: StateFlow<List<NearbyPeer>> = mutablePeers.asStateFlow()
    private val lastSeen = mutableMapOf<String, Long>()
    private var scanner: BluetoothLeScanner? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var scanning = false
    private var advertising = false
    private var rotationJob: kotlinx.coroutines.Job? = null
    private var cleanupJob: kotlinx.coroutines.Job? = null
    private var currentToken: ByteArray = newToken()

    override val peers: StateFlow<List<NearbyPeer>> = mutablePeers

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val token = result.scanRecord?.getServiceData(serviceUuid) ?: return
            if (token.size != TOKEN_BYTES) return
            val id = token.toHex()
            synchronized(lastSeen) {
                lastSeen[id] = System.currentTimeMillis()
                mutablePeers.value = lastSeen.keys.map { key ->
                    NearbyPeer(
                        ephemeralId = key,
                        displayName = "Nearby member",
                        approximateDistanceMeters = null,
                        transport = "Bluetooth LE"
                    )
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // Keep the failure observable through start() or the UI's status message.
            scanning = false
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            advertising = true
        }

        override fun onStartFailure(errorCode: Int) {
            advertising = false
        }
    }

    override suspend fun start(): Result<Unit> {
        val bluetooth = adapter ?: return Result.failure(
            IllegalStateException("This device does not support Bluetooth")
        )
        if (!bluetooth.isEnabled) return Result.failure(
            IllegalStateException("Turn Bluetooth on to discover nearby members")
        )
        if (!hasRequiredPermissions()) return Result.failure(
            SecurityException("Bluetooth permissions have not been granted")
        )
        val localScanner = bluetooth.bluetoothLeScanner
            ?: return Result.failure(IllegalStateException("BLE scanning is unavailable"))
        val localAdvertiser = bluetooth.bluetoothLeAdvertiser
            ?: return Result.failure(IllegalStateException("BLE advertising is unavailable"))

        return runCatching {
            scanner = localScanner
            advertiser = localAdvertiser
            // Service data embeds the custom 128-bit UUID; filtering by advertised service UUID
            // would miss devices that fit the UUID only in the service-data structure.
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()
            localScanner.startScan(null, settings, scanCallback)
            scanning = true
            startAdvertising(localAdvertiser)
            rotationJob?.cancel()
            rotationJob = scope.launch {
                while (isActive) {
                    delay(ROTATE_INTERVAL_MS)
                    if (advertising) {
                        stopAdvertisingSafely()
                        currentToken = newToken()
                        advertiser?.let { startAdvertising(it) }
                    }
                }
            }
            cleanupJob?.cancel()
            cleanupJob = scope.launch {
                while (isActive) {
                    delay(CLEANUP_INTERVAL_MS)
                    val cutoff = System.currentTimeMillis() - PEER_TTL_MS
                    synchronized(lastSeen) {
                        lastSeen.entries.removeAll { it.value < cutoff }
                        mutablePeers.value = lastSeen.keys.map { key ->
                            NearbyPeer(key, "Nearby member", null, "Bluetooth LE")
                        }
                    }
                }
            }
        }.map { Unit }
    }

    override suspend fun stop() {
        rotationJob?.cancel()
        cleanupJob?.cancel()
        if (hasRequiredPermissions()) {
            runCatching { if (scanning) scanner?.stopScan(scanCallback) }
            stopAdvertisingSafely()
        }
        scanning = false
        advertising = false
        scanner = null
        advertiser = null
        synchronized(lastSeen) {
            lastSeen.clear()
            mutablePeers.value = emptyList()
        }
    }

    fun close() {
        scope.cancel()
    }

    private fun startAdvertising(target: BluetoothLeAdvertiser) {
        if (!hasRequiredPermissions()) return
        // Do not also add the UUID list: repeating a 128-bit UUID can exceed the 31-byte
        // legacy advertising payload. The service-data field itself carries the UUID + token.
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceData(serviceUuid, currentToken)
            .build()
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .build()
        target.startAdvertising(settings, data, advertiseCallback)
    }

    private fun stopAdvertisingSafely() {
        if (hasRequiredPermissions()) runCatching { advertiser?.stopAdvertising(advertiseCallback) }
        advertising = false
    }

    private fun hasRequiredPermissions(): Boolean {
        val required = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return required.all {
            ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun newToken() = ByteArray(TOKEN_BYTES).also(random::nextBytes)

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }

    companion object {
        private const val TOKEN_BYTES = 8
        private const val ROTATE_INTERVAL_MS = 15 * 60 * 1000L
        private const val CLEANUP_INTERVAL_MS = 15_000L
        private const val PEER_TTL_MS = 75_000L
    }
}
