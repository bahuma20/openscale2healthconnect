package io.bahuma.openscale2healthconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.bahuma.openscale2healthconnect.component.HyperlinkText
import io.bahuma.openscale2healthconnect.ui.theme.OpenScaleToHealthConnectTheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionsRationale()
        }
    }
}

@Composable
fun PermissionsRationale() {
    OpenScaleToHealthConnectTheme {
        Scaffold { innerPadding ->
            Surface(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "OpenScale to Health Connect",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Privacy Statement",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HyperlinkText(
                        fullText = "This app only writes to HealthConnect and does not read anything. It will copy your weight data from OpenScale to Health Connect. Your data will never leave your device and will not be uploaded to the internet or another third party than HealthConnect.\n\nYou can find more detailed information in our privacy policy.",
                        hyperLinks = mutableMapOf(
                            "privacy policy" to "https://bahuma.io/privacy-openscale2healthconnect.php"
                        ),
                        linkTextColor = MaterialTheme.colorScheme.primary,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewPermissionsRationaleActivityPreview() {
    PermissionsRationale()
}