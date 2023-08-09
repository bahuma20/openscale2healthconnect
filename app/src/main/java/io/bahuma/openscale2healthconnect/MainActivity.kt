package io.bahuma.openscale2healthconnect

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.bahuma.openscale2healthconnect.component.HyperlinkText
import io.bahuma.openscale2healthconnect.service.HealthConnectDataService
import io.bahuma.openscale2healthconnect.service.HealthConnectService
import io.bahuma.openscale2healthconnect.service.OpenScaleDataService
import io.bahuma.openscale2healthconnect.service.OpenScaleService
import io.bahuma.openscale2healthconnect.service.PackageDetector
import io.bahuma.openscale2healthconnect.service.SyncService
import io.bahuma.openscale2healthconnect.ui.theme.OpenScaleToHealthConnectTheme
import kotlinx.coroutines.launch
import java.util.Date


class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AppViewModel
    private lateinit var openScaleService: OpenScaleService
    private lateinit var openScaleDataService: OpenScaleDataService

    private lateinit var healthConnectService: HealthConnectService
    private lateinit var healthConnectDataService: HealthConnectDataService

    private lateinit var syncService: SyncService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[AppViewModel::class.java]

        val packageDetector = PackageDetector(packageManager)
        try {
            val packageName = packageDetector.detectPackage()

            viewModel.setOpenScaleAvailable(true)

            openScaleService = OpenScaleService(this, viewModel, packageName)

            openScaleDataService = OpenScaleDataService(this, packageName)

            openScaleService.init()

            openScaleService.checkPermissionGranted()

            syncService = SyncService(this, viewModel, packageName)
            syncService.watchSyncWorkerState()
        } catch (e: PackageDetector.PackageNotFoundException) {
            Log.e("test", "Package not found")
            viewModel.setHealthConnectAvailable(false)
        }

        healthConnectService = HealthConnectService(this, viewModel)
        healthConnectDataService = HealthConnectDataService()


        lifecycle.coroutineScope.launch {
            val healthConnectClient = healthConnectService.detectHealthConnect()
            if (healthConnectClient != null) {
                healthConnectDataService.healthConnectClient = healthConnectClient
            }
        }

        setContent {
            OpenScaleToHealthConnectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }

    @Composable
    fun App(viewModel: AppViewModel = viewModel()) {

        val coroutineScope = rememberCoroutineScope()


        val lastRunString: String
        val lastRunInstant = viewModel.lastRun.value
        lastRunString = if (lastRunInstant == null) {
            "Not run yet"
        } else {
            val lastRun = Date.from(lastRunInstant)
            val dateFormat = android.text.format.DateFormat.getDateFormat(applicationContext)
            val timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)
            dateFormat.format(lastRun) + " " + timeFormat.format(lastRun)
        }

        Column(Modifier.padding(16.dp)) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    Modifier.padding(16.dp)
                ) {
                    Text(
                        "OpenScale",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    if (viewModel.openScaleAvailable.value) {
                        if (viewModel.openScalePermissionsGranted.value) {
                            Row(
                                Modifier
                                    .padding(vertical = 8.dp)
                                    .padding(start = 16.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                Text("Permissions granted")
                            }
                        } else {
                            Button(
                                enabled = viewModel.openScaleAvailable.value && !viewModel.openScalePermissionsGranted.value,
                                onClick = {
                                    openScaleService.requestPermissions()
                                }) {
                                Text("Request openScale permissions")
                            }
                        }

                        UserSelect()
                    } else {
                        Row() {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "",
                                tint = Color.Red,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("OpenScale is not installed on your device. Please install it before you continue.")
                        }
                    }


                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.ArrowForward,
                    contentDescription = "",
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(90f)
                )
            }

            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    Modifier.padding(16.dp)
                ) {
                    Text(
                        "Health Connect",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    if (!viewModel.healthConnectAvailable.value) {
                        Row() {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "",
                                tint = Color.Red,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Health Connect is not available on this device. Please make sure the Health Connect application is installed and updated to the latest version.")
                        }
                    }

                    if (viewModel.healthConnectAvailable.value && viewModel.healthConnectAllPermissionsGranted.value) {
                        Row(
                            Modifier
                                .padding(vertical = 8.dp)
                                .padding(start = 16.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            Text("Permissions granted")
                        }
                    } else {
                        Button(
                            enabled = viewModel.healthConnectAvailable.value && !viewModel.healthConnectAllPermissionsGranted.value,
                            onClick = {
                                coroutineScope.launch {
                                    healthConnectService.requestPermissions()
                                }
                            }) {
                            Text("Request Health Connect permissions")
                        }
                    }
                }
            }


            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Button(
                    enabled = viewModel.openScaleAvailable.value && viewModel.openScalePermissionsGranted.value && viewModel.healthConnectAllPermissionsGranted.value && viewModel.openScaleSelectedUser.value != null,
                    onClick = {
                        syncService.runWorkOnce()
                    }) {
                    Text("Manual full sync")
                }
            }

            if (viewModel.syncRunning.value) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    Text(text = "Sync is running")
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Last run: $lastRunString", modifier = Modifier.padding(vertical = 16.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = viewModel.syncEnabled.value,
                    onCheckedChange = {
                        if (it) {
                            syncService.setUpSyncWorker()
                        } else {
                            syncService.removeSyncWorker()
                        }
                    },
                    enabled = viewModel.openScaleAvailable.value && viewModel.syncEnabled.value || viewModel.openScalePermissionsGranted.value && viewModel.healthConnectAllPermissionsGranted.value && viewModel.openScaleSelectedUser.value != null,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text("Enable Auto-Sync (Every 2 hours)")
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                HyperlinkText(
                    fullText = "Privacy Policy",
                    hyperLinks = mutableMapOf(
                        "Privacy Policy" to "https://bahuma.io/privacy-openscale2healthconnect.php"
                    ),
                    linkTextColor = MaterialTheme.colorScheme.primary
                )
            }
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UserSelect(viewModel: AppViewModel = viewModel()) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                label = { Text("OpenScale user") },
                value = viewModel.openScaleSelectedUser.value?.username ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                viewModel.openScaleUsers.value.forEach { user ->
                    DropdownMenuItem(
                        text = {
                            Text(user.username)
                        },
                        onClick = {
                            viewModel.selectOpenScaleUser(user)
                            openScaleDataService.saveSelectedUserId(user.id)
                            expanded = false
                        }
                    )
                }

            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        OpenScaleToHealthConnectTheme {
            App()
        }
    }
}

