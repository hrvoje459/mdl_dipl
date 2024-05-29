package fer.dipl.mdl.mdl_invalid_app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import fer.dipl.mdl.mdl_invalid_app.ui.theme.MDL_invalid_appTheme
import com.android.identity.util.Logger


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.google.zxing.WriterException
import fer.dipl.mdl.mdl_invalid_app.activities.NFCPresentationActivity
import fer.dipl.mdl.mdl_invalid_app.activities.QRPresentationActivity
import fer.dipl.mdl.mdl_invalid_app.activities.RequestCredentialActivity
import fer.dipl.mdl.mdl_invalid_app.helpers.DrivingCredentialRequest
import fer.dipl.mdl.mdl_invalid_app.helpers.QRTransferHelper
import id.walt.mdoc.doc.MDoc
import java.util.Base64


fun decodeDataElement(data: ByteArray): DataElementHrv {
    val mapper = ObjectMapper(CBORFactory())
    return mapper.readValue(data, DataElementHrv::class.java)
}
data class DataElementHrv(
    val digestID: String,
    val random: ByteArray,
    val elementIdentifier: String,
    val elementValue: Any
){
    constructor() : this("", byteArrayOf(), "","")
    override fun toString(): String {
        var tmp = "DataElementHrv("
        tmp += "digestID=" + digestID + ", "
        tmp += "random=h'" + random.joinToString(""){ String.format("%02X", it) } + "', "
        tmp += "elementIdentifier=" + elementIdentifier + ", "
        tmp += "elementValue=" + elementValue
        tmp += ")"

        return tmp
    }
}

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

        QRTransferHelper.kill()

        var bmp = Bitmap.createBitmap(50, 50, Bitmap.Config.RGB_565)
        try {
            for (x in 0 until 50) {
                for (y in 0 until 50) {
                    bmp.setPixel(x, y, if (true) Int.MIN_VALUE else Int.MAX_VALUE)
                }
            }
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        val credential = DrivingCredentialRequest(applicationContext).getCredential(applicationContext)

        if (credential != null){
            Logger.d("CREDENTIAL ALREADY EXISTS", credential.toCBORHex())
        }


        setContent {
            MDL_invalid_appTheme {
                // A surface container using the 'background' color from the theme
                MainContent(applicationContext = applicationContext, bitmap = bmp, credential)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        QRTransferHelper.kill()
    }

    override fun onRestart() {
        super.onRestart()

        QRTransferHelper.kill()
    }
}

@Composable
fun MainContent(applicationContext: Context, bitmap: Bitmap, credential: MDoc?){
    var driving_credential_present by remember { mutableStateOf(false) }

    var family_name = ""
    var given_name:String = ""
    var portrait: String = ""
    var birth_date:String = ""
    var issue_date:String = ""
    var expiry_date:String = ""
    var issuing_country:String = ""
    var issuing_authority:String = ""
    var age_over_18:Boolean = false
    var age_over_21:Boolean = false
    var age_over_24:Boolean = false
    var age_over_65:Boolean = false
    var driving_privileges = ""

    var portrait_image: Bitmap = bitmap

    if(credential != null){
        driving_credential_present = true
        credential!!.issuerSigned.nameSpaces?.get("org.iso.18013.5.1")?.forEach {
            val data_element = decodeDataElement(it.value)

            when(data_element.elementIdentifier){
                "family_name" -> {
                    family_name = data_element.elementValue.toString()
                }
                "given_name" -> {
                    given_name = data_element.elementValue.toString()
                }
                "portrait" -> {
                    portrait = data_element.elementValue.toString()
                }
                "birth_date" -> {
                    birth_date = data_element.elementValue.toString()
                }
                "issue_date" -> {
                    issue_date = data_element.elementValue.toString()
                }
                "expiry_date" -> {
                    expiry_date = data_element.elementValue.toString()
                }
                "issuing_country" -> {
                    issuing_country = data_element.elementValue.toString()
                }
                "issuing_authority" -> {
                    issuing_authority = data_element.elementValue.toString()
                }
                "age_over_18" -> {
                    age_over_18 = data_element.elementValue as Boolean
                }
                "age_over_21" -> {
                    age_over_21 = data_element.elementValue as Boolean
                }
                "age_over_24" -> {
                    age_over_24 = data_element.elementValue as Boolean
                }
                "age_over_65" -> {
                    age_over_65 = data_element.elementValue as Boolean
                }
                "driving_privileges" -> {
                    driving_privileges = data_element.elementValue.toString()
                }

            }

        }

        Logger.d("PORTRAIT", portrait)

        val portrait_byte_array = Base64.getUrlDecoder().decode(portrait)
        portrait_image = BitmapFactory.decodeByteArray(portrait_byte_array, 0, portrait_byte_array.size)
    }






    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {},
            bottomBar = {
                if (driving_credential_present){
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Row {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    Logger.d("MAIN HRV", "buttons")
                                    val i: Intent = Intent(applicationContext, NFCPresentationActivity::class.java)
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
                                    val transferHelper =
                                        QRTransferHelper.getInstance(applicationContext)
                                    if (transferHelper.qrEng.value == ""){
                                        Logger.d("MAIN HRV", "button its null")
                                    }else{
                                        Logger.d("MAIN HRV", transferHelper.qrEng.value!!)
                                        val i: Intent = Intent(applicationContext, QRPresentationActivity::class.java)
                                        i.putExtra("qr_code_value", transferHelper.qrEng.value)
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        ContextCompat.startActivity(applicationContext, i, null)
                                    }
                                }
                            ) {
                                Text(text = "QR CODE")
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }

                    }
                }

            },

            floatingActionButton = {
                if (!driving_credential_present){
                    FloatingActionButton(
                        onClick = {
                            Logger.d("MAIN HRV", "request")
                            val i: Intent = Intent(applicationContext, RequestCredentialActivity::class.java)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ContextCompat.startActivity(applicationContext, i, null)
                        },) {
                        Text(text = "Request credential")
                    }
                }

            }

        ){
                innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (driving_credential_present){
                            Card (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                                ,
                            ){
                                Row (
                                    modifier = Modifier
                                        .height(150.dp)
                                        .fillMaxWidth()
                                        .align(Alignment.CenterHorizontally),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Image(
                                        bitmap = if (credential == null){bitmap.asImageBitmap()}else{portrait_image.asImageBitmap()},
                                        contentDescription = "some useful description",
                                        modifier = Modifier
                                            .size(125.dp)
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
                                    Text(
                                        text = given_name,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .width(200.dp)
                                            .weight(2f)
                                        ,
                                        textAlign = TextAlign.Center
                                    )
                                }
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
                                    Text(
                                        text = family_name,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .width(200.dp)
                                            .weight(2f)
                                        ,
                                        textAlign = TextAlign.Center
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
                                    Text(
                                        text = birth_date,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .width(200.dp)
                                            .weight(2f)
                                        ,
                                        textAlign = TextAlign.Center
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
                                    Text(
                                        text = expiry_date,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .width(200.dp)
                                            .weight(2f)
                                        ,
                                        textAlign = TextAlign.Center
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
                                    Text(
                                        text = issue_date,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .width(200.dp)
                                            .weight(2f)
                                        ,
                                        textAlign = TextAlign.Center
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
                                    Text(
                                        text = issuing_country,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .width(200.dp)
                                            .weight(2f)
                                        ,
                                        textAlign = TextAlign.Center
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
                                    Text(
                                        text = issuing_authority,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .width(200.dp)
                                            .weight(2f)
                                        ,
                                        textAlign = TextAlign.Center
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
                                    Text(
                                        text = driving_privileges,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .width(200.dp)
                                            .weight(2f)
                                        ,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Row {
                                    Button(
                                        onClick = {
                                            Logger.d("MAIN CONT", "DELETE CREDENTAIL")
                                            val deleted = DrivingCredentialRequest(applicationContext).deleteCredential(applicationContext)
                                            if (deleted){
                                                driving_credential_present = false
                                            }
                                        }
                                    ) {
                                        Text(text = "Delete credential")
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
        }
    }
}

