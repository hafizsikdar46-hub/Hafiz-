package com.example.scanner

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.model.SignalCategory
import com.example.model.WifiNetworkInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed interface ScanOutcome {
    data class Success(
        val networks: List<WifiNetworkInfo>,
        val isFresh: Boolean,
        val isThrottled: Boolean
    ) : ScanOutcome

    data class Error(
        val errorType: ScanErrorType,
        val message: String,
        val detail: String,
        val actionType: ErrorActionType
    ) : ScanOutcome
}

enum class ScanErrorType {
    WIFI_DISABLED,
    LOCATION_DISABLED,
    PERMISSION_MISSING,
    SCAN_FAILED
}

enum class ErrorActionType {
    ENABLE_WIFI,
    ENABLE_LOCATION,
    REQUEST_PERMISSIONS,
    RETRY
}

class WifiScanner(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val locationManager = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun isWifiEnabled(): Boolean {
        return wifiManager?.isWifiEnabled == true
    }

    fun isLocationEnabled(): Boolean {
        return locationManager?.let { LocationManagerCompat.isLocationEnabled(it) } ?: false
    }

    fun hasRequiredPermissions(): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasNearby = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return (hasFine || hasCoarse) && hasNearby
    }

    suspend fun performScan(): ScanOutcome = withContext(Dispatchers.IO) {
        val wm = wifiManager
            ?: return@withContext ScanOutcome.Error(
                errorType = ScanErrorType.SCAN_FAILED,
                message = "Wi-Fi Hardware Unavailable",
                detail = "This device does not report a functional Wi-Fi subsystem.",
                actionType = ErrorActionType.RETRY
            )

        // 1. Check Wi-Fi state
        if (!wm.isWifiEnabled) {
            return@withContext ScanOutcome.Error(
                errorType = ScanErrorType.WIFI_DISABLED,
                message = "Wi-Fi is Turned Off 📵",
                detail = "Wi-Fi radio is currently disabled. Please turn on Wi-Fi so we can scan nearby router signals.",
                actionType = ErrorActionType.ENABLE_WIFI
            )
        }

        // 2. Check Permissions
        if (!hasRequiredPermissions()) {
            return@withContext ScanOutcome.Error(
                errorType = ScanErrorType.PERMISSION_MISSING,
                message = "Permission Needed 🛡️",
                detail = "Android requires Location permission to detect nearby Wi-Fi router beacons.",
                actionType = ErrorActionType.REQUEST_PERMISSIONS
            )
        }

        // 3. Check Location Services
        if (!isLocationEnabled()) {
            return@withContext ScanOutcome.Error(
                errorType = ScanErrorType.LOCATION_DISABLED,
                message = "Location Service is Off 📍",
                detail = "Android system policy requires device Location to be turned ON to deliver Wi-Fi scan results to apps.",
                actionType = ErrorActionType.ENABLE_LOCATION
            )
        }

        // 4. Initiate Wi-Fi Scan
        var isFresh = false
        var isThrottled = false

        try {
            val startSuccess = try {
                @Suppress("DEPRECATION")
                wm.startScan()
            } catch (e: Exception) {
                false
            }

            if (startSuccess) {
                // Wait for SCAN_RESULTS_AVAILABLE_ACTION broadcast with a 6-second timeout
                val broadcastResult = withTimeoutOrNull(6000L) {
                    suspendCancellableCoroutine { continuation ->
                        val receiver = object : BroadcastReceiver() {
                            override fun onReceive(c: Context?, intent: Intent?) {
                                if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                                    val updated = intent.getBooleanExtra(
                                        WifiManager.EXTRA_RESULTS_UPDATED,
                                        false
                                    )
                                    try {
                                        context.applicationContext.unregisterReceiver(this)
                                    } catch (_: Exception) {}

                                    if (continuation.isActive) {
                                        continuation.resume(updated)
                                    }
                                }
                            }
                        }

                        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                        ContextCompat.registerReceiver(
                            context.applicationContext,
                            receiver,
                            filter,
                            ContextCompat.RECEIVER_EXPORTED
                        )

                        continuation.invokeOnCancellation {
                            try {
                                context.applicationContext.unregisterReceiver(receiver)
                            } catch (_: Exception) {}
                        }
                    }
                }

                if (broadcastResult == true) {
                    isFresh = true
                } else {
                    isThrottled = true
                }
            } else {
                // startScan returned false (throttled by Android 9+ or pending scan)
                isThrottled = true
            }

            // Read the real scan results from WifiManager
            @Suppress("DEPRECATION")
            val rawResults: List<ScanResult> = try {
                wm.scanResults ?: emptyList()
            } catch (secEx: SecurityException) {
                return@withContext ScanOutcome.Error(
                    errorType = ScanErrorType.PERMISSION_MISSING,
                    message = "Permission Denied by System 🔒",
                    detail = secEx.localizedMessage ?: "Unable to read scan results due to security policy.",
                    actionType = ErrorActionType.REQUEST_PERMISSIONS
                )
            }

            val parsedNetworks = rawResults.map { raw ->
                val ssid = raw.SSID ?: ""
                val bssid = raw.BSSID ?: ""
                val maskedBssid = maskBssid(bssid)
                val rssi = raw.level
                val freq = raw.frequency
                val band = when {
                    freq in 2400..2499 -> "2.4 GHz"
                    freq in 4900..5899 -> "5 GHz"
                    freq in 5900..7100 -> "6 GHz"
                    else -> "${freq}MHz"
                }
                val signalCategory = when {
                    rssi >= -65 -> SignalCategory.STRONG
                    rssi in -79..-66 -> SignalCategory.MODERATE
                    else -> SignalCategory.WEAK
                }
                val level = try {
                    @Suppress("DEPRECATION")
                    WifiManager.calculateSignalLevel(rssi, 5)
                } catch (_: Exception) {
                    2
                }

                WifiNetworkInfo(
                    ssid = ssid,
                    maskedBssid = maskedBssid,
                    rssi = rssi,
                    frequencyMhz = freq,
                    signalLevel = level,
                    bandLabel = band,
                    category = signalCategory
                )
            }.sortedByDescending { it.rssi }

            ScanOutcome.Success(
                networks = parsedNetworks,
                isFresh = isFresh,
                isThrottled = isThrottled
            )
        } catch (e: Exception) {
            ScanOutcome.Error(
                errorType = ScanErrorType.SCAN_FAILED,
                message = "Scan Encountered an Error",
                detail = e.localizedMessage ?: "Unknown hardware scan failure.",
                actionType = ErrorActionType.RETRY
            )
        }
    }

    private fun maskBssid(bssid: String): String {
        if (bssid.isBlank()) return "Unknown BSSID"
        val parts = bssid.split(":")
        return if (parts.size >= 6) {
            "${parts[0]}:${parts[1]}:${parts[2]}:**:**:${parts[5]}"
        } else {
            bssid.take(6) + "**"
        }
    }
}
