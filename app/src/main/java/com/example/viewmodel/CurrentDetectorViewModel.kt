package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.PowerEstimate
import com.example.model.WifiNetworkInfo
import com.example.scanner.ErrorActionType
import com.example.scanner.ScanErrorType
import com.example.scanner.ScanOutcome
import com.example.scanner.WifiScanner
import com.example.util.PowerEstimator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScanState {
    data object Idle : ScanState
    data class Scanning(val stage: String) : ScanState
    data class Success(
        val estimate: PowerEstimate,
        val networks: List<WifiNetworkInfo>,
        val isThrottled: Boolean
    ) : ScanState
    data class Error(
        val errorType: ScanErrorType,
        val title: String,
        val detail: String,
        val actionType: ErrorActionType
    ) : ScanState
}

data class CurrentDetectorUiState(
    val scanState: ScanState = ScanState.Idle,
    val isScanning: Boolean = false,
    val showTechnicalDetails: Boolean = false,
    val showNetworksList: Boolean = false,
    val isWifiEnabled: Boolean = true,
    val isLocationEnabled: Boolean = true,
    val hasPermissions: Boolean = false,
    val scanCount: Int = 0
)

class CurrentDetectorViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = WifiScanner(application)

    private val _uiState = MutableStateFlow(CurrentDetectorUiState())
    val uiState: StateFlow<CurrentDetectorUiState> = _uiState.asStateFlow()

    init {
        refreshHardwareState()
    }

    fun refreshHardwareState() {
        _uiState.update {
            it.copy(
                isWifiEnabled = scanner.isWifiEnabled(),
                isLocationEnabled = scanner.isLocationEnabled(),
                hasPermissions = scanner.hasRequiredPermissions()
            )
        }
    }

    fun toggleTechnicalDetails() {
        _uiState.update { it.copy(showTechnicalDetails = !it.showTechnicalDetails) }
    }

    fun toggleNetworksList() {
        _uiState.update { it.copy(showNetworksList = !it.showNetworksList) }
    }

    fun startScan() {
        if (_uiState.value.isScanning) return

        refreshHardwareState()

        val currentHasPerm = scanner.hasRequiredPermissions()
        val currentWifiOn = scanner.isWifiEnabled()
        val currentLocOn = scanner.isLocationEnabled()

        if (!currentHasPerm) {
            _uiState.update {
                it.copy(
                    scanState = ScanState.Error(
                        errorType = ScanErrorType.PERMISSION_MISSING,
                        title = "Location Permission Required 🛡️",
                        detail = "Android requires Location permission for apps to scan nearby Wi-Fi beacons. We do not track your location.",
                        actionType = ErrorActionType.REQUEST_PERMISSIONS
                    )
                )
            }
            return
        }

        if (!currentWifiOn) {
            _uiState.update {
                it.copy(
                    scanState = ScanState.Error(
                        errorType = ScanErrorType.WIFI_DISABLED,
                        title = "Wi-Fi is Off 📵",
                        detail = "Your device Wi-Fi radio is off. Please turn on Wi-Fi so we can detect router signals.",
                        actionType = ErrorActionType.ENABLE_WIFI
                    )
                )
            }
            return
        }

        if (!currentLocOn) {
            _uiState.update {
                it.copy(
                    scanState = ScanState.Error(
                        errorType = ScanErrorType.LOCATION_DISABLED,
                        title = "Location Services Disabled 📍",
                        detail = "Android OS requires Location services (GPS toggle) to be ON to reveal Wi-Fi scan results.",
                        actionType = ErrorActionType.ENABLE_LOCATION
                    )
                )
            }
            return
        }

        val newScanCount = _uiState.value.scanCount + 1

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    scanCount = newScanCount,
                    scanState = ScanState.Scanning("Scanning 2.4 GHz & 5 GHz radio bands...")
                )
            }

            when (val outcome = scanner.performScan()) {
                is ScanOutcome.Success -> {
                    val estimate = PowerEstimator.estimate(
                        networks = outcome.networks,
                        isFreshHardwareScan = outcome.isFresh,
                        scanIndex = newScanCount
                    )

                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            scanState = ScanState.Success(
                                estimate = estimate,
                                networks = outcome.networks,
                                isThrottled = outcome.isThrottled
                            )
                        )
                    }
                }
                is ScanOutcome.Error -> {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            scanState = ScanState.Error(
                                errorType = outcome.errorType,
                                title = outcome.message,
                                detail = outcome.detail,
                                actionType = outcome.actionType
                            )
                        )
                    }
                }
            }
        }
    }
}
