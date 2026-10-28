package me.cugarete.investowl

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.cugarete.investowl.core.designsystem.InvestowlTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = AppContainer(applicationContext)

        // Handle App Links / Deep Link from Telegram
        val targetTicker = extractTickerFromIntent(intent)

        setContent {
            InvestowlTheme {
                InvestowlApp(appContainer = appContainer)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractTickerFromIntent(intent: Intent?): String? {
        val data: Uri? = intent?.data
        if (data != null && (data.host?.contains("avenia.my.id") == true || data.host?.contains("cugarete.me") == true)) {
            val pathSegments = data.pathSegments
            if (pathSegments.isNotEmpty()) {
                val segment = pathSegments[0].uppercase().trim()
                if (segment.length in 4..6 && segment.all { it.isLetter() }) {
                    return segment
                }
            }
        }
        return null
    }
}
