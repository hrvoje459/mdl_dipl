package fer.dipl.mdl.mdl_verifier_app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import com.android.identity.android.mdoc.deviceretrieval.VerificationHelper
//import com.android.identity.android.mdoc.transport.ConnectionMethodUdp
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.mdoc.connectionmethod.ConnectionMethod
import com.android.identity.mdoc.connectionmethod.ConnectionMethodBle
import com.android.identity.mdoc.connectionmethod.ConnectionMethodNfc
import com.android.identity.util.Logger
import java.util.UUID

class VerifierTransferHelper private constructor(
    private var context: Context,
    private var activity: Activity
) {

    companion object {
        private val TAG = "VerifierTransferHelper"


        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: VerifierTransferHelper? = null

        fun getInstance(context: Context, activity: Activity) =
            instance ?: synchronized(this) {
                instance ?: VerifierTransferHelper(context, activity).also { instance = it }
            }

        fun kill(){
            this.instance = null
        }
    }

    var verificationHelper: VerificationHelper? = null
    private var connectionMethodUsed: ConnectionMethod? = null

    private val listener = object : VerificationHelper.Listener {
        override fun onReaderEngagementReady(readerEngagement: ByteArray) {
            Logger.d(TAG, "onReaderEngagementReady")
        }

        override fun onDeviceEngagementReceived(connectionMethods: MutableList<ConnectionMethod>) {
            Logger.d(TAG, "onDeviceEngagementReceived")
            verificationHelper!!.connect(connectionMethods.first())
        }

        override fun onMoveIntoNfcField() {
            Logger.d(TAG, "onMoveIntoNfcField")
        }

        override fun onDeviceConnected() {
            Logger.d(TAG, "onDeviceConnected")
            verificationHelper!!.sendRequest("teellooou".toByteArray())
        }

        override fun onDeviceDisconnected(transportSpecificTermination: Boolean) {
            Logger.d(TAG, "onDeviceDisconnected, $transportSpecificTermination")
        }

        override fun onResponseReceived(deviceResponseBytes: ByteArray) {
            Logger.d(TAG, "onResponseReceived")
            Logger.d(TAG, String(deviceResponseBytes))
        }

        override fun onError(error_: Throwable) {
            Logger.d(TAG, "onError $error_")
        }
    }


    private fun initializeVerificationHelper() {
        val builder = VerificationHelper.Builder(context, listener, context.mainExecutor)
        val options = DataTransportOptions.Builder()
            .setBleUseL2CAP(false)
            .build()
        builder.setDataTransportOptions(options)

        val connectionMethods = mutableListOf<ConnectionMethod>()
        val bleUuid = UUID.randomUUID()
            connectionMethods.add(ConnectionMethodBle(
                false,
                true,
                null,
                bleUuid))
            connectionMethods.add(ConnectionMethodBle(
                true,
                false,
                bleUuid,
                null))
            connectionMethods.add(ConnectionMethodNfc(4096, 32768))

        verificationHelper?.disconnect()
        verificationHelper = builder.build()
        Logger.d(TAG, "Initialized VerificationHelper")
    }


    init {

        initializeVerificationHelper()

        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        nfcAdapter.enableReaderMode(
            activity,
            { tag ->
                    verificationHelper!!.nfcProcessOnTagDiscovered(tag)
            },
            NfcAdapter.FLAG_READER_NFC_A + NfcAdapter.FLAG_READER_NFC_B
                    + NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK + NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null)
    }

}