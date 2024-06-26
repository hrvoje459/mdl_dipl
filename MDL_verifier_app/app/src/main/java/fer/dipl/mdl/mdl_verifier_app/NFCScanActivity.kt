package fer.dipl.mdl.mdl_verifier_app

import android.content.Intent
import android.content.res.Resources
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock.sleep
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.android.identity.android.mdoc.deviceretrieval.VerificationHelper
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.mdoc.connectionmethod.ConnectionMethod
import com.android.identity.mdoc.connectionmethod.ConnectionMethodBle
import com.android.identity.util.Logger
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import fer.dipl.mdl.mdl_verifier_app.ui.theme.MDL_verifier_appTheme
import java.util.UUID

class NFCScanActivity : ComponentActivity() {

    //private lateinit var verifierTransferHelper: VerifierTransferHelper
    private lateinit var transferHelper: VerifierTransferHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transferHelper = VerifierTransferHelper.getInstance(applicationContext, this)


        val extras = intent.extras

        Logger.d("REQ NFC", extras!!.getStringArrayList("requested_items").toString())

        transferHelper.setRequestedItems(extras!!.getStringArrayList("requested_items")!!.toTypedArray())

        setContent {
            MDL_verifier_appTheme {

                var stateDisplay = remember { mutableStateOf("Idle") }

                transferHelper.state.observe(this as LifecycleOwner){ state ->
                    stateDisplay.value = state
                    if (state == "Error"){
                        transferHelper.verificationHelper!!.disconnect()
                        VerifierTransferHelper.kill()
                    }
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

                                            val i: Intent = Intent(applicationContext, QRScanActivity::class.java)

                                            val requested_items : ArrayList<String> = arrayListOf()
                                            transferHelper.getRequestedItems().forEach { requested_items.add(it) }

                                            VerifierTransferHelper.kill()

                                            i.putExtra("requested_items", requested_items)
                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            ContextCompat.startActivity(applicationContext, i, null)
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


    override fun onStop() {
        Logger.d("STOP", "STOP NFC")
        super.onStop()
        VerifierTransferHelper.kill()
        Logger.d("STOP", "STOP NFC")
    }
}

