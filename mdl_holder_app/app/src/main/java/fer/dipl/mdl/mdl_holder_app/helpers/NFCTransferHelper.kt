package fer.dipl.mdl.mdl_holder_app.helpers

import COSE.AlgorithmID
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import cbor.Cbor
import com.android.identity.android.mdoc.deviceretrieval.DeviceRetrievalHelper
import com.android.identity.android.mdoc.transport.DataTransport
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.internal.Util
import com.android.identity.securearea.SecureArea
import com.android.identity.util.Logger
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.X509CertUtils
import fer.dipl.mdl.mdl_holder_app.activities.RequestApprovalActivity
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.cose.COSECryptoProvider
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.dataretrieval.DeviceRequest
import id.walt.mdoc.devicesigned.DeviceAuth
import id.walt.mdoc.devicesigned.DeviceSigned
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.docrequest.MDocRequest
import id.walt.mdoc.docrequest.MDocRequestBuilder
import id.walt.mdoc.docrequest.MDocRequestVerificationParams
import id.walt.mdoc.issuersigned.IssuerSigned
import id.walt.mdoc.issuersigned.IssuerSignedItem
import id.walt.mdoc.mdocauth.DeviceAuthentication
import id.walt.mdoc.readerauth.ReaderAuthentication
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
import java.util.OptionalLong

// Copied and modified based on https://github.com/openwallet-foundation-labs/identity-credential/blob/main/samples/preconsent-mdl/src/main/java/com/android/identity/preconsent_mdl/TransferHelper.kt
class NFCTransferHelper(private val context: Context) {

    var deviceRetrievalHelper: DeviceRetrievalHelper? = null

    var eDeviceKeyPair: KeyPair

    var state = MutableLiveData<String>()

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
            instance = null
        }
    }


    val retrievalListener = object: DeviceRetrievalHelper.Listener{
        override fun onEReaderKeyReceived(p0: PublicKey) {
            Logger.d("MAIN RET", "onEReaderKeyReceived")
            state.value = "Reader Key Received"
            //ODO("Not yet implemented")
        }

        override fun onDeviceRequest(p0: ByteArray) {
            Logger.d("MAIN RET", "onDeviceRequest")
            Logger.d("MAIN RET", String(p0))
            state.value = "Device Request Received"

            val i: Intent = Intent(context, RequestApprovalActivity::class.java)
            i.putExtra("mdoc_request", p0)
            i.putExtra("initiator", "NFC")
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(context, i, null)

            //ODO("Not yet implemented")
        }

        override fun onDeviceDisconnected(p0: Boolean) {
            Logger.d("MAIN RET", "onDeviceDisconnected")
            state.value = "Device Disconnected"
            //ODO("Not yet implemented")
        }

        override fun onError(p0: Throwable) {
            Logger.d("MAIN RET", "onError")
            state.value = "Error"
            //ODO("Not yet implemented")
        }

    }


    init {
        val eDeviceKeyCurve = SecureArea.EC_CURVE_P256
        eDeviceKeyPair = Util.createEphemeralKeyPair(eDeviceKeyCurve)
    }

    fun setConnected(
        eDeviceKeyPair: KeyPair,
        eDeviceKeyCurve: Int,
        transport: DataTransport,
        deviceEngagement: ByteArray,
        handover: ByteArray
    ) {
        deviceRetrievalHelper = DeviceRetrievalHelper.Builder(
            context,
            retrievalListener,
            context.mainExecutor,
            eDeviceKeyPair
        )
            .useForwardEngagement(transport, deviceEngagement, handover)
            .build()
    }


    fun verifyCredentialRequest(request: DeviceRequest): Boolean{

        // test change to request; should fail validation (it did)
        //request_bytes[request_bytes.size-15] = 'c'.code.toByte()

        //val parsedMapElement = Cbor.decodeFromHexString<MapElement>(request.docRequests.first().decodedItemsRequest.nameSpaces.value.values.first().toCBORHex())

        //Logger.d("REQUEST", parsedMapElement.toCBORHex())

        var certChain: List<X509Certificate>;

        request.docRequests.first().readerAuth!!.x5Chain!!.let {
            certChain = CertificateFactory.getInstance("X509").generateCertificates(
                ByteArrayInputStream(it)
            ).map { it as X509Certificate }
            Logger.d("READER AUTH: ", certChain.toString())
        }

        val countries_secrets_folder = "issuer_secrets_hr"

        var rootCaCertificate: X509Certificate? = null;

        val rootCaCertFile = context.assets.open("secrets/$countries_secrets_folder/root_ca_cert.json")
        rootCaCertificate = X509CertUtils.parse(rootCaCertFile.reader().readText())


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
                COSECryptoProviderKeyInfo("READER_KEY_ID", AlgorithmID.ECDSA_256, certChain.first().publicKey,  x5Chain = certChain, trustedRootCAs =  listOf(rootCaCertificate!!)),
                COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256,  device_key!!.toECPublicKey(), device_key!!.toECPrivateKey(), x5Chain = listOf(), trustedRootCAs =  listOf(rootCaCertificate!!)),
            )
        )

        val sessionTranscript = EncodedCBORElement(deviceRetrievalHelper!!.sessionTranscript).decode() as ListElement

        val reqVerified = request.docRequests.first().verify(
            MDocRequestVerificationParams(
                requiresReaderAuth = true,
                "READER_KEY_ID",
                allowedToRetain = mapOf("org.iso.18013.5.1" to setOf()),
                ReaderAuthentication(sessionTranscript, request.docRequests.first().itemsRequest)
            ), cryptoProvider_device
        )

        return  reqVerified && cryptoProvider_device.verifyX5Chain(request.docRequests.first().readerAuth!!, "READER_KEY_ID")

    }

    fun createPresentation(request: MDocRequest): MDoc {

        val sessionTranscript = EncodedCBORElement(deviceRetrievalHelper!!.sessionTranscript).decode() as ListElement

        var device_key : ECKey? = null

        try {
            val readFile = File(context.filesDir, "mdoc_dir/user_key.txt")
            val fIn: FileInputStream = FileInputStream(readFile)
            val myReader = BufferedReader(InputStreamReader(fIn))

            //bufferString = myReader.readText()

            device_key = ECKey.parse(myReader.readText())

            myReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cryptoProvider_device = SimpleCOSECryptoProvider(
            listOf(
                //COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256,  device_key!!.toECPublicKey(), device_key!!.toECPrivateKey(), x5Chain = listOf(), trustedRootCAs =  listOf(rootCaCertificate!!)),
                COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256,  device_key!!.toECPublicKey(), device_key!!.toECPrivateKey(), x5Chain = listOf(), trustedRootCAs =  listOf()),
            )
        )

        val device_auth = DeviceAuthentication(sessionTranscript, "org.iso.18013.5.1.mDL", request.decodedItemsRequest.nameSpaces.toEncodedCBORElement())

        // we can not use this presentWithDeviceSignature function due to https://github.com/walt-id/waltid-identity/issues/420
        /*
        val presentation = DrivingCredentialRequest(context).getCredential(context)!!
            .presentWithDeviceSignature(
                request,
                device_auth,
                cryptoProvider_device, "DEVICE_KEY_ID")
        */

        val myPresentation = presentWithDeviceSignatureHrv(
            request,
            device_auth,
            cryptoProvider_device,
            "DEVICE_KEY_ID",
            selectDisclosures(request, DrivingCredentialRequest(context).getCredential(context)!!)
        )

        //return presentation
        return  myPresentation
    }
}


// These function are extracted here and modified to get around this issue: https://github.com/walt-id/waltid-identity/issues/420
fun presentWithDeviceSignatureHrv(mDocRequest: MDocRequest, deviceAuthentication: DeviceAuthentication, cryptoProvider: COSECryptoProvider, keyID: String? = null, issuer_signed:IssuerSigned): MDoc {
    val coseSign1 = cryptoProvider.sign1(getDeviceSignedPayload(deviceAuthentication), keyID).detachPayload()
    return MDoc(
         StringElement("org.iso.18013.5.1.mDL"),
        issuer_signed,
        DeviceSigned(EncodedCBORElement(mDocRequest.decodedItemsRequest.nameSpaces), DeviceAuth(deviceSignature = coseSign1))
    )
}

fun selectDisclosures(mDocRequest: MDocRequest, credential:MDoc): IssuerSigned {
    return IssuerSigned(
        credential.issuerSigned.nameSpaces?.mapValues { entry ->
            val requestedItems = mDocRequest.getRequestedItemsFor(entry.key)
            entry.value.filter { encodedItem ->
                requestedItems.containsKey(encodedItem.decode<IssuerSignedItem>().elementIdentifier.value)
            }
        },
        credential.issuerSigned.issuerAuth
    )
}

fun getDeviceSignedPayload(deviceAuthentication: DeviceAuthentication) = EncodedCBORElement(deviceAuthentication.toDE()).toCBOR()
