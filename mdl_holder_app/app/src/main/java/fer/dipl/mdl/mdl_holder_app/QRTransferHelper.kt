package fer.dipl.mdl.mdl_holder_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
//import android.hardware.biometrics.BiometricPrompt
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.identity.android.mdoc.deviceretrieval.DeviceRetrievalHelper
import com.android.identity.android.mdoc.engagement.QrEngagementHelper
import com.android.identity.android.mdoc.transport.DataTransport
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.internal.Util
import com.android.identity.mdoc.connectionmethod.ConnectionMethod
import com.android.identity.mdoc.connectionmethod.ConnectionMethodBle
import com.android.identity.securearea.SecureArea
import com.android.identity.util.Logger
import java.security.KeyPair
import java.security.PublicKey
import java.util.OptionalLong
import java.util.UUID


class QRTransferHelper(private val context: Context) {


    var qrEngagementHelper : QrEngagementHelper? = null
    var deviceRetrievalHelper: DeviceRetrievalHelper? = null

    var qrEng = MutableLiveData<String>("")

    lateinit var eDeviceKeyPair: KeyPair



    fun getQR (): LiveData<String>{
        return qrEng
    }


    companion object{
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: QRTransferHelper? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: QRTransferHelper(context).also { instance = it }
            }


        fun kill(){
            this.instance = null
        }
    }



    val listener = object: QrEngagementHelper.Listener{
        override fun onDeviceEngagementReady() {
            Logger.d("MAIN HRV", "onDeviceEngagementReady")
            Logger.d("MAIN HRV", qrEngagementHelper?.deviceEngagementUriEncoded!!)
            qrEng.value = qrEngagementHelper?.deviceEngagementUriEncoded!!
            val value = "Hello world"
            val i: Intent = Intent(context, QRPresentationActivity::class.java)
            i.putExtra("qr_code_value", qrEng.value)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(context, i, null)
            //ODO("Not yet implemented")
        }

        override fun onDeviceConnecting() {
            Logger.d("MAIN HRV", "onDeviceConnecting")
            //ODO("Not yet implemented")
        }

        override fun onDeviceConnected(p0: DataTransport) {

            Logger.d("MAIN HRV", "onDeviceConnected")


            val deviceRetrievalHelperBuilder = DeviceRetrievalHelper.Builder(
                context,
                retrievalListener,
                context.mainExecutor,
                eDeviceKeyPair
            )

            deviceRetrievalHelperBuilder.useForwardEngagement(p0, qrEngagementHelper!!.deviceEngagement, qrEngagementHelper!!.handover)

            deviceRetrievalHelper = deviceRetrievalHelperBuilder.build()


            //ODO("Not yet implemented")
        }

        override fun onError(p0: Throwable) {
            Logger.d("MAIN HRV", "onError")
            Logger.d("MAIN HRV", p0.message!!)

            //ODO("Not yet implemented")
        }

    }

    val retrievalListener = object: DeviceRetrievalHelper.Listener{
        override fun onEReaderKeyReceived(p0: PublicKey) {
            Logger.d("MAIN RET", "onEReaderKeyReceived")
            //ODO("Not yet implemented")
        }

        override fun onDeviceRequest(p0: ByteArray) {
            Logger.d("MAIN RET", "onDeviceRequest")
            Logger.d("MAIN RET", String(p0))


            deviceRetrievalHelper!!.sendDeviceResponse("delulu".toByteArray(), OptionalLong.empty())

            //ODO("Not yet implemented")
        }

        override fun onDeviceDisconnected(p0: Boolean) {
            Logger.d("MAIN RET", "onDeviceDisconnected")
            //ODO("Not yet implemented")
        }

        override fun onError(p0: Throwable) {
            Logger.d("MAIN RET", "onError")
            //ODO("Not yet implemented")
        }

    }


    val options = DataTransportOptions.Builder()
        .setBleUseL2CAP(false)
        .build()





    init {
        val eDeviceKeyCurve = SecureArea.EC_CURVE_P256
        /* val eDeviceKeyPair by lazy {
            Util.createEphemeralKeyPair(eDeviceKeyCurve)
        }*/
        eDeviceKeyPair = Util.createEphemeralKeyPair(eDeviceKeyCurve)


        val builder : QrEngagementHelper.Builder = QrEngagementHelper.Builder(
            context,
            eDeviceKeyPair.public,
            options,
            listener,
            context.mainExecutor
        )




        val connectionMethods = mutableListOf<ConnectionMethod>()
        val bleUuid = UUID.randomUUID()
        connectionMethods.add(
            ConnectionMethodBle(
            false,
            true,
            null,
            bleUuid)
        )

        builder.setConnectionMethods(connectionMethods)


        qrEngagementHelper = builder.build()







    }
}