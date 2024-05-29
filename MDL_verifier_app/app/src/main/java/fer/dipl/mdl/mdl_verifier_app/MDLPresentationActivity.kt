package fer.dipl.mdl.mdl_verifier_app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.android.identity.mdoc.util.MdocUtil
import id.walt.mdoc.doc.MDoc
import com.android.identity.util.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.google.zxing.WriterException
import fer.dipl.mdl.mdl_verifier_app.ui.theme.MDL_verifier_appTheme
import java.util.Base64


class MDLPresentationActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        val extras = intent.extras

        VerifierTransferHelper.getInstance(applicationContext, this)
            .verificationHelper!!.disconnect()


        val mdoc = extras!!.getByteArray("mdoc_bytes")
        val mdoc_mdoc = MDoc.fromCBOR(mdoc!!)


        val issuer_signature_verified = extras.getBoolean ("issuer_signature_verified")
        val device_signature_verified = extras.getBoolean ("device_signature_verified")
        val issuer_certificate_verified = extras.getBoolean ("issuer_certificate_verified")

        Logger.d("PRESENTATION", mdoc_mdoc.toCBORHex())


        var bmp = Bitmap.createBitmap(50, 50, Bitmap.Config.RGB_565)
        try {
            for (x in 0 until 50) {
                for (y in 0 until 50) {
                    //bmp.setPixel(x, y, 4292018175.toInt())
                    bmp.setPixel(x, y, if (true) Int.MIN_VALUE else Int.MAX_VALUE)
                }
            }
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        setContent {
            MDL_verifier_appTheme {
                // A surface container using the 'background' color from the theme
                MainContent(applicationContext = applicationContext, bitmap = bmp, mdoc_mdoc, issuer_signature_verified, device_signature_verified, issuer_certificate_verified)
            }
        }


    }
}



@Composable
fun MainContent(applicationContext: Context, bitmap: Bitmap, credential: MDoc?, issuer_signature_verified:Boolean, device_signature_verified: Boolean, issuer_certificate_verified:Boolean){

    var family_name = ""
    var given_name:String = ""
    var portrait: String = ""
    var birth_date:String = ""
    var issue_date:String = ""
    var expiry_date:String = ""
    var issuing_country:String = ""
    var issuing_authority:String = ""
    var age_over_18:Boolean? = null
    var age_over_21:Boolean? = null
    var age_over_24:Boolean? = null
    var age_over_65:Boolean? = null
    var driving_privileges = ""
    var document_number = ""

    var portrait_image: Bitmap = bitmap

    if(credential != null){
        Logger.d("CREDENTIAL ALREADY EXISTS", credential!!.issuerSigned.nameSpaces?.keys.toString())
        credential!!.issuerSigned.nameSpaces?.get("org.iso.18013.5.1")?.forEach {
            val data_element = decodeNameSpace(it.value)

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
                "document_number" -> {
                    document_number = data_element.elementValue.toString()
                }

            }

        }

        val portrait_byte_array = Base64.getUrlDecoder().decode(portrait)
        portrait_image = BitmapFactory.decodeByteArray(portrait_byte_array, 0, portrait_byte_array.size)

    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
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
                                val i: Intent = Intent(applicationContext, MainActivity::class.java)
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                ContextCompat.startActivity(applicationContext, i, null)
                            }
                        ) {
                            Text(text = "Exit")
                            Icon(Icons.Default.ExitToApp, contentDescription = "Exit")
                        }

                    }
                }
            }
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
                    Row (
                        modifier = Modifier
                            .height(150.dp)
                            .background(MaterialTheme.colorScheme.primary)
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

                    if (given_name.isNotEmpty()){
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
                    }
                    if (family_name.isNotEmpty()){
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
                    }
                    if (birth_date.isNotEmpty()){
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
                    }
                    if (expiry_date.isNotEmpty()){
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
                    }
                    if (issue_date.isNotEmpty()){
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
                    }
                    if (document_number.isNotEmpty()){
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
                            Text(
                                text = document_number,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .width(200.dp)
                                    .weight(2f)
                                ,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (issuing_country.isNotEmpty()){
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
                    }
                    if (issuing_authority.isNotEmpty()){
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
                    }
                    if (driving_privileges.isNotEmpty()){
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
                    }
                    if (age_over_18 != null){
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
                            Text(
                                text = age_over_18.toString(),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .width(200.dp)
                                    .weight(2f)
                                ,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (age_over_21 != null){
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
                            Text(
                                text = age_over_21.toString(),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .width(200.dp)
                                    .weight(2f)
                                ,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (age_over_24 != null){
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
                            Text(
                                text = age_over_24.toString(),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .width(200.dp)
                                    .weight(2f)
                                ,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (age_over_65 != null){
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
                            Text(
                                text = age_over_65.toString(),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .width(200.dp)
                                    .weight(2f)
                                ,
                                textAlign = TextAlign.Center
                            )
                        }
                    }





                    Row {
                        Text(
                            text = "Verification:",
                            modifier = Modifier
                                .padding(16.dp)
                                .width(200.dp)
                                .weight(1f)
                            ,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Left
                        )
                    }
                    Row {
                        Text(
                            text = "Issuer signature verified: ",
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(2f)
                        )
                        if (issuer_signature_verified){
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
                            text = "Device signature verified: ",
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(2f)
                        )
                        if (device_signature_verified){
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
                                    .size(25.dp)
                                    .align(Alignment.CenterVertically)
                                    .weight(1f)
                                ,
                                tint = Color.Red
                            )
                        }
                    }

                    Row {
                        Text(
                            text = "Issuer certificate verified: ",
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(2f)
                        )
                        if (issuer_certificate_verified){
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
                                    .size(25.dp)
                                    .align(Alignment.CenterVertically)
                                    .weight(1f)
                                ,
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}


fun decodeNameSpace(data: ByteArray): DataElementHrv {
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