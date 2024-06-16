package com.mastertech.btaccomppermiss


import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mastertech.btaccomppermiss.ui.theme.BTaccompPermissTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BTaccompPermissTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RequestBTPermission()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestBTPermission() {
    val context = LocalContext.current
    val bluetoothAdapter = context.getSystemService<BluetoothManager>()?.adapter



    //Registra y recuerda el estado del permiso
        val bTPermissionState =
            rememberPermissionState(permission =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ) {
                android.Manifest.permission.BLUETOOTH_CONNECT
            } else { android.Manifest.permission.BLUETOOTH})


    //funcion de utilidad para calcular el estado basado en el PermisionState
    fun getScreenState(state: PermissionState) = when (state.status) {
        is PermissionStatus.Denied -> PermissionScreenState(
            title = "Conectese al Dispositivo BT", buttonText = "Otorgar Permiso"
        )

        PermissionStatus.Granted -> PermissionScreenState(
            title = "Ud. puede conectarse ahora", buttonText = "Conectese"
        )
    }

    // define la UI PermissionScreen basada en el estado del permiso y las interacciones del usuario
    var screenState by remember(bTPermissionState.status) {
        mutableStateOf(getScreenState(bTPermissionState))
    }

    val takeResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {result -> if (result.resultCode == RESULT_OK) {
            Toast.makeText(context, "BT ON", Toast.LENGTH_SHORT)
                .show()


        } else {
            Toast.makeText(context, "BT OFF", Toast.LENGTH_SHORT)
                .show()
            //LastChance()
            //Mensaje advertencia BT is OFF Desea encenderlo
            //Rechazo, "Se cerrara la aplicacion", Acepta, Conectarse, Volver a intentarlo
            finish()
            // takeResultLauncher.launch(enableBtIntent)
           // RequestBTPermission()
            }
        }
    )

    PermissionScreen(
        state = screenState,
        onClick = {
            // Siempre requiera permisos en contexto, provea un Rationale si es necesario y chequee su status
            //Antes de usar un API que requiera un permiso
            when (bTPermissionState.status) {
                PermissionStatus.Granted -> {
                    Toast.makeText(context, "Conectandose...", Toast.LENGTH_SHORT).show()

                    // Check if Bluetooth is enabled
                    if (bluetoothAdapter != null) {
                        if (!bluetoothAdapter.isEnabled) {
                            // Request to enable ON Bluetooth
                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            takeResultLauncher.launch(enableBtIntent)
                            // Start the activity for result

                        } else {
                            //BT is already ON
                            Toast.makeText(context, "BT is ON", Toast.LENGTH_SHORT).show()}
                    } else {
                        Toast.makeText(context, "Bluetooth is not supported", Toast.LENGTH_SHORT).show()
                        // Bluetooth is not supported
                        //the app will be closed
                        //finish()
                    }
                }

                is PermissionStatus.Denied -> {
                    if (bTPermissionState.status.shouldShowRationale) {
                        //update our UI based on the user interaction by showing a rationale
                        screenState = PermissionScreenState(
                            title = "Conectese al Dispositivo BT",
                            buttonText = "Permiso BT",
                            rationale = "En orden de conectarse al dispositivo BT ud, debe otorgar el permiso " +
                                    "Aceptando la proxima peticion.\n\nLe gustaria continuar?"
                        )
                    } else {
                        // Lanza directamente el dialo del permiso del sistema
                        bTPermissionState.launchPermissionRequest()
                    }
                }
            }
        },
        onRationaleReply = { accepted ->
            if (accepted) {
                bTPermissionState.launchPermissionRequest()
            }
            //Reset the state after user interaction
            screenState = getScreenState(bTPermissionState)
        }
    )

    fun finish() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter?.disable()
        TODO("Not yet implemented")
    }


}



