package com.example.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.PowerStatus
import com.example.scanner.ErrorActionType
import com.example.ui.components.ErrorCard
import com.example.ui.components.HeuristicExplanationCard
import com.example.ui.components.LightningAnimation
import com.example.ui.components.NetworkListCard
import com.example.ui.components.RadarScanAnimation
import com.example.ui.components.ResultCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricYellow
import com.example.viewmodel.CurrentDetectorViewModel
import com.example.viewmodel.ScanState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentDetectorScreen(
    viewModel: CurrentDetectorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe lifecycle to refresh hardware states (e.g. if user toggles Wi-Fi or GPS in Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshHardwareState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission launcher for Location and Nearby Wi-Fi
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        viewModel.refreshHardwareState()
        val allGranted = results.values.all { it }
        if (allGranted) {
            viewModel.startScan()
        }
    }

    val requestPermissions = {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        permissionsLauncher.launch(perms.toTypedArray())
    }

    val handleAction: (ErrorActionType) -> Unit = { action ->
        when (action) {
            ErrorActionType.REQUEST_PERMISSIONS -> requestPermissions()
            ErrorActionType.ENABLE_WIFI -> {
                try {
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                } catch (_: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
            ErrorActionType.ENABLE_LOCATION -> {
                try {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } catch (_: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
            ErrorActionType.RETRY -> viewModel.startScan()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElectricYellow.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = ElectricYellow,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Current Detector ⚡",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val currentStatus = (uiState.scanState as? ScanState.Success)?.estimate?.status

                // Large Animated Lightning Icon
                LightningAnimation(
                    isScanning = uiState.isScanning,
                    status = currentStatus,
                    size = 110.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Subtitle tag
                Text(
                    text = "Smart Wi-Fi Electromagnetic Load Estimator",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Large "CHECK CURRENT ⚡" Button (shown when not scanning)
                if (!uiState.isScanning && uiState.scanState !is ScanState.Success) {
                    Button(
                        onClick = {
                            if (!uiState.hasPermissions) {
                                requestPermissions()
                            } else {
                                viewModel.startScan()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .testTag("check_current_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "CHECK CURRENT ⚡",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    fontSize = 17.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Dynamic State Content: Scanning, Success, Error, or Idle
                Crossfade(
                    targetState = uiState.scanState,
                    label = "scan_state_crossfade"
                ) { state ->
                    when (state) {
                        is ScanState.Idle -> {
                            IdleWelcomeCard(
                                onCheckClick = {
                                    if (!uiState.hasPermissions) requestPermissions()
                                    else viewModel.startScan()
                                }
                            )
                        }

                        is ScanState.Scanning -> {
                            RadarScanAnimation(stageText = state.stage)
                        }

                        is ScanState.Success -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ResultCard(
                                    estimate = state.estimate,
                                    isThrottled = state.isThrottled,
                                    onScanAgain = { viewModel.startScan() }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Detected Wi-Fi networks expandable list
                                NetworkListCard(
                                    networks = state.networks,
                                    isExpanded = uiState.showNetworksList,
                                    onToggle = { viewModel.toggleNetworksList() }
                                )
                            }
                        }

                        is ScanState.Error -> {
                            ErrorCard(
                                errorType = state.errorType,
                                title = state.title,
                                detail = state.detail,
                                actionType = state.actionType,
                                onActionClick = handleAction
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // How the Estimate Works (Transparent Scoring & Technical explanation)
                val currentEstimate = (uiState.scanState as? ScanState.Success)?.estimate
                HeuristicExplanationCard(
                    estimate = currentEstimate,
                    isExpanded = uiState.showTechnicalDetails,
                    onToggle = { viewModel.toggleTechnicalDetails() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Mandatory Disclaimer
                DisclaimerCard()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun IdleWelcomeCard(onCheckClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("idle_welcome_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "কারেন্ট আছে নাকি গেছে? 🤔",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scan nearby physical Wi-Fi router radio signals to estimate if electrical power is active in your surroundings.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 21.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusPreviewPill(label = "🟢 Live Power", color = Color(0xFF00E676))
                StatusPreviewPill(label = "🟡 Uncertain", color = ElectricYellow)
                StatusPreviewPill(label = "🔴 Blackout", color = Color(0xFFFF3D71))
            }
        }
    }
}

@Composable
private fun StatusPreviewPill(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DisclaimerCard() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("disclaimer_card")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "⚠️",
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
            Column {
                Text(
                    text = "Electrical Disclaimer",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "This app estimates power availability using nearby Wi-Fi signals. It cannot directly detect electrical current and may give incorrect results. Routers may remain powered by UPS/battery systems or Wi-Fi radios may be switched off.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 16.sp,
                        fontSize = 11.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
