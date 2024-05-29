package fer.dipl.mdl.mdl_holder_app.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.android.identity.util.Logger
import fer.dipl.mdl.mdl_holder_app.helpers.NFCTransferHelper
import fer.dipl.mdl.mdl_holder_app.helpers.NfcEngagementHandler
import fer.dipl.mdl.mdl_holder_app.helpers.QRTransferHelper
import fer.dipl.mdl.mdl_holder_app.ui.theme.MDL_holder_appTheme



class NFCPresentationActivity: ComponentActivity() {

    private lateinit var nfcTransferHelper: NFCTransferHelper

    lateinit var ser_con :ServiceConnection

        @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcTransferHelper = NFCTransferHelper.getInstance(applicationContext)

         ser_con = object :ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Logger.d("MAIN SERVICE CONN", "onServiceConnected")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Logger.d("MAIN SERVICE CONN", "onServiceDisconnected")
            }
        }

        bindService(
            Intent(this, NfcEngagementHandler::class.java),
            ser_con,
            Context.BIND_AUTO_CREATE
        )

        setContent {
            MDL_holder_appTheme {

                var stateDisplay = remember { mutableStateOf("Idle") }

                nfcTransferHelper.state.observe(this as LifecycleOwner){state ->
                    stateDisplay.value = state
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {},
                        bottomBar = {
                            BottomAppBar(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Row {
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            Logger.d("MAIN HRV", "buttons")
                                        }
                                    ) {
                                        Text(text = "NFC ENGAGEMENT")
                                        Icon(Icons.Default.Add, contentDescription = "Add")
                                    }
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            Logger.d("MAIN HRV", "buttons2")
                                            val transferHelper =
                                                QRTransferHelper.getInstance(applicationContext)
                                        }
                                    ) {
                                        Text(text = "QR CODE")
                                        Icon(Icons.Default.Add, contentDescription = "Add")
                                    }
                                }

                            }
                        },
                    ){
                            innerPadding ->

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "NFC SCANNING IN PROGRESS")
                            Box(modifier = Modifier.padding(innerPadding)){
                                CircularProgressIndicator(
                                    modifier = Modifier.width(64.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            }


                            Text(text = stateDisplay.value)
                        }
                    }
                }
            }
        }
    }

    @Override
    override fun onPause() {
        Logger.d("MAIN SERVICE STOP", "STOP")

        unbindService(ser_con)
        super.onPause()
    }
    /*@Override
    override fun onStop() {
        super.onStop()
        TransferHelper.kill()
        transferHelper.qrEngagementHelper?.close()
        transferHelper.deviceRetrievalHelper?.disconnect()

        Logger.d("MAIN DESTROY", "on QR exit")
    }*/
}



