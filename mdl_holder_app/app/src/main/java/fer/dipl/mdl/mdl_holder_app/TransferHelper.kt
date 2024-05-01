package fer.dipl.mdl.mdl_holder_app

import android.content.Context
import android.content.Intent
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ContentInfoCompat.Flags
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.identity.android.mdoc.engagement.QrEngagementHelper
import com.android.identity.android.mdoc.transport.DataTransport
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.internal.Util
import com.android.identity.mdoc.connectionmethod.ConnectionMethod
import com.android.identity.mdoc.connectionmethod.ConnectionMethodBle
import com.android.identity.securearea.SecureArea
import com.android.identity.util.Logger
import java.util.UUID


class TransferHelper(private val context: Context) {


    var qrEngagementHelper : QrEngagementHelper? = null

    var qrEng = MutableLiveData<String>()

    fun getQR (): LiveData<String>{
        return qrEng
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
            //ODO("Not yet implemented")
        }

        override fun onError(p0: Throwable) {
            Logger.d("MAIN HRV", "onError")
            Logger.d("MAIN HRV", p0.message!!)

            //ODO("Not yet implemented")
        }

    }

    val options = DataTransportOptions.Builder()
        .setBleUseL2CAP(false)
        .build()





    init {
        val eDeviceKeyCurve = SecureArea.EC_CURVE_P256
        val eDeviceKeyPair by lazy {
            Util.createEphemeralKeyPair(eDeviceKeyCurve)
        }


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