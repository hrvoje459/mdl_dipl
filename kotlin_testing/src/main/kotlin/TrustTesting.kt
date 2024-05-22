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
import java.math.BigInteger
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class TrustTesting {
}


suspend fun main(){

    val READER_KEY_ID = "READER_KEY"
    lateinit var rootCaKeyPair: KeyPair
    lateinit var readerKeyPair: KeyPair
    lateinit var rootCaCertificate: X509Certificate
    lateinit var readerCertificate: X509Certificate

    Security.addProvider(BouncyCastleProvider())
    val kpg = KeyPairGenerator.getInstance("EC")
    kpg.initialize(256)
    rootCaKeyPair = kpg.genKeyPair()
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

    // create reader certificate
    readerCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CA"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC Test Reader"),
        SubjectPublicKeyInfo.getInstance(readerKeyPair.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false))
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.digitalSignature))
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootCaKeyPair.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }

    val certs = listOf(readerCertificate, rootCaCertificate)


    // instantiate simple cose crypto provider for issuer keys and certificates
    val cryptoProvider = SimpleCOSECryptoProvider(
        listOf(
            COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerKeyPair.public, readerKeyPair.private, certs, listOf(rootCaCertificate)),
        )
    )

    val readerKeyInfo = COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerKeyPair.public, readerKeyPair.private, certs, listOf(rootCaCertificate))


    val deviceEngagementBytes = "device_eng"
    val EReaderKeyBytes = readerKeyPair.public.encoded

    val sessionTranscript = ListElement(listOf(deviceEngagementBytes.toDE(), EReaderKeyBytes.toDE()))

    val mdocRequest = MDocRequestBuilder("org.iso.18013.5.1.mDL")
        .addDataElementRequest("org.iso.18013.5.1", "family_name", true)
        .addDataElementRequest("org.iso.18013.5.1", "birth_date", false)
        .sign(sessionTranscript, cryptoProvider, READER_KEY_ID)


        println("VERFIY CHAIN: "+ cryptoProvider.verifyX5Chain(mdocRequest.readerAuth!!, READER_KEY_ID))


    /*val tm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tm.init(null as? KeyStore)
    println(tm.trustManagers
        .filterIsInstance<X509TrustManager>()
        .flatMap { it.acceptedIssuers.toList() }
        .plus(readerKeyInfo.trustedRootCAs)
        .firstOrNull {
            rootCaCertificate.issuerX500Principal.name.equals(it.subjectX500Principal.name)
        })

    println(rootCaCertificate.issuerX500Principal.name.equals(readerKeyInfo.trustedRootCAs.first().subjectX500Principal.name))*/


}
