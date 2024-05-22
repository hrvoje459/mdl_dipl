package fer.dipl.mdl.issuer_app

import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.X509CertUtils
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.keys.jwk.JWKKeyMetadata
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*

class ReaderCreation {

}

val country_code = "HR"
val secrets_folder = "issuer_secrets_hr"

suspend fun main(){


    val rootJWK = "./${secrets_folder}/root_ca_jwk.json"
    val rootCert = "./${secrets_folder}/root_ca_cert.json"

    val intermediateJWK = "./${secrets_folder}/intermediate_ca_jwk.json"
    val intermediateCert = "./${secrets_folder}/intermediate_ca_cert.json"

    val readerJWK = "./${secrets_folder}/reader_jwk.json"
    val readerCert = "./${secrets_folder}/reader_cert.json"


    val rootJWKFile = File(rootJWK)
    val rootCertFile = File(rootCert)

    val intermediateJWKFile = File(intermediateJWK)
    val intermediateCertFile = File(intermediateCert)

    val readerJWKFile = File(readerJWK)
    val readerCertFile = File(readerCert)



    var rootECKey : ECKey? = null
    var intermediateECKey : ECKey? = null

    var rootCaCertificate: X509Certificate? = null;
    var intermediateCaCertificate: X509Certificate? = null;


    // check for ROOT CA key and cert
    if (rootJWKFile.exists() && rootCertFile.exists()) {
        if (rootJWKFile.length() == 0L || rootCertFile.length() == 0L) {
            println("The file '$rootJWKFile' or '$rootCertFile' is empty.")
        } else {
            println("The files '$rootJWKFile' or '$rootCertFile' are not empty.")
            rootECKey = ECKey.parse(rootJWKFile.readText())
            //println(rootJWKFile.readText())
            rootCaCertificate = X509CertUtils.parse(rootCertFile.readText())
            //println(rootCertFile.readText())

        }
    }
    // check for INTERMEDIATE CA key and cert
    if (intermediateJWKFile.exists() && intermediateCertFile.exists()) {
        if (intermediateJWKFile.length() == 0L || intermediateCertFile.length() == 0L) {
            println("The file '$intermediateJWKFile' or '$intermediateCertFile' is empty.")
        } else {
            println("The files '$intermediateJWKFile' or '$intermediateCertFile' are not empty.")
            intermediateECKey = ECKey.parse(intermediateJWKFile.readText())
            //println(intermediateJWKFile.readText())
            intermediateCaCertificate = X509CertUtils.parse(intermediateCertFile.readText())
            //println(intermediateCertFile.readText())

        }
    }




    var readerECKey : ECKey? = null
    var readerCertificate: X509Certificate? = null;


    val readerKey = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
    readerECKey = ECKey.parse(readerKey.exportJWK())

    // create reader certificate
    readerCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC Iterm CA " +  country_code), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(1747585131000L), X500Name("CN=MDOC Reader " + country_code),
        SubjectPublicKeyInfo.getInstance(readerECKey.toECPublicKey().encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(intermediateECKey?.toECPrivateKey())).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }



    readerJWKFile.writeText(readerKey.exportJWK())
    readerCertFile.writeText(X509CertUtils.toPEMString(readerCertificate))

    println(readerCertificate)

}