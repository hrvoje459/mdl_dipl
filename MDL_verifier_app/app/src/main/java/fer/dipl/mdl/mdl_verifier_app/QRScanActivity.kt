package fer.dipl.mdl.mdl_verifier_app

import android.content.Intent
import android.content.res.Resources
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock.sleep
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.android.identity.android.mdoc.deviceretrieval.VerificationHelper
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.mdoc.connectionmethod.ConnectionMethod
import com.android.identity.mdoc.connectionmethod.ConnectionMethodBle
import com.android.identity.util.Logger
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import fer.dipl.mdl.mdl_verifier_app.ui.theme.MDL_verifier_appTheme
import java.util.UUID

class QRScanActivity : ComponentActivity() {

    //private lateinit var verifierTransferHelper: VerifierTransferHelper
    private lateinit var transferHelper: VerifierTransferHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transferHelper = VerifierTransferHelper.getInstance(applicationContext, this)
        /*val extras = intent.extras

        var verificationHelper: VerificationHelper? = null

        val listener = object: VerificationHelper.Listener{
            override fun onReaderEngagementReady(p0: ByteArray) {
                Logger.d("MAIN CONNECTION", "onReaderEngagementReady")
                //ODO("Not yet implemented")
            }

            override fun onDeviceEngagementReceived(p0: MutableList<ConnectionMethod>) {
                Logger.d("MAIN CONNECTION", "onDeviceEngagementReceived")
                Logger.d("MAIN CONNECTION", p0.first().toString())

                /*val bleUuid = UUID.randomUUID()
                val connectionMethod =
                    ConnectionMethodBle(
                        false,
                        true,
                        null,
                        bleUuid)*/
                verificationHelper!!.connect(p0.first())
                //ODO("Not yet implemented")
            }

            override fun onMoveIntoNfcField() {
                Logger.d("MAIN CONNECTION", "onMoveIntoNfcField")
                //ODO("Not yet implemented")
            }

            override fun onDeviceConnected() {
                Logger.d("MAIN CONNECTION", "onDeviceConnected")
                //sleep(5000)
                verificationHelper!!.sendRequest("teellooou".toByteArray())
                //ODO("Not yet implemented")
            }

            override fun onDeviceDisconnected(p0: Boolean) {
                Logger.d("MAIN CONNECTION", "onDeviceDisconnected")
                //ODO("Not yet implemented")
            }

            override fun onResponseReceived(p0: ByteArray) {
                Logger.d("MAIN CONNECTION", "onResponseReceived")
                Logger.d("MAIN CONNECTION", String(p0))
                //ODO("Not yet implemented")
            }

            override fun onError(p0: Throwable) {
                Logger.d("MAIN CONNECTION", "onError")
                Logger.d("MAIN CONNECTION", p0.message.toString())

                //ODO("Not yet implemented")
            }

        }

        val verificationHelperBuilder = VerificationHelper.Builder(applicationContext, listener, applicationContext.mainExecutor)

        val options = DataTransportOptions.Builder()
            .setBleUseL2CAP(false)
            .build()

        verificationHelperBuilder.setDataTransportOptions(options)


        verificationHelper = verificationHelperBuilder.build()

        //verifierTransferHelper = VerifierTransferHelper.getInstance(applicationContext, this)

        //verificationHelper.setDeviceEngagementFromQrCode(extras!!.getString("qr_code_value").toString())

        val nfcAdapter = NfcAdapter.getDefaultAdapter(applicationContext)
        nfcAdapter.enableReaderMode(
            this,
            { tag ->
                //if (state.value == State.IDLE) {
                    verificationHelper.nfcProcessOnTagDiscovered(tag)
                    //verifierTransferHelper.verificationHelper!!.nfcProcessOnTagDiscovered(tag)
                //}
                //state.postValue(State.ENGAGING)
            },
            NfcAdapter.FLAG_READER_NFC_A + NfcAdapter.FLAG_READER_NFC_B
                    + NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK + NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null)


        //val bleUuid = UUID.randomUUID()

        /*val connectionMethod =
            ConnectionMethodBle(
                false,
                true,
                null,
                bleUuid)*/

        //verificationHelper.connect(connectionMethod)


        setContent{
            MDL_verifier_appTheme{
                if (extras!= null){
                    Greeting(extras.getString("qr_code_value").toString())
                }
            }
        }*/

        setContent {
            MDL_verifier_appTheme {
                // A surface container using the 'background' color from the theme
                /*Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }*/
                Column {
                    QrScanner(
                        onClose = { Logger.d("MAIN HRV", "close" ) },
                        qrCodeReturn = { qrtext ->
                            Logger.d("MAIN HRV", "qr_text" + qrtext)
                            //toast(qrtext)
                            //verifierTransferHelper.verificationHelper!!.setDeviceEngagementFromQrCode(qrtext)
                            //verificationHelper.setDeviceEngagementFromQrCode(qrtext)
                            transferHelper.verificationHelper!!.setDeviceEngagementFromQrCode(qrtext)
                            /*val i: Intent = Intent(applicationContext, ConnectionActivity::class.java)
                            i.putExtra("qr_code_value", qrtext)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ContextCompat.startActivity(applicationContext, i, null)*/
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            //.fillMaxSize()
                            .height(Resources.getSystem().displayMetrics.heightPixels.dp - 1500.dp)
                    )
                    Text(text = "SCAN QR OR TAP THE BACK FOR NFC ENGAGEMENT")
                }

            }
        }
    }

    override fun onStop() {
        super.onStop()
        VerifierTransferHelper.kill()
    }
}


@Composable
private fun QrScanner(
    qrCodeReturn: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Box(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            AndroidView(
                modifier = Modifier,
                factory = {
                    CodeScannerView(it).apply {
                        val codeScanner = CodeScanner(it, this).apply {
                            isAutoFocusEnabled = true
                            isAutoFocusButtonVisible = false
                            scanMode = ScanMode.SINGLE
                            decodeCallback = DecodeCallback { result ->
                                qrCodeReturn.invoke(result.text)
                                releaseResources()
                            }
                            errorCallback = ErrorCallback {
                                releaseResources()
                            }
                            camera = CodeScanner.CAMERA_BACK
                            isFlashEnabled = false
                        }
                        codeScanner.startPreview()
                    }
                },
            )
        }
        //CloseBtn(onClick = onClose)
    }
}