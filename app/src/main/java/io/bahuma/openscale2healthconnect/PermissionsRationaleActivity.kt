package io.bahuma.openscale2healthconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.bahuma.openscale2healthconnect.component.HyperlinkText
import io.bahuma.openscale2healthconnect.ui.theme.OpenScaleToHealthConnectTheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenScaleToHealthConnectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HyperlinkText(
                        fullText = "This app only writes to HealthConnect and does not read anything. It will copy your weight data from OpenScale to Health Connect. Your data will never leave your device or will be uploaded to the internet or a third party.\n\nYou can find more detailed information in our privacy policy.",
                        hyperLinks = mutableMapOf(
                            "privacy policy" to "https://bahuma.io/privacy-openscale2healthconnect.php"
                        )
                    )
                }
            }
        }
    }
}