package fer.dipl.mdl

import com.nimbusds.jose.jwk.*
import com.nimbusds.jose.jwk.gen.JWKGenerator
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator
import com.nimbusds.jose.util.X509CertChainUtils
import com.nimbusds.jose.util.X509CertUtils
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.keys.jwk.JWKKeyMetadata
import id.walt.did.dids.registrar.dids.DidJwkCreateOptions
import id.walt.did.dids.registrar.local.jwk.DidJwkRegistrar
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPrivateKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*


class Testing {
}

fun bytesToHex(bytes: ByteArray): String {
    val hexString = StringBuilder()
    for (b in bytes) {
        val hex = Integer.toHexString(0xff and b.toInt())
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }
    return hexString.toString()
}

suspend fun main(){
    val key = JWKKey.generate(KeyType.Ed25519, JWKKeyMetadata())

    println("JWK: " + key.exportJWK())
    println(key.getPublicKey().jwk)
    println(key.getPublicKeyRepresentation().joinToString(""){ String.format("%02X", it) } )
    println()

    val secureRandom = SecureRandom()
    Security.addProvider(BouncyCastleProvider())
    val kpg = KeyPairGenerator.getInstance("Ed25519", "BC")
    kpg.initialize(256, secureRandom)


    val rootCaKeyPair = kpg.genKeyPair()
    //val params = (rootCaKeyPair.public as java.security.interfaces.ECPublicKey).params

    println("ZIVOTE: " + rootCaKeyPair.private)
    println("ZIVOTE: " + rootCaKeyPair.private.encoded.joinToString(""){ String.format("%02X", it) })
    println("ZIVOTE: " + rootCaKeyPair.public.encoded.joinToString(""){ String.format("%02X", it) })

    println("ZIVOTE: " + rootCaKeyPair.private.javaClass)

    val pkcs8KeySpec = PKCS8EncodedKeySpec(rootCaKeyPair.private.encoded)
    val keyFactory = java.security.KeyFactory.getInstance("Ed25519", "BC")
    val privkey = keyFactory.generatePrivate(pkcs8KeySpec)

    println("ZIVOTE: " + privkey)
    println("ZIVOTE: " + privkey.javaClass)



    //val keyGenerator: JWKGenerator<out JWK> = OctetKeyPairGenerator(Curve.Ed25519).keyIDFromThumbprint(true)
    val keyGenerator: JWKGenerator<out JWK> = OctetKeyPairGenerator(Curve.P_521).keyIDFromThumbprint(true)


    val keytry = keyGenerator.generate()
    keytry.toOctetKeyPair().x.decode()

    val key2 = ECKey.Builder(Curve.P_521, keytry.toOctetKeyPair().x, keytry.toOctetKeyPair().d)
        .keyID(keytry.keyID)
        .build()

    println(key2)



    //println(params.curve)
    //val ec_key  =  ECKey.Builder()


    val temp_jwk : OctetSequenceKey = OctetSequenceKey.Builder(rootCaKeyPair.private.encoded).build()
    println("TEMP_JWK: " + temp_jwk)
    val temp_jwk2 : OctetSequenceKey = OctetSequenceKey.Builder(rootCaKeyPair.public.encoded).build()
    println("TEMP_JWK: " + temp_jwk2)

    //val rootCaKeyPair = KeyPair(key.getPublicKey(), key.)
    val publicKeyBytes = rootCaKeyPair.getPublic().getEncoded();
    val privateKeyBytes = rootCaKeyPair.getPrivate().getEncoded();

    val publicKeyBase64 = Base64.getUrlEncoder().encodeToString(rootCaKeyPair.public.encoded)
    val privateKeyBase64 = Base64.getUrlEncoder().encodeToString(rootCaKeyPair.private.encoded)

    // Convert the bytes to hexadecimal strings
    //val publicKeyHex = bytesToHex(publicKeyBytes);
    //val privateKeyHex = bytesToHex(privateKeyBytes);

    println("Public encoded bytes: " + rootCaKeyPair.public.encoded.joinToString(""){ String.format("%02X", it) })
    println("Private encoded bytes: " + rootCaKeyPair.private.encoded.joinToString(""){ String.format("%02X", it) })
    //println("PUBLIC: " + publicKeyHex)
    //println("PRIVATE: " + privateKeyHex)

    println("Public base64: " + publicKeyBase64)
    println("Private base64: " + privateKeyBase64)



    // create CA certificate
    val rootCaCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CSP"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(rootCaKeyPair.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootCaKeyPair.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }
    val test: Ed25519PrivateKeyParameters

    //println("CERT: " + rootCaCertificate.encoded.joinToString(""){ String.format("%02X", it) })





    println("X: " + keytry.toOctetKeyPair().x)
    println("D: " + keytry.toOctetKeyPair().d)
    //println("KID: " + keytry.toOctetKeyPair().keyID)

    //val publicKeyBase64 = Base64.getEncoder().encodeToString(rootCaKeyPair.getPublic().getEncoded())
    //val privateKeyBase64 = Base64.getEncoder().encodeToString(rootCaKeyPair.getPrivate().getEncoded())

    println("DECODE: " + keytry.toOctetKeyPair().x)

    //val temp : PrivateKey = PrivateKey(keytry.toOctetKeyPair().x)


    // create CA certificate
    /*val hrvojeCaCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CSP"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(rootCaKeyPair.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("Ed25519").setProvider("BC").build(keytry.toOctetKeyPair().d)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }
*/

    val testkey = JWKKey(keytry)

    println(testkey.jwk)

    val registrar = DidJwkRegistrar()
    // Create end user did
    val user_options = DidJwkCreateOptions(
        keyType = KeyType.Ed25519
    )
    val user_didResult = registrar.registerByKey(testkey, user_options)
    println("DID: " + user_didResult.did)




    val der_cert = rootCaCertificate.encoded.joinToString(""){ String.format("%02X", it) }
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val der_bytes = hexStringToByteArray(der_cert)
    val decoded_cert = certificateFactory.generateCertificate(ByteArrayInputStream(der_bytes)) as X509Certificate

    //println(decoded_cert)


}