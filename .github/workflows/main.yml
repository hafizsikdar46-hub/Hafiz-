package com.phonedoctor.checker

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.*
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    // ------------------------------------------------------------
    // DATA MODEL
    // ------------------------------------------------------------

    enum class Status {
        PASS,
        FAIL,
        UNVERIFIED,
        INFO
    }

    data class Result(
        val name: String,
        val status: Status,
        val detail: String
    )

    // ------------------------------------------------------------
    // STATE
    // ------------------------------------------------------------

    private val results = mutableStateListOf<Result>()

    private var scanning by mutableStateOf(true)
    private var progress by mutableStateOf(0f)

    // ------------------------------------------------------------
    // PERMISSIONS
    // ------------------------------------------------------------

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            startAutomaticScan()
        }

    // ------------------------------------------------------------
    // ACTIVITY START
    // ------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PhoneDoctorUI()
        }

        requestRequiredPermissions()
    }

    // ------------------------------------------------------------
    // PERMISSION REQUEST
    // ------------------------------------------------------------

    private fun requestRequiredPermissions() {

        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.add(Manifest.permission.CAMERA)
            permissions.add(Manifest.permission.RECORD_AUDIO)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val needed = permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        } else {
            startAutomaticScan()
        }
    }

    // ------------------------------------------------------------
    // AUTOMATIC FULL SCAN
    // ------------------------------------------------------------

    private fun startAutomaticScan() {

        if (!scanning) return

        results.clear()
        scanning = true
        progress = 0f

        Thread {

            val tests = listOf(
                { checkDeviceInformation() },
                { checkBattery() },
                { checkRAM() },
                { checkStorage() },
                { checkWiFi() },
                { checkBluetooth() },
                { checkNetwork() },
                { checkGPS() },
                { checkCamera() },
                { checkFlash() },
                { checkSensors() },
                { checkBiometric() },
                { checkDisplay() },
                { checkTouch() },
                { checkSpeakerPath() },
                { checkMicrophone() },
                { checkVibration() }
            )

            tests.forEachIndexed { index, test ->

                try {
                    test()
                } catch (e: Exception) {

                    addResult(
                        Result(
                            "Test ${index + 1}",
                            Status.UNVERIFIED,
                            "Could not verify: ${e.message ?: "Unknown error"}"
                        )
                    )
                }

                progress =
                    ((index + 1).toFloat() / tests.size.toFloat())

                Thread.sleep(100)
            }

            runOnUiThread {
                scanning = false
                progress = 1f
            }

        }.start()
    }

    // ------------------------------------------------------------
    // ADD RESULT
    // ------------------------------------------------------------

    private fun addResult(result: Result) {

        runOnUiThread {
            results.add(result)
        }
    }

    // ------------------------------------------------------------
    // DEVICE INFORMATION
    // ------------------------------------------------------------

    private fun checkDeviceInformation() {

        addResult(
            Result(
                "Brand",
                Status.INFO,
                Build.BRAND
            )
        )

        addResult(
            Result(
                "Model",
                Status.INFO,
                Build.MODEL
            )
        )

        addResult(
            Result(
                "Device",
                Status.INFO,
                Build.DEVICE
            )
        )

        addResult(
            Result(
                "Android",
                Status.INFO,
                "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
            )
        )

        addResult(
            Result(
                "CPU",
                Status.INFO,
                "${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"} • ${Runtime.getRuntime().availableProcessors()} cores"
            )
        )

        checkIMEI()
    }

    // ------------------------------------------------------------
    // IMEI
    // ------------------------------------------------------------

    private fun checkIMEI() {

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {

            addResult(
                Result(
                    "IMEI",
                    Status.INFO,
                    "Restricted by Android on most modern devices"
                )
            )

            return
        }

        try {

            if (
                checkSelfPermission(
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                addResult(
                    Result(
                        "IMEI",
                        Status.INFO,
                        "Permission not granted"
                    )
                )

                return
            }

            val telephony =
                getSystemService(
                    TELEPHONY_SERVICE
                ) as TelephonyManager

            @Suppress("DEPRECATION")
            val imei = telephony.deviceId

            addResult(
                Result(
                    "IMEI",
                    Status.INFO,
                    imei ?: "Unavailable"
                )
            )

        } catch (e: Exception) {

            addResult(
                Result(
                    "IMEI",
                    Status.INFO,
                    "Unavailable"
                )
            )
        }
    }

    // ------------------------------------------------------------
    // BATTERY
    // ------------------------------------------------------------

    private fun checkBattery() {

        val intent =
            registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

        if (intent == null) {

            addResult(
                Result(
                    "Battery",
                    Status.UNVERIFIED,
                    "Battery information unavailable"
                )
            )

            return
        }

        val level =
            intent.getIntExtra(
                BatteryManager.EXTRA_LEVEL,
                -1
            )

        val scale =
            intent.getIntExtra(
                BatteryManager.EXTRA_SCALE,
                -1
            )

        val temperature =
            intent.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE,
                -1
            )

        val voltage =
            intent.getIntExtra(
                BatteryManager.EXTRA_VOLTAGE,
                -1
            )

        val status =
            intent.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                -1
            )

        val percentage =
            if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else {
                -1
            }

        val temp =
            if (temperature >= 0) {
                temperature / 10f
            } else {
                null
            }

        val charging =
            when (status) {

                BatteryManager.BATTERY_STATUS_CHARGING ->
                    "Charging"

                BatteryManager.BATTERY_STATUS_FULL ->
                    "Full"

                BatteryManager.BATTERY_STATUS_DISCHARGING ->
                    "Discharging"

                BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                    "Not charging"

                else ->
                    "Unknown"
            }

        val text = buildString {

            append(
                if (percentage >= 0)
                    "$percentage%"
                else
                    "Unknown"
            )

            append(" • $charging")

            if (temp != null) {
                append(" • ${"%.1f".format(Locale.US, temp)}°C")
            }

            if (voltage > 0) {
                append(" • ${voltage}mV")
            }

            append(
                " • Battery capacity health: not available from standard Android API"
            )
        }

        addResult(
            Result(
                "Battery",
                Status.INFO,
                text
            )
        )
    }

    // ------------------------------------------------------------
    // RAM
    // ------------------------------------------------------------

    private fun checkRAM() {

        val manager =
            getSystemService(
                ACTIVITY_SERVICE
            ) as ActivityManager

        val memoryInfo =
            ActivityManager.MemoryInfo()

        manager.getMemoryInfo(memoryInfo)

        addResult(
            Result(
                "RAM",
                Status.INFO,
                "Total ${formatBytes(memoryInfo.totalMem)} • " +
                        "Available ${formatBytes(memoryInfo.availMem)}"
            )
        )
    }

    // ------------------------------------------------------------
    // STORAGE
    // ------------------------------------------------------------

    private fun checkStorage() {

        val path =
            Environment.getDataDirectory()

        val stat =
            android.os.StatFs(path.path)

        val total =
            stat.totalBytes

        val free =
            stat.availableBytes

        val used =
            total - free

        addResult(
            Result(
                "Storage",
                Status.INFO,
                "Total ${formatBytes(total)} • " +
                        "Used ${formatBytes(used)} • " +
                        "Free ${formatBytes(free)}"
            )
        )
    }

    // ------------------------------------------------------------
    // WIFI
    // ------------------------------------------------------------

    private fun checkWiFi() {

        try {

            val connectivity =
                getSystemService(
                    CONNECTIVITY_SERVICE
                ) as ConnectivityManager

            val network =
                connectivity.activeNetwork

            if (network == null) {

                addResult(
                    Result(
                        "Wi-Fi",
                        Status.UNVERIFIED,
                        "No active network connection"
                    )
                )

                return
            }

            val capabilities =
                connectivity.getNetworkCapabilities(network)

            val wifi =
                capabilities?.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                ) == true

            if (wifi) {

                val validated =
                    capabilities?.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    ) == true

                addResult(
                    Result(
                        "Wi-Fi",
                        if (validated)
                            Status.PASS
                        else
                            Status.UNVERIFIED,
                        if (validated)
                            "Connected and internet validated"
                        else
                            "Connected to Wi-Fi; internet validation unavailable"
                    )
                )

            } else {

                addResult(
                    Result(
                        "Wi-Fi",
                        Status.UNVERIFIED,
                        "Wi-Fi is not the active network"
                    )
                )
            }

        } catch (e: Exception) {

            addResult(
                Result(
                    "Wi-Fi",
                    Status.UNVERIFIED,
                    "Unable to verify"
                )
            )
        }
    }

    // ------------------------------------------------------------
    // BLUETOOTH
    // ------------------------------------------------------------

    private fun checkBluetooth() {

        try {

            val manager =
                getSystemService(
                    BLUETOOTH_SERVICE
                ) as BluetoothManager

            val adapter =
                manager.adapter

            if (adapter == null) {

                addResult(
                    Result(
                        "Bluetooth",
                        Status.FAIL,
                        "Bluetooth adapter unavailable"
                    )
                )

                return
            }

            val enabled =
                try {
                    adapter.isEnabled
                } catch (_: SecurityException) {
                    false
                }

            if (enabled) {

                addResult(
                    Result(
                        "Bluetooth",
                        Status.PASS,
                        "Bluetooth adapter is available and enabled"
                    )
                )

            } else {

                addResult(
                    Result(
                        "Bluetooth",
                        Status.UNVERIFIED,
                        "Bluetooth is disabled; radio communication not tested"
                    )
                )
            }

        } catch (e: Exception) {

            addResult(
                Result(
                    "Bluetooth",
                    Status.UNVERIFIED,
                    "Unable to verify"
                )
            )
        }
    }

    // ------------------------------------------------------------
    // MOBILE NETWORK / SIM
    // ------------------------------------------------------------

    private fun checkNetwork() {

        try {

            val telephony =
                getSystemService(
                    TELEPHONY_SERVICE
                ) as TelephonyManager

            val simState =
                telephony.simState

            val simText =
                when (simState) {

                    TelephonyManager.SIM_STATE_READY ->
                        "SIM ready"

                    TelephonyManager.SIM_STATE_ABSENT ->
                        "No SIM inserted"

                    TelephonyManager.SIM_STATE_NETWORK_LOCKED ->
                        "Network locked"

                    TelephonyManager.SIM_STATE_PIN_REQUIRED ->
                        "PIN required"

                    TelephonyManager.SIM_STATE_PUK_REQUIRED ->
                        "PUK required"

                    else ->
                        "SIM state unavailable"
                }

            addResult(
                Result(
                    "SIM / Network",
                    Status.INFO,
                    simText
                )
            )

            val networkType =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    telephony.dataNetworkType.toString()

                } else {

                    @Suppress("DEPRECATION")
                    telephony.networkType.toString()
                }

            addResult(
                Result(
                    "Mobile Network",
                    Status.INFO,
                    "Network type code: $networkType"
                )
            )

        } catch (e: Exception) {

            addResult(
                Result(
                    "SIM / Network",
                    Status.UNVERIFIED,
                    "Unable to read network information"
                )
            )
        }
    }

    // ------------------------------------------------------------
    // GPS
    // ------------------------------------------------------------

    private fun checkGPS() {

        try {

            val locationManager =
                getSystemService(
                    LOCATION_SERVICE
                ) as LocationManager

            val gpsEnabled =
                locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
                )

            if (gpsEnabled) {

                addResult(
                    Result(
                        "GPS",
                        Status.UNVERIFIED,
                        "GPS provider enabled; satellite reception requires live GNSS test"
                    )
                )

            } else {

                addResult(
                    Result(
                        "GPS",
                        Status.UNVERIFIED,
                        "GPS provider disabled"
                    )
                )
            }

        } catch (e: Exception) {

            addResult(
                Result(
                    "GPS",
                    Status.UNVERIFIED,
                    "Unable to verify GPS"
                )
            )
        }
    }

    // ------------------------------------------------------------
    // CAMERA
    // -------------------
