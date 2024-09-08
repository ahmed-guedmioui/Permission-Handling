package com.hamza_apps.permissionhandling

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.hamza_apps.permissionhandling.ui.theme.PermissionHandlingTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionHandlingTheme {

                val showDialog =
                    mainViewModel.showDialog.collectAsState().value

                val launchAppSettings =
                    mainViewModel.launchAppSettings.collectAsState().value

                val permissionsResultActivityLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { result ->
                        permissions.forEach { permission ->
                            if (result[permission] == false) {
                                if (!shouldShowRequestPermissionRationale(permission)) {
                                    mainViewModel.updateLaunchAppSettings(true)
                                }
                                mainViewModel.updateShowDialog(true)
                            }
                        }
                    }
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Button(onClick = {
                        permissions.forEach { permission ->
                            val isGranted = checkSelfPermission(permission) ==
                                    PackageManager.PERMISSION_GRANTED

                            if (!isGranted) {
                                if (shouldShowRequestPermissionRationale(permission)) {
                                    mainViewModel.updateShowDialog(true)
                                } else {
                                    permissionsResultActivityLauncher.launch(permissions)
                                }
                            }

                        }
                    }) {
                        Text(text = "Request Permission")
                    }

                }

                if (showDialog) {
                    PermissionDialog(
                        onDismiss = {
                            mainViewModel.updateShowDialog(false)
                        },
                        onConfirm = {
                            mainViewModel.updateShowDialog(false)

                            if (launchAppSettings) {
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                ).also {
                                    startActivity(it)
                                }
                                mainViewModel.updateLaunchAppSettings(false)
                            } else {
                                permissionsResultActivityLauncher.launch(permissions)
                            }

                        }
                    )
                }


            }
        }
    }

    @Composable
    fun PermissionDialog(
        onDismiss: () -> Unit,
        onConfirm: () -> Unit,
    ) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = onConfirm
                ) {
                    Text(text = "Ok")
                }
            },
            title = {
                Text(
                    text = "Camera, Microphone and storage permissions are needed",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "This app needs access to your camera and microphone"
                )
            }
        )
    }
}












