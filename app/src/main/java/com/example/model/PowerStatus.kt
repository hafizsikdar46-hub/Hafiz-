package com.example.model

enum class PowerStatus(
    val emoji: String,
    val labelBangla: String,
    val labelEnglish: String
) {
    PROBABLY_ON("🟢", "কারেন্ট আছে সম্ভবত!", "PROBABLY ON"),
    UNCERTAIN("🟡", "সন্দেহজনক / নিশ্চিত না!", "UNCERTAIN"),
    PROBABLY_OFF("🔴", "কারেন্ট নাই মনে হচ্ছে!", "PROBABLY OFF")
}

data class PowerEstimate(
    val status: PowerStatus,
    val confidencePercent: Int,
    val headlineBangla: String,
    val subHeadlineBangla: String,
    val funnyDetailsBangla: String,
    val technicalExplanation: String,
    val totalNetworks: Int,
    val strongCount: Int,
    val moderateCount: Int,
    val weakCount: Int,
    val averageRssi: Int?,
    val isFreshHardwareScan: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
