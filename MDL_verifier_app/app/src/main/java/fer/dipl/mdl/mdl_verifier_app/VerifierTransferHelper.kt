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
import androidx.lifecycle.MutableLiveData
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
import id.walt.mdoc.dataelement.BooleanElement
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.MapKey
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.doc.MDocVerificationParams
import id.walt.mdoc.doc.VerificationType
import id.walt.mdoc.doc.and
import id.walt.mdoc.docrequest.ItemsRequest
import id.walt.mdoc.docrequest.MDocRequest
import id.walt.mdoc.docrequest.MDocRequestBuilder
import id.walt.mdoc.mdocauth.DeviceAuthentication
import id.walt.mdoc.readerauth.ReaderAuthentication
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import java.io.ByteArrayInputStream
import java.io.File
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.UUID

@Serializable
data class MyItemsRequest  constructor (
    val docType: StringElement,
    val nameSpaces: MapElement
) {
    /**
     * Convert to CBOR map element
     */
    fun toMapElement() = buildMap {
        put(MapKey("docType"), docType)
        put(MapKey("nameSpaces"), nameSpaces)
    }.toDE()

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
    private lateinit var mdocRequest: MDocRequest
    private lateinit var requested_items: Array<String>



    var state = MutableLiveData<String>()


    fun setRequestedItems(requested_items: Array<String>){
        this.requested_items = requested_items
    }

    fun getRequestedItems(): Array<String>{
        return requested_items
    }

    private val listener = object : VerificationHelper.Listener {
        override fun onReaderEngagementReady(readerEngagement: ByteArray) {
            Logger.d(TAG, "onReaderEngagementReady")
            state.value = "Engagement Ready"
        }

        override fun onDeviceEngagementReceived(connectionMethods: MutableList<ConnectionMethod>) {
            Logger.d(TAG, "onDeviceEngagementReceived")
            verificationHelper!!.connect(connectionMethods.first())
            state.value = "Engagement received"

        }

        override fun onMoveIntoNfcField() {
            Logger.d(TAG, "onMoveIntoNfcField")
        }

        override fun onDeviceConnected() {
            Logger.d(TAG, "onDeviceConnected")

            state.value = "Device Connected"

            createMdocRequest(requested_items)

            Logger.d(TAG, id.walt.mdoc.dataretrieval.DeviceRequest(
                listOf(
                    mdocRequest
                )
            ).toCBORHex())

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
            state.value = "Device Disconnected"
        }

        override fun onResponseReceived(deviceResponseBytes: ByteArray) {
            Logger.d(TAG, "onResponseReceived")
            Logger.d(TAG, String(deviceResponseBytes))
            //Logger.d(TAG, MDoc.fromCBOR(deviceResponseBytes).toCBORHex().length.toString())
            //Logger.d(TAG, MDoc.fromCBOR(deviceResponseBytes).toCBORHex().substring(0,2000))
            //Logger.d(TAG, MDoc.fromCBOR(deviceResponseBytes).toCBORHex().substring(2000,4000))
            //Logger.d(TAG, MDoc.fromCBOR(deviceResponseBytes).toCBORHex().substring(4000,6000))
            //Logger.d(TAG, MDoc.fromCBOR(deviceResponseBytes).toCBORHex().substring(6000,8000))
            //Logger.d(TAG, MDoc.fromCBOR(deviceResponseBytes).toCBORHex().substring(8000,10000))
            //Logger.d(TAG, MDoc.fromCBOR(deviceResponseBytes).toCBORHex().substring(10000))
            state.value = "Response Received"

            val mdoc_presentation = MDoc.fromCBOR(deviceResponseBytes)

            val sessionTranscript = EncodedCBORElement(verificationHelper!!.sessionTranscript).decode() as ListElement

            val device_auth = DeviceAuthentication(sessionTranscript, "org.iso.18013.5.1.mDL", mdoc_presentation.deviceSigned!!.nameSpaces)

            Logger.d("DEVICE AUTH", device_auth.toCBOR().joinToString(""){ String.format("%02X", it) })

            var pres_chain: List<X509Certificate> = mutableListOf()

            Logger.d("CHAIN", mdoc_presentation.toCBORHex())
            Logger.d("CHAIN", mdoc_presentation.issuerSigned.toString())
            Logger.d("CHAIN", mdoc_presentation.issuerSigned.issuerAuth!!.toCBORHex())
            Logger.d("CHAIN", mdoc_presentation.issuerSigned.issuerAuth!!.x5Chain.toString())


            val presentation_chain = mdoc_presentation.issuerSigned.issuerAuth!!.x5Chain!!

            Logger.d("CHAIN", mdoc_presentation.issuerSigned.issuerAuth!!.x5Chain.toString())

            try {
                Logger.d("CHAIN", mdoc_presentation.issuerSigned.issuerAuth!!.x5Chain.toString())
                pres_chain =  CertificateFactory.getInstance("X509").generateCertificates(
                    ByteArrayInputStream(presentation_chain)
                ).map { it as X509Certificate }
            }catch (e:Exception){
                Logger.d("CHAIN EXCEPTION", e.message!!)
            }

            pres_chain.forEach{
                Logger.d("CHAIN", it.toString())
            }


            val recreated_device_key = OneKey(CBORObject.DecodeFromBytes(mdoc_presentation.MSO!!.deviceKeyInfo.deviceKey.toCBOR()))


            val trusted_roots: MutableList<X509Certificate> = mutableListOf()

            context.assets.list("trusted_roots")!!.forEach {
                Logger.d("TRUSTED ROOTS FILES", it)
                val temp_cert = context.assets.open("trusted_roots/" + it).reader().readText()
                Logger.d("TRUSTED CERT", temp_cert)
                trusted_roots.add(X509CertUtils.parse(temp_cert))
            }


            val cryptoProvider_reader = SimpleCOSECryptoProvider(listOf(
                COSECryptoProviderKeyInfo("ISSUER_KEY_ID", AlgorithmID.ECDSA_256, pres_chain.first().publicKey, x5Chain =  pres_chain, trustedRootCAs =  trusted_roots),
                COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256, recreated_device_key.AsPublicKey(), x5Chain =  pres_chain, trustedRootCAs =  listOf(pres_chain.last()))

            ))

            val dummy_request = MDocRequestBuilder("org.iso.18013.5.1.mDL")
                .addDataElementRequest("org.iso.18013.5.1", "portrait", false)
                .addDataElementRequest("org.iso.18013.5.1", "age_over_18", false)
                .build()


            val mdocVerified = mdoc_presentation.verify(
                MDocVerificationParams(
                    VerificationType.DOC_TYPE and
                            VerificationType.DEVICE_SIGNATURE and
                            VerificationType.ISSUER_SIGNATURE and
                            VerificationType.ITEMS_TAMPER_CHECK and VerificationType.VALIDITY,
                    issuerKeyID = "ISSUER_KEY_ID",
                    deviceKeyID = "DEVICE_KEY_ID",
                    deviceAuthentication = device_auth,
                    mDocRequest = dummy_request
                ), cryptoProvider_reader)

            Logger.d("MDOC VERIFIED", mdocVerified.toString())


            val issuer_signature_verified = mdoc_presentation.verifySignature(cryptoProvider_reader, "ISSUER_KEY_ID")
            val device_signature_verified = mdoc_presentation.verifyDeviceSignature(device_auth, cryptoProvider_reader, "DEVICE_KEY_ID")
            val issuer_certificate_verified = mdoc_presentation.verifyCertificate(cryptoProvider_reader, "ISSUER_KEY_ID")

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
            state.value = "Error"

        }
    }

    fun createMdocRequest(requested_items: Array<String>){

        //val country_code = "hr"
        val countries_secrets_folder = "issuer_secrets_hr"

        val READER_KEY_ID = "READER_KEY"

        var readerECKey: ECKey? = null

        var rootCaCertificate: X509Certificate? = null;
        var intermediateCaCertificate: X509Certificate? = null;
        var readerCertificate: X509Certificate? = null;

        val rootCaCertFile = context.assets.open("secrets/$countries_secrets_folder/root_ca_cert.json")
        rootCaCertificate =X509CertUtils.parse(rootCaCertFile.reader().readText())
        println("ROOT " + rootCaCertificate.toString())


        val intermediateCaCertFile = context.assets.open("secrets/$countries_secrets_folder/intermediate_ca_cert.json")
        intermediateCaCertificate =X509CertUtils.parse(intermediateCaCertFile.reader().readText())



        val readerCertFile = context.assets.open("secrets/$countries_secrets_folder/reader_cert.json")
        readerCertificate =X509CertUtils.parse(readerCertFile.reader().readText())

        val readerJwkFile = context.assets.open("secrets/$countries_secrets_folder/reader_jwk.json")

        val readerkeyjson = readerJwkFile.reader().readText()
        println("HRVOJE::" + readerkeyjson)

        readerECKey = ECKey.parse(readerkeyjson)


        val certs = listOf(readerCertificate!!, intermediateCaCertificate!!, rootCaCertificate!!)

        val readerCryptoProvider = SimpleCOSECryptoProvider(
            listOf(
                COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerECKey!!.toKeyPair().public, readerECKey.toKeyPair().private, certs, listOf(rootCaCertificate)),
            )
        )

        val sessionTranscript = EncodedCBORElement(verificationHelper!!.sessionTranscript).decode() as ListElement

        Logger.d("SEESION", sessionTranscript.toCBORHex())


        val mdoc_request_builder = MDocRequestBuilder("org.iso.18013.5.1.mDL")

        requested_items.forEach {
            mdoc_request_builder.addDataElementRequest("org.iso.18013.5.1", it, false)
            Logger.d("REQUESTED ITEM: ", it)
        }

        val enc =  EncodedCBORElement(
            MyItemsRequest(
            docType = "org.iso.18013.5.1.mDL".toDE(),
            nameSpaces = mdoc_request_builder.nameSpaces.map { ns ->
                Pair(MapKey(ns.key), ns.value.map { item ->
                    Pair(MapKey(item.key), BooleanElement(item.value))
                }.toMap().toDE())
            }.toMap().toDE()
        ).toMapElement())

        val readerAuth = readerCryptoProvider.sign1(EncodedCBORElement(ReaderAuthentication(sessionTranscript, enc).toCBOR()).toCBOR(), READER_KEY_ID)

        Logger.d("ReaderA", readerAuth.toCBORHex())


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

        nfcAdapter.disableReaderMode(activity)

        Logger.d("INIT ACTIVITY", activity.localClassName)


        nfcAdapter.enableReaderMode(
            activity,
            { tag ->
                    verificationHelper!!.nfcProcessOnTagDiscovered(tag)
            },
            NfcAdapter.FLAG_READER_NFC_A + NfcAdapter.FLAG_READER_NFC_B
                    + NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK + NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null)


        Logger.d("NFC ENABLED", nfcAdapter.isEnabled.toString())


    }

}