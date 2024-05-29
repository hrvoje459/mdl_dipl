package fer.dipl.mdl.mdl_verifier_app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.identity.util.Logger
import fer.dipl.mdl.mdl_verifier_app.ui.theme.MDL_verifier_appTheme


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
                Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
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

        VerifierTransferHelper.kill()

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
                Row {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val i: Intent = Intent(context, NFCScanActivity::class.java)

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
                        Text(text = "NFC ENGAGEMENT")
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
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
    ){
            innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .fillMaxHeight()
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
                ,
            ){

                Row {
                    Text(
                        text = "Family name:",
                        modifier = Modifier
                            .padding(16.dp)
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