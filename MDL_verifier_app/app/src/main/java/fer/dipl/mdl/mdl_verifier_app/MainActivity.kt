package fer.dipl.mdl.mdl_verifier_app

import COSE.AlgorithmID
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.X509CertUtils
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.docrequest.MDocRequestBuilder
import java.io.File
import java.security.cert.X509Certificate

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
                    Radios(applicationContext)
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
private fun Radios(/*innerPadding: PaddingValues*/context: Context)
{


    var include_family_name by remember { mutableStateOf(false) }
    var include_given_name by remember { mutableStateOf(false) }
    var include_portrait by remember { mutableStateOf(true) }
    var include_driving_privileges by remember { mutableStateOf(true) }
    var include_age_over_18 by remember { mutableStateOf(true) }
    var include_expiry_date by remember { mutableStateOf(true) }

    var include_document_number by remember { mutableStateOf(false) }
    var include_issue_date by remember { mutableStateOf(false) }
    var include_birth_date by remember { mutableStateOf(false) }
    var include_issuing_country by remember { mutableStateOf(false) }
    var include_issuing_authority by remember { mutableStateOf(false) }
    var include_age_over_21 by remember { mutableStateOf(false) }
    var include_age_over_24 by remember { mutableStateOf(false) }
    var include_age_over_65 by remember { mutableStateOf(false) }


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
                            val i: Intent = Intent(context, NFCScanActivity::class.java)

                            val requested_items : MutableList<String> = mutableListOf()


                            if (include_family_name) requested_items.add("family_name")
                            if (include_given_name) requested_items.add("given_name")
                            if (include_portrait) requested_items.add("portrait")
                            if (include_driving_privileges) requested_items.add("driving_privileges")
                            if (include_age_over_18) requested_items.add("age_over_18")
                            if (include_expiry_date) requested_items.add("expiry_date")
                            if (include_document_number) requested_items.add("document_number")
                            if (include_issue_date) requested_items.add("issue_date")
                            if (include_birth_date) requested_items.add("birth_date")
                            if (include_issuing_country) requested_items.add("issuing_country")
                            if (include_issuing_authority) requested_items.add("issuing_authority")
                            if (include_age_over_21) requested_items.add("age_over_21")
                            if (include_age_over_24) requested_items.add("age_over_24")
                            if (include_age_over_65) requested_items.add("age_over_65")



                            i.putExtra("requested_items", requested_items.toTypedArray())
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ContextCompat.startActivity(context, i, null)
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
                            val i: Intent = Intent(context, QRScanActivity::class.java)


                            val requested_items : ArrayList<String> = arrayListOf()


                            if (include_family_name) requested_items.add("family_name")
                            if (include_given_name) requested_items.add("given_name")
                            if (include_portrait) requested_items.add("portrait")
                            if (include_driving_privileges) requested_items.add("driving_privileges")
                            if (include_age_over_18) requested_items.add("age_over_18")
                            if (include_expiry_date) requested_items.add("expiry_date")
                            if (include_document_number) requested_items.add("document_number")
                            if (include_issue_date) requested_items.add("issue_date")
                            if (include_birth_date) requested_items.add("birth_date")
                            if (include_issuing_country) requested_items.add("issuing_country")
                            if (include_issuing_authority) requested_items.add("issuing_authority")
                            if (include_age_over_21) requested_items.add("age_over_21")
                            if (include_age_over_24) requested_items.add("age_over_24")
                            if (include_age_over_65) requested_items.add("age_over_65")



                            i.putExtra("requested_items", requested_items)

                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ContextCompat.startActivity(context, i, null)
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
                //.verticalScroll(
                //    rememberScrollState()
                //)
                .fillMaxSize()
                .fillMaxHeight()
                //.wrapContentHeight(align = Alignment.CenterVertically)
                .background(Color.Red)
            ,
            //verticalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card (
                modifier = Modifier
                    //.size(width = 240.dp, height = 100.dp)
                    .fillMaxWidth()
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
                //.align(Alignment.CenterHorizontally)
                ,
            ){


                Row {
                    Text(
                        text = "Family name:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_family_name,
                        onCheckedChange = {
                            include_family_name = it
                        }
                    )
                }
                Row {
                    Text(
                        text = "Given name:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_given_name,
                        onCheckedChange = {
                            include_given_name = it
                        }
                    )
                }
                Row {
                    Text(
                        text = "Portrait:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_portrait,
                        onCheckedChange = {
                            include_portrait = it
                        },
                        enabled = false,
                        thumbContent = if (include_portrait) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        },
                        colors = SwitchDefaults.colors(
                            //checkedThumbColor = MaterialTheme.colorScheme.primary,
                            //checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledCheckedThumbColor = MaterialTheme.colorScheme.primary,
                            disabledCheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    )
                }
                Row {
                    Text(
                        text = "Driving privileges:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_driving_privileges,
                        onCheckedChange = {
                            include_driving_privileges = it
                        },
                        enabled = false,
                        thumbContent = if (include_driving_privileges) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        },
                        colors = SwitchDefaults.colors(
                            //checkedThumbColor = MaterialTheme.colorScheme.primary,
                            //checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledCheckedThumbColor = MaterialTheme.colorScheme.primary,
                            disabledCheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        )


                    )
                }
                Row {
                    Text(
                        text = "Expiry date:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_expiry_date,
                        onCheckedChange = {
                            include_expiry_date = it
                        },
                        enabled = false,
                        thumbContent = if (include_expiry_date) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        },
                        colors = SwitchDefaults.colors(
                            //checkedThumbColor = MaterialTheme.colorScheme.primary,
                            //checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledCheckedThumbColor = MaterialTheme.colorScheme.primary,
                            disabledCheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    )
                }
                Row {
                    Text(
                        text = "Age over 18:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_age_over_18,
                        onCheckedChange = {
                            include_age_over_18 = it
                        }
                    )
                }
                Row {
                    Text(
                        text = "Document number:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_document_number,
                        onCheckedChange = {
                            include_document_number = it
                        }
                    )
                }
                Row {
                    Text(
                        text = "Issue date:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_issue_date,
                        onCheckedChange = {
                            include_issue_date = it
                        }
                    )
                }
                Row {
                    Text(
                        text = "Birth date:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_birth_date,
                        onCheckedChange = {
                            include_birth_date = it
                        }
                    )
                }
                Row {
                    Text(
                        text = "Issuing country:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_issuing_country,
                        onCheckedChange = {
                            include_issuing_country = it
                        }
                    )
                }

                Row {
                    Text(
                        text = "Issuing authority:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_issuing_authority,
                        onCheckedChange = {
                            include_issuing_authority = it
                        }
                    )
                }
                Row {
                    Text(
                        text = "Age over 21:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_age_over_21,
                        onCheckedChange = {
                            include_age_over_21 = it
                        }
                    )
                }
                Row {
                    Text(
                        text = "Age over 24:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_age_over_24,
                        onCheckedChange = {
                            include_age_over_24 = it
                        }
                    )
                }
                Row {
                    Text(
                        text = "Age over 65:",
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Yellow)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        modifier = Modifier.weight(1f),
                        checked = include_age_over_65,
                        onCheckedChange = {
                            include_age_over_65 = it
                        }
                    )
                }



            }

        }
    }



}