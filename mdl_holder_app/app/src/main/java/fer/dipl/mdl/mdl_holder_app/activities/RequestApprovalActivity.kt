package fer.dipl.mdl.mdl_holder_app.activities

import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.android.identity.util.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import fer.dipl.mdl.mdl_holder_app.MainActivity
import fer.dipl.mdl.mdl_holder_app.helpers.NFCTransferHelper
import fer.dipl.mdl.mdl_holder_app.helpers.QRTransferHelper
import fer.dipl.mdl.mdl_holder_app.ui.theme.MDL_holder_appTheme
import id.walt.mdoc.dataretrieval.DeviceRequest
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.docrequest.MDocRequestBuilder
import java.util.IllegalFormatCodePointException
import java.util.OptionalLong

class RequestApprovalActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras

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
private fun RequestRadios(context: Context, mdoc_request: ByteArray, initator: String)
{
    val request = DeviceRequest.fromCBOR(mdoc_request)

    lateinit var requested_items_decoded: RequestedItemsMap

    request.docRequests.first().decodedItemsRequest.nameSpaces.value.values.forEach {
        requested_items_decoded = decodeCborMap(it.toCBOR())
    }
    
    var readerAuthenticated = false 
    
    if (initator == "QR"){
        val transfer_helper = QRTransferHelper.getInstance(context)
        readerAuthenticated = transfer_helper.verifyCredentialRequest(request)
    }
    if (initator == "NFC"){
        val transfer_helper = NFCTransferHelper.getInstance(context)
        readerAuthenticated = transfer_helper.verifyCredentialRequest(request)
    }

    val openAlertDialog = remember { mutableStateOf(false) }

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
                Row {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            Logger.d("MAIN HRV", "buttons")
                            val i: Intent = Intent(context, MainActivity::class.java)
                            if (initator == "QR"){
                                QRTransferHelper.getInstance(context).deviceRetrievalHelper!!.sendTransportSpecificTermination()
                                QRTransferHelper.getInstance(context).deviceRetrievalHelper!!.disconnect()
                                QRTransferHelper.kill()
                            }
                            if (initator == "NFC"){
                                NFCTransferHelper.getInstance(context).deviceRetrievalHelper!!.sendTransportSpecificTermination()
                                NFCTransferHelper.getInstance(context).deviceRetrievalHelper!!.disconnect()
                                NFCTransferHelper.kill()
                            }
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ContextCompat.startActivity(context, i, null)
                        }
                    ) {
                        Text(text = "Decline")
                        Icon(Icons.Default.Close, contentDescription = "Decline")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            Logger.d("MAIN HRV", "buttons1")

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

                            biometricPromptBuilder.setTitle("Send mDL presentation")
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

                                    var presentation: MDoc?

                                    if (initator == "QR"){
                                        presentation = QRTransferHelper.getInstance(context).createPresentation(user_mdoc_request)
                                        QRTransferHelper.getInstance(context).deviceRetrievalHelper!!.sendDeviceResponse(presentation.toCBOR(), OptionalLong.empty())
                                        QRTransferHelper.kill()
                                    }
                                    if (initator == "NFC"){
                                        presentation = NFCTransferHelper.getInstance(context).createPresentation(user_mdoc_request)
                                        NFCTransferHelper.getInstance(context).deviceRetrievalHelper!!.sendDeviceResponse(presentation.toCBOR(), OptionalLong.empty())
                                        NFCTransferHelper.kill()
                                    }

                                    super.onAuthenticationSucceeded(result)

                                    openAlertDialog.value = true
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                }

                            }

                            biometricPrompt.authenticate(CancellationSignal(), context.mainExecutor, biocall)
                        }
                    ) {
                        Text(text = "Send response")
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
            if (openAlertDialog.value){
                AlertDialog(
                    title = {
                        Text(text = "Successfully sent")
                    },
                    onDismissRequest = {
                        val i: Intent = Intent(context, MainActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ContextCompat.startActivity(context, i, null)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val i: Intent = Intent(context, MainActivity::class.java)
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                ContextCompat.startActivity(context, i, null)
                            }
                        ) {
                            Text(text = "Ok")
                        }
                    },
                )
            }
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
                ,
            ){
                Row {
                    Text(
                        text = "Reader verified: ",
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(2f)
                    )
                    if (readerAuthenticated){
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(25.dp)
                                .weight(1f)
                            ,
                            tint = Color.Green,
                        )
                    }else{
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier
                                .size(128.dp)
                                .align(Alignment.CenterVertically)
                                .weight(1f)
                            ,
                            tint = Color.Red
                        )
                    }
                }

                Row {
                    Text(
                        text = "REQUESTED ITEMS:",
                        modifier = Modifier
                            .padding(5.dp)
                            .absolutePadding(left = 10.dp)
                            .width(200.dp)
                            .weight(1f)
                        ,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left
                    )
                }

                if (requested_items_decoded.family_name != null){
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
                }
                if (requested_items_decoded.given_name != null){
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
                }
                if (requested_items_decoded.portrait != null){
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
                }
                if (requested_items_decoded.driving_privileges != null){
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
                }
                if (requested_items_decoded.expiry_date != null){
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
                }
                if (requested_items_decoded.age_over_18 != null){
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
                }
                if (requested_items_decoded.document_number != null){
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
                }
                if (requested_items_decoded.issue_date != null){
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
                }
                if (requested_items_decoded.birth_date != null){
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
                }
                if (requested_items_decoded.issuing_country != null){
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
                }
                if (requested_items_decoded.issuing_authority != null){
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
                }
                if (requested_items_decoded.age_over_21 != null){
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
                }
                if (requested_items_decoded.age_over_24 != null){
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
                }
                if (requested_items_decoded.age_over_65 != null){
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
}


fun decodeCborMap(data: ByteArray): RequestedItemsMap {
    val mapper = ObjectMapper(CBORFactory())
    return mapper.readValue(data, RequestedItemsMap::class.java)
}

data class RequestedItemsMap(
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
    constructor() : this(null,null,null,null,null,null,null,null,null,null,null,null,null,null)
}