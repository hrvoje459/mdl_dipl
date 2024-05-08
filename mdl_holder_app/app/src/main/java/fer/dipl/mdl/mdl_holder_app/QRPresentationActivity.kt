package fer.dipl.mdl.mdl_holder_app

import android.R
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.android.identity.util.Logger
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import fer.dipl.mdl.mdl_holder_app.ui.theme.MDL_holder_appTheme



class QRPresentationActivity: ComponentActivity() {

    private lateinit var transferHelper: QRTransferHelper


    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transferHelper = QRTransferHelper.getInstance(applicationContext)



        var biometricPromptBuilder = BiometricPrompt.Builder(
            applicationContext
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
                super.onAuthenticationSucceeded(result)
            }

            override fun onAuthenticationFailed() {

                Logger.d("MAIN AUTH", "onAuthenticationFailed")
                super.onAuthenticationFailed()
            }

        }

        //biometricPrompt.authenticate(CancellationSignal(), mainExecutor, biocall)


        var value = ""

        val extras = intent.extras
        if (extras != null) {
            value = extras.getString("qr_code_value").toString()
            //The key argument here must match that used in the other activity
        }



        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(value, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height

        var bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        try {
            //val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    //bmp.setPixel(x, y, 4292018175.toInt())
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Int.MIN_VALUE else Int.MAX_VALUE)

                }
            }
            //(findViewById<View>(R.id.img_result_qr) as ImageView).setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }



        setContent {
            MDL_holder_appTheme {
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
                                            Logger.d("MAIN HRV", "buttons2")
                                            val transferHelper = QRTransferHelper.getInstance(applicationContext)

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
                                    //verticalArrangement = Arrangement.spacedBy(1.dp),
                                    //verticalArrangement = Arrangement.SpaceEvenly


                                ) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "some useful description",
                                        modifier = Modifier
                                            //.width((Resources.getSystem().displayMetrics.widthPixels
                                            //        / Resources.getSystem().displayMetrics.densityDpi) .dp)
                                            .size(Resources.getSystem().displayMetrics.widthPixels.dp - 500.dp)
                                            .align(Alignment.CenterHorizontally)
                                            //.align(Alignment.CenterVertically)
                                        //.padding(10.dp)
                                    )
                                    /*Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "some useful description",
                                        modifier = Modifier
                                            .size(200.dp)
                                        //.padding(innerPadding)
                                    )
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "some useful description",
                                        modifier = Modifier
                                            .size(100.dp)
                                        //.padding(innerPadding)
                                    )
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "some useful description",
                                        modifier = Modifier
                                            .size(50.dp)
                                        //.padding(innerPadding)
                                    )
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "some useful description",
                                        modifier = Modifier
                                            .size(25.dp)
                                        //.padding(innerPadding)
                                    )*/

                                    //GreetingPreview2("Mile " + value, bmp)
                                }
                        }
                    //ImageView(applicationContext).setImageBitmap(bmp)
                    /*Button(
                        onClick = {
                        Logger.d("MAIN HRV", "buttons")
                    }) {
                        GreetingPreview2("Mile " + value, bmp)
                    }*/
                }
            }
        }
    }

    @Override
    override fun onStop() {
        super.onStop()
        QRTransferHelper.kill()
        transferHelper.qrEngagementHelper?.close()
        transferHelper.deviceRetrievalHelper?.disconnect()

        Logger.d("MAIN DESTROY", "on QR exit")
    }
}




@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {

    Text(
        text = "Bogdaj $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
@Composable
fun GreetingPreview2(name: String, bitmap: Bitmap) {
    MDL_holder_appTheme {
        Greeting2(name)
        BitmapImage(bitmap)
    }

}
@Composable
fun BitmapImage(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "some useful description",

    )
}