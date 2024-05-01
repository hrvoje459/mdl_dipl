package fer.dipl.mdl.mdl_verifier_app

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.android.identity.android.mdoc.deviceretrieval.VerificationHelper
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.mdoc.connectionmethod.ConnectionMethod
import com.android.identity.mdoc.connectionmethod.ConnectionMethodBle
import com.android.identity.util.Logger
import fer.dipl.mdl.mdl_verifier_app.ui.theme.MDL_verifier_appTheme
import java.util.UUID

class ConnectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras

        val listener = object: VerificationHelper.Listener{
            override fun onReaderEngagementReady(p0: ByteArray) {
                Logger.d("MAIN CONNECTION", "onReaderEngagementReady")
                //ODO("Not yet implemented")
            }

            override fun onDeviceEngagementReceived(p0: MutableList<ConnectionMethod>) {
                Logger.d("MAIN CONNECTION", "onDeviceEngagementReceived")
                //ODO("Not yet implemented")
            }

            override fun onMoveIntoNfcField() {
                Logger.d("MAIN CONNECTION", "onMoveIntoNfcField")
                //ODO("Not yet implemented")
            }

            override fun onDeviceConnected() {
                Logger.d("MAIN CONNECTION", "onDeviceConnected")
                //ODO("Not yet implemented")
            }

            override fun onDeviceDisconnected(p0: Boolean) {
                Logger.d("MAIN CONNECTION", "onDeviceDisconnected")
                //ODO("Not yet implemented")
            }

            override fun onResponseReceived(p0: ByteArray) {
                Logger.d("MAIN CONNECTION", "onResponseReceived")
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


        val verificationHelper = verificationHelperBuilder.build()

        verificationHelper.setDeviceEngagementFromQrCode(extras!!.getString("qr_code_value").toString())

        val bleUuid = UUID.randomUUID()

        val connectionMethod =
            ConnectionMethodBle(
                false,
                true,
                null,
                bleUuid)

        verificationHelper.connect(connectionMethod)


        setContent{
            MDL_verifier_appTheme{
                if (extras!= null){
                    Greeting(extras.getString("qr_code_value").toString())
                }
            }
        }
    }
}