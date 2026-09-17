package com.example

import com.example.model.PowerStatus
import com.example.model.SignalCategory
import com.example.model.WifiNetworkInfo
import com.example.util.PowerEstimator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun zeroNetworks_estimates_probablyOff() {
        val estimate = PowerEstimator.estimate(
            networks = emptyList(),
            isFreshHardwareScan = true,
            scanIndex = 1
        )

        assertEquals(PowerStatus.PROBABLY_OFF, estimate.status)
        assertTrue(estimate.confidencePercent > 75)
        assertTrue(estimate.headlineBangla.isNotBlank())
        assertTrue(estimate.subHeadlineBangla.isNotBlank())
        assertTrue(estimate.funnyDetailsBangla.isNotBlank())
        assertEquals(0, estimate.totalNetworks)
    }

    @Test
    fun singleWeakNetwork_estimates_uncertain() {
        val singleWeak = listOf(
            WifiNetworkInfo(
                ssid = "Neighbor_UPS_Backup",
                maskedBssid = "12:34:56:**:**:ab",
                rssi = -88,
                frequencyMhz = 2412,
                signalLevel = 1,
                bandLabel = "2.4 GHz",
                category = SignalCategory.WEAK
            )
        )

        val estimate = PowerEstimator.estimate(
            networks = singleWeak,
            isFreshHardwareScan = true,
            scanIndex = 1
        )

        assertEquals(PowerStatus.UNCERTAIN, estimate.status)
        assertEquals(1, estimate.totalNetworks)
        assertEquals(1, estimate.weakCount)
        assertEquals(0, estimate.strongCount)
    }

    @Test
    fun multipleNetworksWithStrongSignal_estimates_probablyOn() {
        val multipleNetworks = listOf(
            WifiNetworkInfo(
                ssid = "Home_Fiber_5G",
                maskedBssid = "aa:bb:cc:**:**:01",
                rssi = -48,
                frequencyMhz = 5180,
                signalLevel = 4,
                bandLabel = "5 GHz",
                category = SignalCategory.STRONG
            ),
            WifiNetworkInfo(
                ssid = "Apt_302_WiFi",
                maskedBssid = "aa:bb:cc:**:**:02",
                rssi = -62,
                frequencyMhz = 2437,
                signalLevel = 3,
                bandLabel = "2.4 GHz",
                category = SignalCategory.STRONG
            ),
            WifiNetworkInfo(
                ssid = "D-Link_Guest",
                maskedBssid = "aa:bb:cc:**:**:03",
                rssi = -72,
                frequencyMhz = 2462,
                signalLevel = 2,
                bandLabel = "2.4 GHz",
                category = SignalCategory.MODERATE
            ),
            WifiNetworkInfo(
                ssid = "TP-Link_Deco",
                maskedBssid = "aa:bb:cc:**:**:04",
                rssi = -81,
                frequencyMhz = 5240,
                signalLevel = 1,
                bandLabel = "5 GHz",
                category = SignalCategory.WEAK
            )
        )

        val estimate = PowerEstimator.estimate(
            networks = multipleNetworks,
            isFreshHardwareScan = true,
            scanIndex = 2
        )

        assertEquals(PowerStatus.PROBABLY_ON, estimate.status)
        assertTrue(estimate.confidencePercent >= 80)
        assertEquals(4, estimate.totalNetworks)
        assertEquals(2, estimate.strongCount)
        assertEquals(1, estimate.moderateCount)
        assertEquals(1, estimate.weakCount)
        assertFalse(estimate.confidencePercent == 100) // Must never claim 100% certainty
    }

    @Test
    fun wifiNetworkInfo_signalPercentageCalculation() {
        val strongNet = WifiNetworkInfo(
            ssid = "TestNet",
            maskedBssid = "00:11:22:**:**:33",
            rssi = -40,
            frequencyMhz = 2412,
            signalLevel = 4,
            bandLabel = "2.4 GHz",
            category = SignalCategory.STRONG
        )
        assertEquals(100, strongNet.signalPercentage)

        val weakNet = WifiNetworkInfo(
            ssid = "",
            maskedBssid = "00:11:22:**:**:44",
            rssi = -100,
            frequencyMhz = 2412,
            signalLevel = 0,
            bandLabel = "2.4 GHz",
            category = SignalCategory.WEAK
        )
        assertEquals(0, weakNet.signalPercentage)
        assertEquals("Hidden Network (00:11:22:**:**:44)", weakNet.displaySsid)
    }
}
