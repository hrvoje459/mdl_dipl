package fer.dipl.mdl.mdl_holder_app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.android.identity.util.Logger
import fer.dipl.mdl.mdl_holder_app.ui.theme.MDL_holder_appTheme



class NFCPresentationActivity: ComponentActivity() {

    private lateinit var nfcTransferHelper: NFCTransferHelper

    lateinit var ser_con :ServiceConnection

        @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //nfcTransferHelper = NFCTransferHelper.getInstance(applicationContext)

         ser_con = object :ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Logger.d("MAIN SERVICE CONN", "onServiceConnected")

                //ODO("Not yet implemented")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Logger.d("MAIN SERVICE CONN", "onServiceDisconnected")

                //ODO("Not yet implemented")
            }
        }

        bindService(
            Intent(this, NfcEngagementHandler::class.java),
            ser_con,
            Context.BIND_AUTO_CREATE

        )



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
                                            //val NFCTransferHelper = NFCTransferHelper.getInstance(applicationContext)

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
                            Text(text = "NFC SCANNING IN PROGRESS")
                            /*Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "some useful description",
                                modifier = Modifier
                                    //.width((Resources.getSystem().displayMetrics.widthPixels
                                    //        / Resources.getSystem().displayMetrics.densityDpi) .dp)
                                    .size(Resources.getSystem().displayMetrics.widthPixels.dp - 500.dp)
                                    .align(Alignment.CenterHorizontally)
                                //.align(Alignment.CenterVertically)
                                //.padding(10.dp)
                            )*/
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
    override fun onPause() {
        Logger.d("MAIN SERVICE STOP", "STOP")

        unbindService(ser_con)
        super.onPause()
    }
    /*@Override
    override fun onStop() {
        super.onStop()
        TransferHelper.kill()
        transferHelper.qrEngagementHelper?.close()
        transferHelper.deviceRetrievalHelper?.disconnect()

        Logger.d("MAIN DESTROY", "on QR exit")
    }*/
}



