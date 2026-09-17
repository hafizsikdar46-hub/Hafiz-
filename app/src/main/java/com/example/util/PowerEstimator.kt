package com.example.util

import com.example.model.PowerEstimate
import com.example.model.PowerStatus
import com.example.model.WifiNetworkInfo

object PowerEstimator {

    fun estimate(
        networks: List<WifiNetworkInfo>,
        isFreshHardwareScan: Boolean,
        scanIndex: Int = 0
    ): PowerEstimate {
        val total = networks.size
        val strong = networks.count { it.isStrong }
        val moderate = networks.count { it.isModerate }
        val weak = networks.count { it.isWeak }
        val avgRssi = if (total > 0) networks.map { it.rssi }.average().toInt() else null

        val (status, confidence, techExplanation) = when {
            total == 0 -> {
                Triple(
                    PowerStatus.PROBABLY_OFF,
                    82,
                    "Zero nearby Wi-Fi radio beacons detected. In urban and suburban areas, standard home routers lose power immediately when grid electricity is interrupted."
                )
            }
            total in 1..2 -> {
                if (strong > 0) {
                    Triple(
                        PowerStatus.PROBABLY_ON,
                        68,
                        "Detected $total active nearby router(s) with strong signal ($strong strong). While highly indicative of grid electricity, a private UPS/IPS or generator cannot be ruled out."
                    )
                } else {
                    Triple(
                        PowerStatus.UNCERTAIN,
                        45,
                        "Detected only $total faint signal(s) with weak signal levels. Faint signals often originate from battery-powered phone hotspots, distant areas, or emergency backups."
                    )
                }
            }
            total >= 3 -> {
                if (strong >= 1 || moderate >= 2) {
                    val calcConfidence = (75 + (total * 2)).coerceAtMost(94)
                    Triple(
                        PowerStatus.PROBABLY_ON,
                        calcConfidence,
                        "Detected $total active Wi-Fi networks ($strong strong, $moderate moderate). Multiple independent routers broadcasting across RF channels strongly indicates standard grid power is active."
                    )
                } else {
                    Triple(
                        PowerStatus.UNCERTAIN,
                        52,
                        "Detected $total networks, but all signals are weak/distant ($weak weak). Power state at your exact electrical circuit remains ambiguous."
                    )
                }
            }
            else -> {
                Triple(
                    PowerStatus.UNCERTAIN,
                    50,
                    "Insufficient signal variance to determine status."
                )
            }
        }

        // Generate commentary with deterministic seed based on network count and scanIndex
        val commentary = BanglaCommentary.getCommentary(status, seed = total + scanIndex)

        return PowerEstimate(
            status = status,
            confidencePercent = confidence,
            headlineBangla = commentary.headline,
            subHeadlineBangla = commentary.subHeadline,
            funnyDetailsBangla = commentary.details,
            technicalExplanation = techExplanation,
            totalNetworks = total,
            strongCount = strong,
            moderateCount = moderate,
            weakCount = weak,
            averageRssi = avgRssi,
            isFreshHardwareScan = isFreshHardwareScan
        )
    }
}
