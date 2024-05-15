package fer.dipl.mdl.mdl_verifier_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.android.identity.android.mdoc.deviceretrieval.VerificationHelper
import com.android.identity.util.Logger
import fer.dipl.mdl.mdl_verifier_app.ui.theme.MDL_verifier_appTheme


import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode

class MainActivity : ComponentActivity() {

    //private lateinit var transferHelper: TransferHelper

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Logger.d("MAIN HRV", "permissionsLauncher ${it.key} = ${it.value}")
                if (!it.value) {
                    Toast.makeText(
                        this,
                        "The ${it.key} permission is required for BLE",
                        Toast.LENGTH_LONG
                    ).show()
                    return@registerForActivityResult
                }
            }
        }

    private val appPermissions: Array<String> =
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                //Manifest.permission.NEARBY_WIFI_DEVICES,
                Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }

    fun toast(text: String){
        Looper.prepare()
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsNeeded = appPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            permissionsLauncher.launch(
                permissionsNeeded.toTypedArray()
            )
        }

        //transferHelper = TransferHelper.getInstance(applicationContext, this)

        /*setContent {
            MDL_verifier_appTheme {
                // A surface container using the 'background' color from the theme
                /*Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }*/
                Column {
                    /*QrScanner(
                        onClose = { Logger.d("MAIN HRV", "close" ) },
                        qrCodeReturn = { qrtext ->
                            Logger.d("MAIN HRV", "qr_text" + qrtext)
                            toast(qrtext)
                            val i: Intent = Intent(applicationContext, ConnectionActivity::class.java)
                            i.putExtra("qr_code_value", qrtext)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ContextCompat.startActivity(applicationContext, i, null)
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            //.fillMaxSize()
                            .height(Resources.getSystem().displayMetrics.heightPixels.dp - 1500.dp)
                    )*/
                    Text(text = "Press button for engagement")
                    Button(onClick = {
                        val i: Intent = Intent(applicationContext, ConnectionActivity::class.java)
                        //i.putExtra("qr_code_value", qrtext)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ContextCompat.startActivity(applicationContext, i, null)
                    }) {

                    }
                    //Text(text = "SCAN QR OR TAP THE BACK FOR NFC ENGAGEMENT")
                }

            }
        }*/
        setContent {
            MDL_verifier_appTheme {
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
                                /*Text(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    text = "Bottom app bar",
                                )*/
                                Row {
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            Logger.d("MAIN HRV", "buttons")
                                            val i: Intent = Intent(applicationContext, NFCScanActivity::class.java)
                                            //i.putExtra("qr_code_value", transferHelper.qrEng.value)
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
                                            Logger.d("MAIN HRV", "buttons1")
                                            /*val transferHelper = TransferHelper.getInstance(applicationContext)
                                            if (transferHelper.qrEng.value == ""){
                                                Logger.d("MAIN HRV", "button its null")
                                            }else{
                                                Logger.d("MAIN HRV", transferHelper.qrEng.value!!)
                                                val i: Intent = Intent(applicationContext, QRPresentationActivity::class.java)
                                                i.putExtra("qr_code_value", transferHelper.qrEng.value)
                                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                ContextCompat.startActivity(applicationContext, i, null)
                                            }*/
                                            val i: Intent = Intent(applicationContext, QRScanActivity::class.java)
                                            //i.putExtra("qr_code_value", transferHelper.qrEng.value)
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
                        /*floatingActionButton = {
                            FloatingActionButton(onClick = {
                                Logger.d("MAIN HRV", "buttons")
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }*/


                    ){
                            innerPadding ->

                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .verticalScroll(
                                    rememberScrollState()
                                ),
                            verticalArrangement = Arrangement.spacedBy(1.dp),

                            ) {

                            //GreetingPreview2("Mile " + value, bmp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MDL_verifier_appTheme {
        Greeting("Android")
    }
}


@Composable
private fun CloseBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
            .minimumInteractiveComponentSize(),
    ) {
        Text(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            text = "Close",
            modifier = Modifier
                .padding(5.dp)
        )
    }
}
