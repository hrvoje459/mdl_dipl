package fer.dipl.mdl.mdl_verifier_app

import COSE.AlgorithmID
import COSE.OneKey
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.nfc.NfcAdapter
import android.util.Log
import androidx.core.content.ContextCompat
import cbor.Cbor
import com.android.identity.android.mdoc.deviceretrieval.VerificationHelper
//import com.android.identity.android.mdoc.transport.ConnectionMethodUdp
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.internal.Util
import com.android.identity.mdoc.connectionmethod.ConnectionMethod
import com.android.identity.mdoc.connectionmethod.ConnectionMethodBle
import com.android.identity.mdoc.connectionmethod.ConnectionMethodNfc
import com.android.identity.util.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.X509CertUtils
import com.upokecenter.cbor.CBORObject
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.dataelement.AnyDataElement
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.doc.MDocVerificationParams
import id.walt.mdoc.doc.VerificationType
import id.walt.mdoc.doc.and
import id.walt.mdoc.docrequest.MDocRequest
import id.walt.mdoc.docrequest.MDocRequestBuilder
import id.walt.mdoc.mdocauth.DeviceAuthentication
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import java.io.ByteArrayInputStream
import java.io.File
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.UUID

data class MySessionTranscript(
    val sessionTranscript: List<Any>
){
    // Ensure a primary constructor is available
    constructor() : this(listOf())
}

fun decodeCbor(data: ByteArray): MySessionTranscript {
    val mapper = ObjectMapper(CBORFactory())
    return mapper.readValue(data, MySessionTranscript::class.java)
}
class VerifierTransferHelper private constructor(
    private var context: Context,
    private var activity: Activity,
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
    private lateinit var mdocRequest: MDocRequest
    private lateinit var requested_items: Array<String>


    fun setRequestedItems(requested_items: Array<String>){
        this.requested_items = requested_items
    }

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
            //verificationHelper!!.sendRequest("teellooou".toByteArray())



            //val sessionTranscript = decodeCbor(verificationHelper!!.sessionTranscript)

            val test: AnyDataElement = EncodedCBORElement(verificationHelper!!.sessionTranscript).decode() as ListElement


            Logger.d("SESSION", test.toString())
            Logger.d("SESSION", test.toCBORHex())
            //val sessionTranscript = ListElement(listOf())


            //println(sessionTranscript)
            //Logger.d("SESSION", sessionTranscript.toString())

            createMdocRequest(requested_items)

            verificationHelper!!.sendRequest(
                id.walt.mdoc.dataretrieval.DeviceRequest(
                    listOf(
                        mdocRequest
                    )
                ).toCBOR()
            )
        }

        override fun onDeviceDisconnected(transportSpecificTermination: Boolean) {
            Logger.d(TAG, "onDeviceDisconnected, $transportSpecificTermination")
        }

        override fun onResponseReceived(deviceResponseBytes: ByteArray) {
            Logger.d(TAG, "onResponseReceived")
            Logger.d(TAG, String(deviceResponseBytes))

            val mdoc_presentation = MDoc.fromCBOR(deviceResponseBytes)

            val sessionTranscript = EncodedCBORElement(verificationHelper!!.sessionTranscript).decode() as ListElement

            val device_auth = DeviceAuthentication(sessionTranscript, "org.iso.18013.5.1.mDL", mdocRequest.decodedItemsRequest.nameSpaces.toEncodedCBORElement())

            val presentation_chain = mdoc_presentation.issuerSigned.issuerAuth!!.x5Chain!!
            val pres_chain = CertificateFactory.getInstance("X509").generateCertificates(
                ByteArrayInputStream(presentation_chain)
            ).map { it as X509Certificate }
            println("PRESENTATION CERT CHAIN: " + pres_chain)

            val recreated_device_key = OneKey(CBORObject.DecodeFromBytes(mdoc_presentation.MSO!!.deviceKeyInfo.deviceKey.toCBOR()))


            val countries_secrets_folder = "issuer_secrets_hr"
            var rootCaCertificate: X509Certificate? = null;

            // check for ROOT CA cert
            if (/*rootCertFile.exists()*/true) {
                if (/*rootCertFile.length() == 0L*/ false) {
                    //println("The file '$rootCertFile' is empty.")
                } else {
                    //println("The file '$rootCertFile' is not empty.")
                    //println(rootJWKFile.readText())
                    //println("ROOT " +  Resources.getSystem().openRawResource(R.raw.root_ca_cert_hr).reader().readText())

                    val rootCaCertFile = context.assets.open("secrets/$countries_secrets_folder/root_ca_cert.json")
                    rootCaCertificate =X509CertUtils.parse(rootCaCertFile.reader().readText())
                    println("ROOT " + rootCaCertificate.toString())
                    //rootCaCertificate = X509CertUtils.parse(rootCertFile.readText())
                    //println(rootCertFile.readText())

                }
            }

            val cryptoProvider_reader = SimpleCOSECryptoProvider(listOf(
                //COSECryptoProviderKeyInfo("ISSUER_KEY_ID", AlgorithmID.ECDSA_256, pres_chain.first().publicKey, x5Chain =  pres_chain, trustedRootCAs =  listOf(pres_chain.last())),
                COSECryptoProviderKeyInfo("ISSUER_KEY_ID", AlgorithmID.ECDSA_256, pres_chain.first().publicKey, x5Chain =  pres_chain, trustedRootCAs =  listOf(rootCaCertificate!!)),
                COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256, recreated_device_key.AsPublicKey(), x5Chain =  pres_chain, trustedRootCAs =  listOf(pres_chain.last()))

            ))

            val mdocVerified = mdoc_presentation.verify(
                MDocVerificationParams(
                    VerificationType.DOC_TYPE and VerificationType.DEVICE_SIGNATURE and VerificationType.ISSUER_SIGNATURE and VerificationType.ITEMS_TAMPER_CHECK,
                    issuerKeyID = "ISSUER_KEY_ID",
                    deviceKeyID = "DEVICE_KEY_ID",
                    deviceAuthentication = device_auth,
                    mDocRequest = mdocRequest
                ), cryptoProvider_reader)

            Logger.d("IS VALID:", mdocVerified.toString())

            val  issuer_signature_verified = mdoc_presentation.verifySignature(cryptoProvider_reader, "ISSUER_KEY_ID")
            val device_signature_verified = mdoc_presentation.verifyDeviceSignature(device_auth, cryptoProvider_reader, "DEVICE_KEY_ID")
            val issuer_certificate_verified = mdoc_presentation.verifyCertificate(cryptoProvider_reader, "ISSUER_KEY_ID")


            Logger.d("presentation verified: ", issuer_signature_verified.toString())
            Logger.d("presentation verified: ", device_signature_verified.toString())
            Logger.d("presentation verified:", issuer_certificate_verified.toString())

            val i: Intent = Intent(context, MDLPresentationActivity::class.java)
            i.putExtra("mdoc_bytes", deviceResponseBytes)
            i.putExtra("issuer_signature_verified", issuer_signature_verified)
            i.putExtra("device_signature_verified", device_signature_verified)
            i.putExtra("issuer_certificate_verified", issuer_certificate_verified)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(context, i, null)

        }

        override fun onError(error_: Throwable) {
            Logger.d(TAG, "onError $error_")
        }
    }

    fun createMdocRequest( requested_items: Array<String>){

        val country_code = "hr"
        val countries_secrets_folder = "issuer_secrets_hr"

        val READER_KEY_ID = "READER_KEY"

        //val rootCert = "./$secrets_folder/root_ca_cert.json"
        //val intermediateCert = "./$secrets_folder/intermediate_ca_cert.json"
        //val readerCert = "./$secrets_folder/reader_cert.json"

        //val readerJWK = "./$secrets_folder/reader_jwk.json"
        var readerECKey: ECKey? = null



        //val rootCertFile = File(rootCert)
        //val intermediateCertFile = File(intermediateCert)
        //val readerCertFile = File(readerCert)

        //val readerJWKFile = File(readerJWK)

        var rootCaCertificate: X509Certificate? = null;
        var intermediateCaCertificate: X509Certificate? = null;
        var readerCertificate: X509Certificate? = null;

        // check for ROOT CA cert
        if (/*rootCertFile.exists()*/true) {
            if (/*rootCertFile.length() == 0L*/ false) {
                //println("The file '$rootCertFile' is empty.")
            } else {
                //println("The file '$rootCertFile' is not empty.")
                //println(rootJWKFile.readText())
                //println("ROOT " +  Resources.getSystem().openRawResource(R.raw.root_ca_cert_hr).reader().readText())

                val rootCaCertFile = context.assets.open("secrets/$countries_secrets_folder/root_ca_cert.json")
                rootCaCertificate =X509CertUtils.parse(rootCaCertFile.reader().readText())
                println("ROOT " + rootCaCertificate.toString())
                //rootCaCertificate = X509CertUtils.parse(rootCertFile.readText())
                //println(rootCertFile.readText())

            }
        }

        // check for INTERMEDIATE CA cert
        if (/*intermediateCertFile.exists()*/true) {
            if (/*intermediateCertFile.length() == 0L*/ false) {
                //println("The file '$intermediateCertFile' is empty.")
            } else {
                //println("The file '$intermediateCertFile' is not empty.")
                //println(intermediateJWKFile.readText())
                //intermediateCaCertificate = X509CertUtils.parse(intermediateCertFile.readText())
                val intermediateCaCertFile = context.assets.open("secrets/$countries_secrets_folder/intermediate_ca_cert.json")
                intermediateCaCertificate =X509CertUtils.parse(intermediateCaCertFile.reader().readText())

                //intermediateCaCertificate = X509CertUtils.parse(Resources.getSystem().openRawResource(R.raw.intermediate_ca_cert_hr).reader().readText())
                //println(intermediateCertFile.readText())

            }
        }

        // check for READER key and cert
        if (/*readerJWKFile.exists() && readerCertFile.exists()*/ true) {
            if (/*readerJWKFile.length() == 0L || intermediateCertFile.length() == 0L*/ false) {
                //println("The file '$readerJWKFile' or '$readerCertFile' is empty.")
            } else {
                //println("The files '$readerJWKFile' or '$readerCertFile' are not empty.")
                //readerECKey = ECKey.parse(readerJWKFile.readText())
                val readerCertFile = context.assets.open("secrets/$countries_secrets_folder/reader_cert.json")
                readerCertificate =X509CertUtils.parse(readerCertFile.reader().readText())

                val readerJwkFile = context.assets.open("secrets/$countries_secrets_folder/reader_jwk.json")
                //rootCaCertificate =X509CertUtils.parse(readerJwkFile.reader().readText())

                val readerkeyjson = readerJwkFile.reader().readText()
                println("HRVOJE::" + readerkeyjson)

                readerECKey = ECKey.parse(readerkeyjson)
                //readerECKey = ECKey.parse(Resources.getSystem().openRawResource(R.raw.reader_jwk_hr).reader().readText())
                //println(intermediateJWKFile.readText())
                //readerCertificate = X509CertUtils.parse(readerCertFile.readText())
                //readerCertificate = X509CertUtils.parse(Resources.getSystem().openRawResource(R.raw.reader_cert_hr).reader().readText())

                //println(intermediateCertFile.readText())

            }
        }


        val certs = listOf(readerCertificate!!, intermediateCaCertificate!!, rootCaCertificate!!)

        val readerCryptoProvider = SimpleCOSECryptoProvider(
            listOf(
                COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerECKey!!.toKeyPair().public, readerECKey.toKeyPair().private, certs, listOf(rootCaCertificate)),
            )
        )


        //val deviceEngagementBytes =  verificationHelper. "engagement_bytes".toByteArray()
        val EReaderKeyBytes = readerECKey.toKeyPair().public.encoded
        //val sessionTranscript = Cbor.decodeFromByteArray<ListElement>(verificationHelper!!.sessionTranscript)
        //val sessionTranscript = ListElement()

        val sessionTranscript = EncodedCBORElement(verificationHelper!!.sessionTranscript).decode() as ListElement

        //val sessionTranscript = ListElement(listOf(deviceEngagementBytes.toDE(), EReaderKeyBytes.toDE()))
        //val sessionTranscript = Cbor.decodeFromByteArray<ListElement>(verificationHelper!!.sessionTranscript)



        //val requested_elements = dataelements
        val requested_elements = arrayOf("family_name", "given_name", "issuing_authority", "portrait")

        val mdoc_request_builder = MDocRequestBuilder("org.iso.18013.5.1.mDL")


        requested_items.forEach {

            mdoc_request_builder.addDataElementRequest("org.iso.18013.5.1", it, false)
            Logger.d("REQUESTED ITEM: ", it)
        }


        val mdoc_request = mdoc_request_builder.sign(sessionTranscript, readerCryptoProvider, READER_KEY_ID)


        this.mdocRequest = mdoc_request;
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