package fer.dipl.mdl.mdl_invalid_app.activities

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.android.identity.util.Logger
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import fer.dipl.mdl.mdl_invalid_app.helpers.QRTransferHelper
import fer.dipl.mdl.mdl_invalid_app.ui.theme.MDL_invalid_appTheme


class QRPresentationActivity: ComponentActivity() {

    private lateinit var qrTransferHelper: QRTransferHelper


    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        qrTransferHelper = QRTransferHelper.getInstance(applicationContext)

        var value = ""

        val extras = intent.extras
        if (extras != null) {
            //The key argument here must match that used in the other activity
            value = extras.getString("qr_code_value").toString()
        }



        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(value, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height

        var bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        try {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Int.MIN_VALUE else Int.MAX_VALUE)

                }
            }
        } catch (e: WriterException) {
            e.printStackTrace()
        }



        setContent {
            MDL_invalid_appTheme {

                var stateDisplay = remember { mutableStateOf("Idle") }

                qrTransferHelper.state.observe(this as LifecycleOwner){ state ->
                    stateDisplay.value = state
                }

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
                                            Logger.d("MAIN HRV", "buttons2")
                                            val qrTransferHelper =
                                                QRTransferHelper.getInstance(applicationContext)

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
                                        .verticalScroll(
                                           rememberScrollState()
                                        ),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "some useful description",
                                        modifier = Modifier
                                            .height(Resources.getSystem().displayMetrics.heightPixels.dp - 1700.dp)
                                            .fillMaxWidth()
                                            .align(Alignment.CenterHorizontally)

                                    )
                                    Text(text = "QR SCANNING IN PROGRESS")
                                    Box(modifier = Modifier.padding(innerPadding)){
                                        CircularProgressIndicator(
                                            modifier = Modifier.width(64.dp),
                                            color = MaterialTheme.colorScheme.secondary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        )
                                    }
                                    Text(text = stateDisplay.value)
                                }
                        }
                }
            }
        }
    }

    @Override
    override fun onStop() {
        super.onStop()
        //QRTransferHelper.kill()

        Logger.d("MAIN DESTROY", "on QR exit")
    }
}


