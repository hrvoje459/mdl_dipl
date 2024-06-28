package fer.dipl.mdl

import COSE.AlgorithmID
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.dataretrieval.DeviceRequest
import id.walt.mdoc.docrequest.MDocRequestBuilder
import id.walt.mdoc.docrequest.MDocRequestVerificationParams
import id.walt.mdoc.readerauth.ReaderAuthentication
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.security.cert.X509Certificate
import java.util.*

class CredentialRequestAndPresentationTesting {
}

suspend fun main(){

    val ISSUER_KEY_ID = "ISSUER_KEY"
    val DEVICE_KEY_ID = "DEVICE_KEY"
    val READER_KEY_ID = "READER_KEY"
    lateinit var rootCaKeyPair: KeyPair
    lateinit var intermCaKeyPair: KeyPair
    lateinit var issuerKeyPair: KeyPair
    lateinit var deviceKeyPair: KeyPair
    lateinit var readerKeyPair: KeyPair
    lateinit var rootCaCertificate: X509Certificate
    lateinit var intermCaCertificate: X509Certificate
    lateinit var issuerCertificate: X509Certificate
    lateinit var readerCertificate: X509Certificate

    Security.addProvider(BouncyCastleProvider())
    val kpg = KeyPairGenerator.getInstance("EC")
    kpg.initialize(256)
    rootCaKeyPair = kpg.genKeyPair()
    intermCaKeyPair = kpg.genKeyPair()
    issuerKeyPair = kpg.genKeyPair()
    deviceKeyPair = kpg.genKeyPair()
    readerKeyPair = kpg.genKeyPair()

    // create CA certificate
    rootCaCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CA"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(rootCaKeyPair.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(true)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootCaKeyPair.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }

    intermCaCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CA"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC Iterm CA"),
        SubjectPublicKeyInfo.getInstance(intermCaKeyPair.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(true)) // When set to false will not pass validation as expected!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign))

        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootCaKeyPair.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }


    // create issuer certificate
    issuerCertificate = X509v3CertificateBuilder(X500Name("CN=MDOC ROOT CA"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC Test Issuer"),
        SubjectPublicKeyInfo.getInstance(issuerKeyPair.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false))
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.digitalSignature))
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootCaKeyPair.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }

    // create reader certificate
    readerCertificate = X509v3CertificateBuilder(X500Name("CN=MDOC Iterm CA"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC Test Reader"),
        SubjectPublicKeyInfo.getInstance(readerKeyPair.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false))
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.digitalSignature))
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(intermCaKeyPair.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }

    val certs = listOf(readerCertificate, intermCaCertificate, rootCaCertificate)


    println("ROOT" + rootCaCertificate)
    println("ROOT" + intermCaCertificate)
    println("ROOT" + issuerCertificate)

    // instantiate simple cose crypto provider for issuer keys and certificates
    val cryptoProvider = SimpleCOSECryptoProvider(
        listOf(
            //COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, issuerKeyPair.public, issuerKeyPair.private, listOf(issuerCertificate), listOf(rootCaCertificate)),
            //COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, issuerKeyPair.public, issuerKeyPair.private, certs, listOf(rootCaCertificate)),
            COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerKeyPair.public, readerKeyPair.private, certs, listOf(rootCaCertificate)),

            //COSECryptoProviderKeyInfo(DEVICE_KEY_ID, AlgorithmID.ECDSA_256, deviceKeyPair.public/*, deviceKeyPair.private*/)
        )
    )

    val deviceEngagementBytes = "device_eng"
    val EReaderKeyBytes = readerKeyPair.public.encoded

    val sessionTranscript = ListElement(listOf(deviceEngagementBytes.toDE(), EReaderKeyBytes.toDE()))

    val mdocRequest = MDocRequestBuilder("org.iso.18013.5.1.mDL")
        .addDataElementRequest("org.iso.18013.5.1", "family_name", true)
        .addDataElementRequest("org.iso.18013.5.1", "birth_date", false)
        .sign(sessionTranscript, cryptoProvider, READER_KEY_ID)


    val deviceRequest = DeviceRequest(listOf(mdocRequest))
    var devReqCbor = deviceRequest.toCBORHex()


    val parsedReq = DeviceRequest.fromCBORHex(devReqCbor)
    val firstParsedDocRequest = parsedReq.docRequests.first()




    val reqVerified = firstParsedDocRequest.verify(
        MDocRequestVerificationParams(
            requiresReaderAuth = true,
            READER_KEY_ID,
            allowedToRetain = mapOf("org.iso.18013.5.1" to setOf("family_name")),
            ReaderAuthentication(sessionTranscript, firstParsedDocRequest.itemsRequest)
        ), cryptoProvider
    )



    println("Request verified: $reqVerified")



    val cryptoProvider_verifier = SimpleCOSECryptoProvider(
        listOf(
            //COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, issuerKeyPair.public, issuerKeyPair.private, listOf(issuerCertificate), listOf(rootCaCertificate)),
            //COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, issuerKeyPair.public, issuerKeyPair.private, certs, listOf(rootCaCertificate)),
            COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerKeyPair.public, x5Chain =  certs, trustedRootCAs =  listOf(rootCaCertificate)),

            //COSECryptoProviderKeyInfo(DEVICE_KEY_ID, AlgorithmID.ECDSA_256, deviceKeyPair.public/*, deviceKeyPair.private*/)
        )
    )

    println("VERFIY CHAIN: " + cryptoProvider_verifier.verifyX5Chain(firstParsedDocRequest.readerAuth!!, READER_KEY_ID))




    lateinit var rootCaKeyPair_false: KeyPair
    lateinit var rootCaCertificate_false: X509Certificate


    rootCaKeyPair_false = kpg.genKeyPair()

    rootCaCertificate_false = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CA"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(rootCaKeyPair_false.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(true)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootCaKeyPair_false.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }


    val cryptoProvider_false = SimpleCOSECryptoProvider(
        listOf(
            //COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, issuerKeyPair.public, issuerKeyPair.private, listOf(issuerCertificate), listOf(rootCaCertificate)),
            //COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, issuerKeyPair.public, issuerKeyPair.private, certs, listOf(rootCaCertificate)),
            COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerKeyPair.public, x5Chain =  certs, trustedRootCAs =  listOf(rootCaCertificate_false)),

            //COSECryptoProviderKeyInfo(DEVICE_KEY_ID, AlgorithmID.ECDSA_256, deviceKeyPair.public/*, deviceKeyPair.private*/)
        )
    )

    val reqVerified_false = firstParsedDocRequest.verify(
        MDocRequestVerificationParams(
            requiresReaderAuth = true,
            READER_KEY_ID,
            allowedToRetain = mapOf("org.iso.18013.5.1" to setOf("family_name")),
            ReaderAuthentication(sessionTranscript, firstParsedDocRequest.itemsRequest)
        ), cryptoProvider_false
    )


    println("Request verified: $reqVerified_false")


   //println("VERFIY CHAIN: " + cryptoProvider_false.verifyX5Chain(firstParsedDocRequest.readerAuth!!, READER_KEY_ID))


    val cryptoProvider_false_mdoc = SimpleCOSECryptoProvider(
        listOf(
            COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, rootCaKeyPair_false.public, rootCaKeyPair_false.private, listOf(rootCaCertificate), listOf(rootCaCertificate)),
            //COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, issuerKeyPair.public, issuerKeyPair.private, certs, listOf(rootCaCertificate)),
            //COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerKeyPair.public, x5Chain =  certs, trustedRootCAs =  listOf(rootCaCertificate_false)),

            //COSECryptoProviderKeyInfo(DEVICE_KEY_ID, AlgorithmID.ECDSA_256, deviceKeyPair.public/*, deviceKeyPair.private*/)
        )
    )

    val mdocRequest_false = MDocRequestBuilder("org.iso.18013.5.1.mDL")
        .addDataElementRequest("org.iso.18013.5.1", "family_name", true)
        .addDataElementRequest("org.iso.18013.5.1", "birth_date", false)
        .sign(sessionTranscript, cryptoProvider_false_mdoc, READER_KEY_ID)

    val reqVerified_false_mdoc = mdocRequest_false.verify(
        MDocRequestVerificationParams(
            requiresReaderAuth = true,
            READER_KEY_ID,
            allowedToRetain = mapOf("org.iso.18013.5.1" to setOf("family_name")),
            ReaderAuthentication(sessionTranscript, firstParsedDocRequest.itemsRequest)
        ), cryptoProvider_verifier
    )


    println("Request verified: $reqVerified_false_mdoc")



}
