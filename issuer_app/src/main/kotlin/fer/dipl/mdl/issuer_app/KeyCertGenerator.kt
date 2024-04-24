package fer.dipl.mdl.issuer_app

import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.X509CertChainUtils
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import java.io.File
import java.math.BigInteger
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*


@Configuration
class KeyCertGenerator() {

    @Autowired
    private val env: MyProperties? = null

    private var initialized = false

    private var did = ""
    private var issuer_jwk = ""

    suspend fun getDid(): String{
        if (initialized == false){
            KeyCertGenerator()
        }

        return did
    }
    suspend fun getIssuerJwk(): String{
        if (initialized == false){
            KeyCertGenerator()
        }
        return issuer_jwk
    }

    suspend fun KeyCertGenerator() {

        println("CRC " + env?.country_code)

        val rootJWK = "./issuer_secrets/root_ca_jwk.json"
        val rootCert = "./issuer_secrets/root_ca_cert.json"

        val intermediateJWK = "./issuer_secrets/intermediate_ca_jwk.json"
        val intermediateCert = "./issuer_secrets/intermediate_ca_cert.json"

        val issuerJWK = "./issuer_secrets/issuer_ca_jwk.json"
        val issuerCert = "./issuer_secrets/issuer_ca_cert.json"



        val rootJWKFile = File(rootJWK)
        val rootCertFile = File(rootCert)

        val intermediateJWKFile = File(intermediateJWK)
        val intermediateCertFile = File(intermediateCert)

        val issuerJWKFile = File(issuerJWK)
        val issuerCertFile = File(issuerCert)

        var rootECKey : ECKey? = null
        var intermediateECKey : ECKey? = null
        var issuerECKey : ECKey? = null

        var rootCaCertificate: X509Certificate? = null;
        var intermediateCaCertificate: X509Certificate? = null;
        var issuerCertificate: X509Certificate? = null;

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
        } else {
            println("The file '$rootJWKFile' or '$rootCertFile' does not exist. Creating root key and cert")


            val rootCaKey = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
            //println("rootCaKey KEY: " + rootCaKey.exportJWK())

            rootECKey = ECKey.parse(rootCaKey.exportJWK())
            //println("EC FROM JWK: " + rootECKey)

            // create root CA certificate
            rootCaCertificate = X509v3CertificateBuilder(
                X500Name("CN=MDOC ROOT CSP"), BigInteger.valueOf(SecureRandom().nextLong()),
                Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
                SubjectPublicKeyInfo.getInstance(rootECKey.toECPublicKey().encoded)
            ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
                .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
                .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootECKey.toECPrivateKey())).let {
                    JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
                }

            //println(rootCaCertificate)

            rootJWKFile.writeText(rootCaKey.exportJWK())
            rootCertFile.writeText(X509CertUtils.toPEMString(rootCaCertificate))

            //println("ZAPISANO")

            // writing to files
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
        } else {
            println("The file '$intermediateJWKFile' or '$intermediateCertFile' does not exist. Creating root key and cert")


            val intermediateKey = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
            //println("intermediateCaKey KEY: " + intermediateKey.exportJWK())

            intermediateECKey = ECKey.parse(intermediateKey.exportJWK())
            //println("EC FROM JWK: " + intermediateECKey)

            // create intermediate CA certificate
            intermediateCaCertificate = X509v3CertificateBuilder(
                X500Name("CN=MDOC ROOT CA"), BigInteger.valueOf(SecureRandom().nextLong()),
                Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC Iterm CA"),
                SubjectPublicKeyInfo.getInstance(intermediateECKey.toECPublicKey().encoded)
            ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
                .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
                .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootECKey?.toECPrivateKey())).let {
                    JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
                }

            //println(intermediateCaCertificate)

            intermediateJWKFile.writeText(intermediateKey.exportJWK())
            intermediateCertFile.writeText(X509CertUtils.toPEMString(intermediateCaCertificate))

            //println("ZAPISANO")

            // writing to files


        }

        // check for ISSUER CA key and cert
        if (issuerJWKFile.exists() && issuerCertFile.exists()) {
            if (issuerJWKFile.length() == 0L || issuerCertFile.length() == 0L) {
                println("The file '$issuerJWKFile' or '$issuerCertFile' is empty.")
            } else {
                println("The files '$intermediateJWKFile' or '$issuerCertFile' are not empty.")
                issuerECKey = ECKey.parse(issuerJWKFile.readText())
                //println(issuerJWKFile.readText())
                issuerCertificate = X509CertUtils.parse(issuerCertFile.readText())
                //println(issuerCertFile.readText())

            }
        } else {
            println("The file '$issuerJWKFile' or '$issuerCertFile' does not exist. Creating root key and cert")


            val issuerKey = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
            //println("intermediateCaKey KEY: " + issuerKey.exportJWK())

            issuerECKey = ECKey.parse(issuerKey.exportJWK())
            //println("EC FROM JWK: " + issuerECKey)

            // create issuer certificate
            issuerCertificate = X509v3CertificateBuilder(
                X500Name("CN=MDOC Iterm CA"), BigInteger.valueOf(SecureRandom().nextLong()),
                Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC Issuer"),
                SubjectPublicKeyInfo.getInstance(issuerECKey.toECPublicKey().encoded)
            ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
                .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
                .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(intermediateECKey?.toECPrivateKey())).let {
                    JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
                }

            //println(issuerCertificate)

            issuerJWKFile.writeText(issuerKey.exportJWK())
            issuerCertFile.writeText(X509CertUtils.toPEMString(issuerCertificate))

            val rootB64Cert = Base64.getEncoder().encodeToString(rootCaCertificate?.encoded)
            val intermediateB64Cert = Base64.getEncoder().encodeToString(intermediateCaCertificate?.encoded)
            val issuerB64Cert = Base64.getEncoder().encodeToString(issuerCertificate?.encoded)

            val certChainList = X509CertChainUtils.toBase64List(Arrays.asList(issuerB64Cert, intermediateB64Cert, rootB64Cert) as List<Any>?)

            val jwkWithCertChain = ECKey.Builder(issuerECKey).x509CertChain(certChainList).build()

            //println(jwkWithCertChain)




            //println("ZAPISANO: " + jwkWithCertChain.toString())

            issuerJWKFile.writeText(jwkWithCertChain.toString())
            issuerCertFile.writeText(X509CertUtils.toPEMString(issuerCertificate))

            // writing to files


        }

        println("ROOT JWK: " + rootECKey)
        println("INTERMEDIATE JWK: " + intermediateECKey)
        println("ISSUER JWK: " + issuerECKey)

        did = "did:jwk:" + Base64.getEncoder().encodeToString(issuerECKey.toString().encodeToByteArray())
        issuer_jwk = issuerECKey.toString()
        //println("JWK_: " + issuer_jwk)

        //println("ROOT CERT: " + rootCaCertificate)
        //println("INTERMEDIATE CERT: " + intermediateCaCertificate)
        //println("ISSUER CERT: " + issuerCertificate)

    }
}