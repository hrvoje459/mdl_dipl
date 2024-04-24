package fer.dipl.mdl

import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.X509CertChainUtils
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
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

class ECKeyTest {
}

suspend fun main(){
    val eckey = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
    println("intermediateCaKey KEY: " + eckey.exportJWK())

    val eckeyfromstr = ECKey.parse(eckey.exportJWK())
    println("EC FROM JWK: " + eckeyfromstr)


    // create CA certificate
    val rootCaCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CSP"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(eckeyfromstr.toECPublicKey().encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(eckeyfromstr.toECPrivateKey())).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }

    val x509b64 = Base64.getEncoder().encodeToString(rootCaCertificate.encoded)


    // create CA certificate
    val issuerCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CSP"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(eckeyfromstr.toECPublicKey().encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(eckeyfromstr.toECPrivateKey())).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }

    val issuerx509b64 = Base64.getEncoder().encodeToString(issuerCertificate.encoded)

    val certlist = X509CertChainUtils.toBase64List(Arrays.asList(issuerx509b64,x509b64) as List<Any>?)

    val ecbuilder = ECKey.Builder(eckeyfromstr).x509CertChain(certlist)




    println(ecbuilder.build().d)
    println(ecbuilder.build())


}