package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.PowerEstimate
import com.example.model.PowerStatus
import com.example.ui.components.ResultCard
import com.example.ui.theme.CurrentDetectorTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun greeting_screenshot() {
        val sampleEstimate = PowerEstimate(
            status = PowerStatus.PROBABLY_ON,
            confidencePercent = 88,
            headlineBangla = "🟢 কারেন্ট আছে ভাই! ⚡😂",
            subHeadlineBangla = "Wi-Fi-গুলো বেঁচে আছে, আশা করা যায় বিদ্যুৎও আছে!",
            funnyDetailsBangla = "ফ্যান ছাড়ো, মোবাইল চার্জে দাও! রাউটারগুলো হাসিমুখে সিগন্যাল দিচ্ছে। 😎",
            technicalExplanation = "Detected 4 active Wi-Fi access points with strong signals.",
            totalNetworks = 4,
            strongCount = 2,
            moderateCount = 1,
            weakCount = 1,
            averageRssi = -61,
            isFreshHardwareScan = true
        )

        composeTestRule.setContent {
            CurrentDetectorTheme {
                ResultCard(
                    estimate = sampleEstimate,
                    isThrottled = false,
                    onScanAgain = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
