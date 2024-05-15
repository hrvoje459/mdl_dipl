package fer.dipl.mdl.mdl_holder_app

import android.annotation.SuppressLint
import android.content.Context
import com.android.identity.android.mdoc.deviceretrieval.DeviceRetrievalHelper
import com.android.identity.android.mdoc.transport.DataTransport
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.internal.Util
import com.android.identity.securearea.SecureArea
import com.android.identity.util.Logger
import com.android.identity.util.Timestamp
import java.security.KeyPair
import java.security.PublicKey
import java.util.OptionalLong

class NFCTransferHelper(private val context: Context) {

    var deviceRetrievalHelper: DeviceRetrievalHelper? = null

    lateinit var eDeviceKeyPair: KeyPair

    companion object{
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: NFCTransferHelper? = null

        private val TAG = "TransferHelper"


        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: NFCTransferHelper(context).also { instance = it }
            }


        fun kill(){
            this.instance = null
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


            //deviceRetrievalHelper!!.sendDeviceResponse("delulu".toByteArray(), OptionalLong.empty())

            val credential = DrivingCredentialRequest(context).getCredential(context)
            deviceRetrievalHelper!!.sendDeviceResponse(credential!!.toCBOR(), OptionalLong.empty())


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
    }

    fun setConnected(
        eDeviceKeyPair: KeyPair,
        eDeviceKeyCurve: Int,
        transport: DataTransport,
        deviceEngagement: ByteArray,
        handover: ByteArray
    ) {
        //scanningDurationMillis = 0
        deviceRetrievalHelper = DeviceRetrievalHelper.Builder(
            context,
            object : DeviceRetrievalHelper.Listener {
                override fun onEReaderKeyReceived(eReaderKey: PublicKey) {
                    Logger.d(TAG, "onEReaderKeyReceived")
                }

                override fun onDeviceRequest(deviceRequestBytes: ByteArray) {
                    Logger.d(TAG, "onDeviceRequest")
                    Logger.d("MAIN RET", "testing27")


                    //deviceRetrievalHelper!!.sendDeviceResponse("delulu".toByteArray(), OptionalLong.empty())
                    val credential = DrivingCredentialRequest(context).getCredential(context)
                    deviceRetrievalHelper!!.sendDeviceResponse(credential!!.toCBOR(), OptionalLong.empty())

                    Logger.d("MAIN RET", "testing28")
                    //deviceRequest = deviceRequestBytes
                    //timestampRequestAvailable = Timestamp.now().toEpochMilli()
                    //scanningDurationMillis = deviceRetrievalHelper?.scanningTimeMillis ?: 0
                    //state.value = State.REQUEST_AVAILABLE
                }

                override fun onDeviceDisconnected(transportSpecificTermination: Boolean) {
                    Logger.d(TAG, "onDeviceDisconnected $transportSpecificTermination")
                    deviceRetrievalHelper?.disconnect()
                    deviceRetrievalHelper = null
                    //state.value = State.NOT_CONNECTED
                }

                override fun onError(error: Throwable) {
                    Logger.d(TAG, "onError", error)
                    deviceRetrievalHelper?.disconnect()
                    deviceRetrievalHelper = null
                    //state.value = State.NOT_CONNECTED
                }

            },
            context.mainExecutor,
            eDeviceKeyPair)
            .useForwardEngagement(transport, deviceEngagement, handover)
            .build()
        //connectionMethod = transport.connectionMethod
        //state.value = State.CONNECTED
    }
}