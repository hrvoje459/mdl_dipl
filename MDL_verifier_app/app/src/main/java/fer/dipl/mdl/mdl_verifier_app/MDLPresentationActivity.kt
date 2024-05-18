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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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


        //Logger.d("PRESENTATION", mdoc_mdoc.toCBORHex())

        val mdoc = extras!!.getByteArray("mdoc_bytes")
        val mdoc_mdoc = MDoc.fromCBOR(mdoc!!)

        Logger.d("PRESENTATION", mdoc_mdoc.toCBORHex())


        var bmp = Bitmap.createBitmap(50, 50, Bitmap.Config.RGB_565)
        try {
            //val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until 50) {
                for (y in 0 until 50) {
                    //bmp.setPixel(x, y, 4292018175.toInt())
                    bmp.setPixel(x, y, if (true) Int.MIN_VALUE else Int.MAX_VALUE)

                }
            }
            //(findViewById<View>(R.id.img_result_qr) as ImageView).setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        setContent {
            MDL_verifier_appTheme {
                // A surface container using the 'background' color from the theme
                MainContent(applicationContext = applicationContext, bitmap = bmp, mdoc_mdoc)
            }
        }


    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(applicationContext: Context, bitmap: Bitmap, credential: MDoc?){
    var driving_credential_present by remember { mutableStateOf(false) }
    var test_data by remember {
        mutableStateOf(0)
    }

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
        Logger.d("CREDENTIAL ALREADY EXISTS", credential!!.issuerSigned.nameSpaces?.keys.toString())
        credential!!.issuerSigned.nameSpaces?.get("org.iso.18013.5.1")?.forEach {
            Logger.d("NAME SPACE ITEAM", it.decode().value.toString())
            val temp_hrv = decodeNameSpace(it.value)
            Logger.d("NAME SPACE ITEAM", temp_hrv.toString())
            Logger.d("NAME SPACE ITEAM IDENTIFIER", temp_hrv.elementIdentifier)
            Logger.d("NAME SPACE ITEAM VALUE", temp_hrv.elementValue.toString())

            when(temp_hrv.elementIdentifier){
                "family_name" -> {
                    family_name = temp_hrv.elementValue.toString()
                }
                "given_name" -> {
                    given_name = temp_hrv.elementValue.toString()
                }
                "portrait" -> {
                    portrait = temp_hrv.elementValue.toString()
                }
                "birth_date" -> {
                    birth_date = temp_hrv.elementValue.toString()
                }
                "issue_date" -> {
                    issue_date = temp_hrv.elementValue.toString()
                }
                "expiry_date" -> {
                    expiry_date = temp_hrv.elementValue.toString()
                }
                "issuing_country" -> {
                    issuing_country = temp_hrv.elementValue.toString()
                }
                "issuing_authority" -> {
                    issuing_authority = temp_hrv.elementValue.toString()
                }
                "age_over_18" -> {
                    age_over_18 = temp_hrv.elementValue as Boolean
                }
                "age_over_21" -> {
                    age_over_21 = temp_hrv.elementValue as Boolean
                }
                "age_over_24" -> {
                    age_over_24 = temp_hrv.elementValue as Boolean
                }
                "age_over_65" -> {
                    age_over_65 = temp_hrv.elementValue as Boolean
                }
                "driving_privileges" -> {
                    driving_privileges = temp_hrv.elementValue.toString()
                }

            }

        }
        Logger.d("PARSED MDOC family_name", family_name)
        Logger.d("PARSED MDOC given_name", given_name)
        Logger.d("PARSED MDOC portrait", portrait)
        Logger.d("PARSED MDOC birth_date", birth_date)
        Logger.d("PARSED MDOC issue_date", issue_date)
        Logger.d("PARSED MDOC expiry_date", expiry_date)
        Logger.d("PARSED MDOC issuing_country", issuing_country)
        Logger.d("PARSED MDOC issuing_authority", issuing_authority)
        Logger.d("PARSED MDOC age_over_18", age_over_18.toString())
        Logger.d("PARSED MDOC age_over_21", age_over_21.toString())
        Logger.d("PARSED MDOC age_over_24", age_over_24.toString())
        Logger.d("PARSED MDOC age_over_65", age_over_65.toString())
        Logger.d("PARSED MDOC driving_privileges", driving_privileges.toString())


        val portrait_byte_array = Base64.getUrlDecoder().decode(portrait)
        portrait_image = BitmapFactory.decodeByteArray(portrait_byte_array, 0, portrait_byte_array.size)


    }






    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                     TopAppBar(title = { "Testing"})
            },
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
                    /*Row {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                Logger.d("MAIN HRV", "buttons")
                                val i: Intent = Intent(applicationContext, NFCPresentationActivity::class.java)
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
                                val transferHelper = QRTransferHelper.getInstance(applicationContext)
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
                    }*/

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

            /*floatingActionButton = {
                if (!driving_credential_present){
                    FloatingActionButton(
                        onClick = {
                            Logger.d("MAIN HRV", "request")
                            val i: Intent = Intent(applicationContext, RequestCredentialActivity::class.java)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ContextCompat.startActivity(applicationContext, i, null)
                        },) {
                        Text(text = "Request credential")
                        //Icon(Icons.Default.Add, contentDescription = "Add")
                    }
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
                    Row (
                        modifier = Modifier
                            .height(150.dp)
                            .background(Color.Cyan)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Image(
                            //bitmap = bitmap.asImageBitmap(),
                            //bitmap = credential == null ? portrait_image.asImageBitmap(),
                            bitmap = if (credential == null){bitmap.asImageBitmap()}else{portrait_image.asImageBitmap()},
                            contentDescription = "some useful description",
                            modifier = Modifier
                                //.width((Resources.getSystem().displayMetrics.widthPixels
                                //        / Resources.getSystem().displayMetrics.densityDpi) .dp)
                                .size(125.dp)
                            //.align(Alignment.CenterVertically)
                            //.padding(10.dp)
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
                        Text(
                            text = given_name,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Yellow)
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
                                .background(Color.Yellow)
                                .width(200.dp)
                                .weight(1f)
                            ,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = family_name,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Yellow)
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
                                .background(Color.Yellow)
                                .width(200.dp)
                                .weight(1f)
                            ,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = birth_date,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Yellow)
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
                                .background(Color.Yellow)
                                .width(200.dp)
                                .weight(1f)
                            ,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = expiry_date,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Yellow)
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
                                .background(Color.Yellow)
                                .width(200.dp)
                                .weight(1f)
                            ,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = issue_date,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Yellow)
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
                                .background(Color.Yellow)
                                .width(200.dp)
                                .weight(1f)
                            ,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = issuing_country,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Yellow)
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
                                .background(Color.Yellow)
                                .width(200.dp)
                                .weight(1f)
                            ,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = issuing_authority,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Yellow)
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
                                .background(Color.Yellow)
                                .width(200.dp)
                                .weight(1f)
                            ,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = driving_privileges,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Yellow)
                                .width(200.dp)
                                .weight(2f)
                            ,
                            textAlign = TextAlign.Center
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
                        Text(
                            text = portrait,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Yellow)
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
    // Ensure a primary constructor is available
    constructor() : this("", byteArrayOf(), "","")
    //constructor() : this(byteArrayOf())
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