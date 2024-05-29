package fer.dipl.mdl.mdl_verifier_app

import android.app.Fragment
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

class QRScanActivity : ComponentActivity() {

    private lateinit var transferHelper: VerifierTransferHelper

    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transferHelper = VerifierTransferHelper.getInstance(applicationContext, this)

        val extras = intent.extras

        val requested_items = extras!!.getStringArrayList("requested_items")


        transferHelper.setRequestedItems(requested_items!!.toTypedArray())


        val qrCodeReturn = { qrtext:String ->
            Logger.d("MAIN HRV", "qr_text" + qrtext)
            VerifierTransferHelper.getInstance(applicationContext, this).setRequestedItems(extras!!.getStringArrayList("requested_items")!!.toTypedArray())
            VerifierTransferHelper.getInstance(applicationContext, this).verificationHelper!!.setDeviceEngagementFromQrCode(qrtext)
        }

        val codeScannerView = CodeScannerView(applicationContext).apply {
            codeScanner = CodeScanner(applicationContext, this).apply {
                isAutoFocusEnabled = true
                isAutoFocusButtonVisible = false
                scanMode = ScanMode.SINGLE
                decodeCallback = DecodeCallback { result ->
                    qrCodeReturn.invoke(result.text)
                    releaseResources()
                }
                errorCallback = ErrorCallback {
                    releaseResources()
                }
                camera = CodeScanner.CAMERA_BACK
                isFlashEnabled = false
            }
            codeScanner.startPreview()
        }


        setContent {
            MDL_verifier_appTheme {
                var stateDisplay = remember { mutableStateOf("Idle") }

                transferHelper.state.observe(this as LifecycleOwner){ state ->
                    stateDisplay.value = state
                    if (state == "Error" || state == "Device Disconnected"){
                        Logger.d("QR SCAN ", "ERROR")

                        val i: Intent = Intent(applicationContext, MainActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ContextCompat.startActivity(applicationContext, i, null)
                    }
                }

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
                                            val i: Intent = Intent(applicationContext, NFCScanActivity::class.java)

                                            val requested_items : ArrayList<String> = arrayListOf()
                                            transferHelper.getRequestedItems().forEach { requested_items.add(it) }

                                            VerifierTransferHelper.kill()
                                            codeScanner.releaseResources()

                                            i.putExtra("requested_items", requested_items)
                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            ContextCompat.startActivity(applicationContext, i, null)

                                        }
                                    ) {
                                        Text(text = "NFC ENGAGEMENT")
                                        Icon(Icons.Default.Add, contentDescription = "Add")
                                    }
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            Logger.d("QR SCAN", "Do nothing")

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
                            Box{
                                QrScanner(
                                    codeScannerView,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .height(Resources.getSystem().displayMetrics.heightPixels.dp - 1700.dp)
                                )
                            }

                            Text(text = "QR SCANNING IN PROGRESS")
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
        super.onStop()

        Logger.d("STOP", "STOP QR")
        VerifierTransferHelper.kill()
        Logger.d("STOP", "STOP QR")
    }
}


@Composable
private fun QrScanner(
    codeScannerView: CodeScannerView,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Box(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            AndroidView(
                modifier = Modifier,
                factory = {
                    codeScannerView
                },
            )
        }
    }
}