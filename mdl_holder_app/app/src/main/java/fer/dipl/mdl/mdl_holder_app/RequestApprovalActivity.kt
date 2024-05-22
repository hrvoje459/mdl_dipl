package fer.dipl.mdl.mdl_holder_app

import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.identity.util.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import fer.dipl.mdl.mdl_holder_app.ui.theme.MDL_holder_appTheme
import id.walt.mdoc.dataretrieval.DeviceRequest
import id.walt.mdoc.docrequest.MDocRequestBuilder
import java.util.OptionalLong

class RequestApprovalActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        val extras = intent.extras


        //Logger.d("PRESENTATION", mdoc_mdoc.toCBORHex())
        Logger.d("U APPROVALU SAM", "1")

        val mdoc_request = extras!!.getByteArray("mdoc_request")
        val initiator = extras!!.getString("initiator")

        setContent {
            MDL_holder_appTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RequestRadios(applicationContext, mdoc_request!!, initiator!!)

                }
            }
        }

    }

}


@Composable
private fun RequestRadios(/*innerPadding: PaddingValues*/ context: Context, mdoc_request: ByteArray, initator: String)
{

    val request = DeviceRequest.fromCBOR(mdoc_request)
    Logger.d("U APPROVALU SAM", "APPROVAL")

    lateinit var requested_items_decoded: MyMap

    request.docRequests.first().decodedItemsRequest.nameSpaces.value.values.forEach {
        Logger.d("ITEAM:" , it.toCBORHex())
        Logger.d("DECODED ITEMS", decodeCborMap(it.toCBOR()).toString())
        requested_items_decoded = decodeCborMap(it.toCBOR())
    }
    
    var readerAuthenticated = false 
    
    if (initator == "QR"){
        val transfer_helper = QRTransferHelper.getInstance(context)
        readerAuthenticated = transfer_helper.verifyCredentialRequest(request)
    }


    var include_family_name by remember { mutableStateOf(if (requested_items_decoded.family_name!= null){true} else { false}) }
    var include_given_name by remember { mutableStateOf(if (requested_items_decoded.given_name!= null){true} else { false}) }
    var include_portrait by remember { mutableStateOf(true) }
    var include_driving_privileges by remember { mutableStateOf(true) }
    var include_age_over_18 by remember { mutableStateOf(if (requested_items_decoded.age_over_18!= null){true} else { false}) }
    var include_expiry_date by remember { mutableStateOf(true) }

    var include_document_number by remember { mutableStateOf(if (requested_items_decoded.document_number!= null){true} else { false}) }
    var include_issue_date by remember { mutableStateOf(if (requested_items_decoded.issue_date!= null){true} else { false}) }
    var include_birth_date by remember { mutableStateOf(if (requested_items_decoded.birth_date!= null){true} else { false}) }
    var include_issuing_country by remember { mutableStateOf(if (requested_items_decoded.issuing_country!= null){true} else { false}) }
    var include_issuing_authority by remember { mutableStateOf(if (requested_items_decoded.issuing_authority!= null){true} else { false}) }
    var include_age_over_21 by remember { mutableStateOf(if (requested_items_decoded.age_over_21!= null){true} else { false}) }
    var include_age_over_24 by remember { mutableStateOf(if (requested_items_decoded.age_over_24!= null){true} else { false}) }
    var include_age_over_65 by remember { mutableStateOf(if (requested_items_decoded.age_over_65!= null){true} else { false}) }








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
                            //val i: Intent = Intent(context, MainActivity::class.java)


                            //i.putExtra("requested_items", requested_items.toTypedArray())
                            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            //ContextCompat.startActivity(context, i, null)
                        }
                    ) {
                        Text(text = "Decline")
                        Icon(Icons.Default.Close, contentDescription = "Decline")
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
                            //val i: Intent = Intent(context, QRScanActivity::class.java)



                            val mdoc_request_builder = MDocRequestBuilder("org.iso.18013.5.1.mDL")
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

                            requested_items.forEach {

                                mdoc_request_builder.addDataElementRequest("org.iso.18013.5.1", it, false)
                                Logger.d("REQUESTED ITEM: ", it)
                            }

                            val user_mdoc_request = mdoc_request_builder.build()


                            var biometricPromptBuilder = BiometricPrompt.Builder(
                                context
                            )

                            biometricPromptBuilder.setTitle("HRVOJE PROMPT")
                            biometricPromptBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)

                            var biometricPrompt = biometricPromptBuilder.build()

                            var biocall = object: BiometricPrompt.AuthenticationCallback(){
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {

                                    Logger.d("MAIN AUTH", "onAuthenticationError")
                                    super.onAuthenticationError(errorCode, errString)
                                }

                                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {

                                    Logger.d("MAIN AUTH", "onAuthenticationHelp")
                                    super.onAuthenticationHelp(helpCode, helpString)
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {

                                    Logger.d("MAIN AUTH", "onAuthenticationSucceeded")

                                    val presentation = QRTransferHelper.getInstance(context).createPresentation(user_mdoc_request)



                                    QRTransferHelper.getInstance(context).deviceRetrievalHelper!!.sendDeviceResponse(presentation.toCBOR(), OptionalLong.empty())

                                    super.onAuthenticationSucceeded(result)
                                }

                                override fun onAuthenticationFailed() {

                                    Logger.d("MAIN AUTH", "onAuthenticationFailed")
                                    Logger.d("MAIN AUTH", "NISMO USPIJELI POSLATI")
                                    super.onAuthenticationFailed()
                                }

                            }

                            biometricPrompt.authenticate(CancellationSignal(), context.mainExecutor, biocall)




                            /*i.putExtra("requested_items", requested_items)

                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ContextCompat.startActivity(context, i, null)*/
                        }
                    ) {
                        Text(text = "Send response")
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

                Text(
                    text = "REQUESTED ITEMS:")

                Row {

                    Text(
                        text = "Reader verified: ")
                    if (readerAuthenticated){
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                            tint = Color.Green
                        )
                    }else{
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                            tint = Color.Red
                        )
                    }


                }

                if (requested_items_decoded.family_name != null){
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
                }
                if (requested_items_decoded.given_name != null){
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
                }

                if (requested_items_decoded.portrait != null){
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
                }
                if (requested_items_decoded.driving_privileges != null){
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
                }
                if (requested_items_decoded.expiry_date != null){
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
                }
                if (requested_items_decoded.age_over_18 != null){
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
                }
                if (requested_items_decoded.document_number != null){
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
                }
                if (requested_items_decoded.issue_date != null){
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
                }
                if (requested_items_decoded.birth_date != null){
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
                }
                if (requested_items_decoded.issuing_country != null){
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
                }
                if (requested_items_decoded.issuing_authority != null){
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
                }
                if (requested_items_decoded.age_over_21 != null){
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
                }
                if (requested_items_decoded.age_over_24 != null){
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
                }
                if (requested_items_decoded.age_over_65 != null){
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
}


fun decodeCborMap(data: ByteArray): MyMap {
    val mapper = ObjectMapper(CBORFactory())
    return mapper.readValue(data, MyMap::class.java)
}

data class MyMap(
    //val iteams_map: Map<String, Boolean>,
    //val family_name: Boolean,
    //val given_name: Boolean,
    //val issuing_authority: Boolean,
    //val issuing_country: Boolean,


    val age_over_18: Boolean?,
    val family_name: Boolean?,
    val given_name: Boolean?,
    val portrait: Boolean?,
    val driving_privileges: Boolean?,
    val expiry_date: Boolean?,

    val document_number: Boolean?,
    val issue_date: Boolean?,
    val birth_date: Boolean?,
    val issuing_country: Boolean?,
    val issuing_authority: Boolean?,
    val age_over_21: Boolean?,
    val age_over_24: Boolean?,
    val age_over_65: Boolean?
){
    // Ensure a primary constructor is available
    constructor() : this(null,null,null,null,null,null,null,null,null,null,null,null,null,null)
}