package com.example.model

enum class SignalCategory(val label: String) {
    STRONG("Strong 📶"),
    MODERATE("Moderate 🟡"),
    WEAK("Weak 🔴")
}

data class WifiNetworkInfo(
    val ssid: String,
    val maskedBssid: String,
    val rssi: Int,
    val frequencyMhz: Int,
    val signalLevel: Int, // 0 to 4
    val bandLabel: String,
    val category: SignalCategory
) {
    val isStrong: Boolean get() = category == SignalCategory.STRONG
    val isModerate: Boolean get() = category == SignalCategory.MODERATE
    val isWeak: Boolean get() = category == SignalCategory.WEAK

    val displaySsid: String
        get() = if (ssid.isBlank() || ssid == "<unknown ssid>") "Hidden Network ($maskedBssid)" else ssid

    val signalPercentage: Int
        get() {
            // RSSI typically ranges from -100 (worst) to -40 (best)
            return when {
                rssi <= -100 -> 0
                rssi >= -40 -> 100
                else -> ((rssi + 100) * 100) / 60
            }.coerceIn(0, 100)
        }
}
