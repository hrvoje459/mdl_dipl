package fer.dipl.mdl.mdl_holder_app.helpers

import COSE.AlgorithmID
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
//import android.hardware.biometrics.BiometricPrompt
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cbor.Cbor
import com.android.identity.android.mdoc.deviceretrieval.DeviceRetrievalHelper
import com.android.identity.android.mdoc.engagement.QrEngagementHelper
import com.android.identity.android.mdoc.transport.DataTransport
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.internal.Util
import com.android.identity.mdoc.connectionmethod.ConnectionMethod
import com.android.identity.mdoc.connectionmethod.ConnectionMethodBle
import com.android.identity.mdoc.engagement.EngagementParser
import com.android.identity.mdoc.engagement.EngagementParser.Engagement
import com.android.identity.mdoc.sessionencryption.SessionEncryption
import com.android.identity.securearea.SecureArea
import com.android.identity.util.Logger
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.X509CertUtils
import fer.dipl.mdl.mdl_holder_app.activities.QRPresentationActivity
import fer.dipl.mdl.mdl_holder_app.activities.RequestApprovalActivity
import fer.dipl.mdl.mdl_holder_app.activities.decodeCborMap
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.dataretrieval.DeviceRequest
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.docrequest.MDocRequest
import id.walt.mdoc.docrequest.MDocRequestBuilder
import id.walt.mdoc.docrequest.MDocRequestVerificationParams
import id.walt.mdoc.mdocauth.DeviceAuthentication
import id.walt.mdoc.readerauth.ReaderAuthentication
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromHexString
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.security.KeyPair
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.UUID


// Copied and modified based on https://github.com/openwallet-foundation-labs/identity-credential/blob/main/samples/preconsent-mdl/src/main/java/com/android/identity/preconsent_mdl/TransferHelper.kt
class QRTransferHelper(private val context: Context) {


    var qrEngagementHelper : QrEngagementHelper? = null
    var deviceRetrievalHelper: DeviceRetrievalHelper? = null

    var qrEng = MutableLiveData<String>("")

    var state = MutableLiveData<String>()

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
            instance = null
        }
    }



    val listener = object: QrEngagementHelper.Listener{
        override fun onDeviceEngagementReady() {
            Logger.d("MAIN HRV", "onDeviceEngagementReady")
            Logger.d("MAIN HRV", qrEngagementHelper?.deviceEngagementUriEncoded!!)
            qrEng.value = qrEngagementHelper?.deviceEngagementUriEncoded!!

            val i: Intent = Intent(context, QRPresentationActivity::class.java)
            i.putExtra("qr_code_value", qrEng.value)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(context, i, null)
            //ODO("Not yet implemented")
        }

        override fun onDeviceConnecting() {
            Logger.d("MAIN HRV", "onDeviceConnecting")
            state.value = "Connecting"
            //ODO("Not yet implemented")
        }

        override fun onDeviceConnected(p0: DataTransport) {

            Logger.d("MAIN HRV", "onDeviceConnected")
            state.value = "Connected"


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

            state.value = "Request received"

            val i: Intent = Intent(context, RequestApprovalActivity::class.java)
            i.putExtra("mdoc_request", p0)
            i.putExtra("initiator", "QR")
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(context, i, null)

            //ODO("Not yet implemented")
        }

        override fun onDeviceDisconnected(p0: Boolean) {
            Logger.d("MAIN RET", "onDeviceDisconnected")
            kill()
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

    fun verifyCredentialRequest(request: DeviceRequest): Boolean{

        // test change to request; should fail validation (it did)
        //request_bytes[request_bytes.size-15] = 'c'.code.toByte()

        var certChain: List<X509Certificate>;

        request.docRequests.first().readerAuth!!.x5Chain!!.let {
            certChain = CertificateFactory.getInstance("X509").generateCertificates(
                ByteArrayInputStream(it)
            ).map { it as X509Certificate }
            Logger.d("READER AUTH: ", certChain.toString())
        }


        val countries_secrets_folder = "issuer_secrets_hr"
        //var rootCaCertificate: X509Certificate? = null;

        var trustedRootsList = mutableListOf<X509Certificate>()


        context.assets.list("secrets")!!.forEach {

            Logger.d("CERTIFICATES", it)
            val rootCaCertFile = context.assets.open("secrets/$it/root_ca_cert.pem")
            val rootCaCertificate = X509CertUtils.parse(rootCaCertFile.reader().readText())

            trustedRootsList.add(rootCaCertificate)

        }

        //val rootCaCertFile = context.assets.open("secrets/$countries_secrets_folder/root_ca_cert.pem")
        //rootCaCertificate = X509CertUtils.parse(rootCaCertFile.reader().readText())

        var device_key : ECKey? = null

        try {
            val readFile = File(context.filesDir, "mdoc_dir/user_key.txt")
            val fIn: FileInputStream = FileInputStream(readFile)
            val myReader = BufferedReader(InputStreamReader(fIn))

            device_key = ECKey.parse(myReader.readText())

            myReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cryptoProvider_device = SimpleCOSECryptoProvider(
            listOf(
                //COSECryptoProviderKeyInfo("READER_KEY_ID", AlgorithmID.ECDSA_256, certChain.first().publicKey,  x5Chain = certChain, trustedRootCAs =  listOf(rootCaCertificate!!)),
                COSECryptoProviderKeyInfo("READER_KEY_ID", AlgorithmID.ECDSA_256, certChain.first().publicKey,  x5Chain = certChain, trustedRootCAs =  trustedRootsList),
                COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256,  device_key!!.toECPublicKey(), device_key!!.toECPrivateKey(), x5Chain = listOf(), trustedRootCAs =  trustedRootsList),
                )
        )

        val sessionTranscript = EncodedCBORElement(deviceRetrievalHelper!!.sessionTranscript).decode() as ListElement

        val reqVerified = request.docRequests.first().verify(
            MDocRequestVerificationParams(
                requiresReaderAuth = true,
                "READER_KEY_ID",
                allowedToRetain = mapOf("org.iso.18013.5.1" to setOf("family_name","given_name","issuing_authority", "portrait")),
                ReaderAuthentication(sessionTranscript, request.docRequests.first().itemsRequest)
            ), cryptoProvider_device
        )
        Logger.d("MDOC SIGNATURE VERIFIED", reqVerified.toString())
        Logger.d("VERFIY CHAIN: ", cryptoProvider_device.verifyX5Chain(request.docRequests.first().readerAuth!!, "READER_KEY_ID").toString())

        return  reqVerified && cryptoProvider_device.verifyX5Chain(request.docRequests.first().readerAuth!!, "READER_KEY_ID")

    }

    fun createPresentation(request: MDocRequest): MDoc {

        Logger.d("REQUEST ITEMS HRV", request.itemsRequest.toCBORHex())

        val sessionTranscript = EncodedCBORElement(deviceRetrievalHelper!!.sessionTranscript).decode() as ListElement

        var device_key : ECKey? = null

        try {
            val readFile = File(context.filesDir, "mdoc_dir/user_key.txt")
            val fIn: FileInputStream = FileInputStream(readFile)
            val myReader = BufferedReader(InputStreamReader(fIn))

            device_key = ECKey.parse(myReader.readText())

            myReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        val countries_secrets_folder = "issuer_secrets_hr"
        var rootCaCertificate: X509Certificate? = null;

        val rootCaCertFile = context.assets.open("secrets/$countries_secrets_folder/root_ca_cert.json")
        rootCaCertificate = X509CertUtils.parse(rootCaCertFile.reader().readText())

        val cryptoProvider_device = SimpleCOSECryptoProvider(
            listOf(
                COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256,  device_key!!.toECPublicKey(), device_key!!.toECPrivateKey(), x5Chain = listOf(), trustedRootCAs =  listOf(rootCaCertificate!!)),
            )
        )

        val device_auth = DeviceAuthentication(sessionTranscript, "org.iso.18013.5.1.mDL", EncodedCBORElement(MapElement(mapOf())))

        val presentation = DrivingCredentialRequest(context).getCredential(context)!!
            .presentWithDeviceSignature(
                request,
                device_auth,
                cryptoProvider_device, "DEVICE_KEY_ID")


        return presentation
    }

}

