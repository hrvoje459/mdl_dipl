package fer.dipl.mdl.mdl_verifier_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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

        setContent {
            MDL_verifier_appTheme {
                // A surface container using the 'background' color from the theme
                /*Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }*/
                QrScanner(
                    onClose = { Logger.d("MAIN HRV", "close" ) },
                    qrCodeReturn = { qrtext ->
                        Logger.d("MAIN HRV", "qrtext" + qrtext)
                        toast(qrtext)
                        val i: Intent = Intent(applicationContext, ConnectionActivity::class.java)
                        i.putExtra("qr_code_value", qrtext)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ContextCompat.startActivity(applicationContext, i, null)
                        },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                )
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
private fun QrScanner(
    qrCodeReturn: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Box(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            AndroidView(
                modifier = Modifier,
                factory = {
                    CodeScannerView(it).apply {
                        val codeScanner = CodeScanner(it, this).apply {
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
                },
            )
        }
        CloseBtn(onClick = onClose)
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
