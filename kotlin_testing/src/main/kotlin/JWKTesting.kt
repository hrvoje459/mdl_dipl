package fer.dipl.mdl

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.keys.jwk.JWKKeyMetadata
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.StringWriter
import java.math.BigInteger
import java.security.*
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*


// REFERENCES:
// https://techxperiment.blogspot.com/2016/10/create-and-read-pkcs-8-format-private.html
// https://www.rfc-editor.org/rfc/rfc8037#section-2


class JWKTesting {
}

suspend fun main(){


    val secureRandom = SecureRandom()
    Security.addProvider(BouncyCastleProvider())
    val kpg = KeyPairGenerator.getInstance("Ed25519", "BC")
    kpg.initialize(256, secureRandom)

    val rootCaKeyPair = kpg.genKeyPair()

    println("Root CA format: " + rootCaKeyPair.private.format)
    println("Root CA private key: " + rootCaKeyPair.private.toString())
    println("Root CA private key: " + rootCaKeyPair.private.javaClass)
    println("Root CA private key: " + rootCaKeyPair.private.encoded.joinToString())
    println("Root CA private key encoded: " + rootCaKeyPair.private.encoded.joinToString(""){ String.format("%02X", it) })
    //println("Root CA public key encoded: " + rootCaKeyPair.public.encoded.joinToString(""){ String.format("%02X", it) })


    val sw1 = StringWriter()
    val gen1 = JcaPKCS8Generator(rootCaKeyPair.private, null)
    val obj1 = gen1.generate()
    JcaPEMWriter(sw1).use { pw ->
        pw.writeObject(obj1)
    }

    // HERE WE HAVE PEM STRING, NOW WE WANT TO GENERATE PRIVATE KEY CLASS FROM IT
    val rootCaPemKey = sw1.toString()
    var copiedRootCaPemKey = rootCaPemKey

    println("Root CA private key PEM:")
    println(copiedRootCaPemKey)

    copiedRootCaPemKey = copiedRootCaPemKey.replace("-----BEGIN PRIVATE KEY-----", "")
    copiedRootCaPemKey = copiedRootCaPemKey.replace("-----END PRIVATE KEY-----", "");
    copiedRootCaPemKey = copiedRootCaPemKey.replace("\n", "");


    println(Base64.getDecoder().decode(copiedRootCaPemKey).joinToString())
    println(Base64.getEncoder().encodeToString(Base64.getDecoder().decode(copiedRootCaPemKey)))


    // THIS IS BASE 64 ENCODED, JWK REPRESENTATION SHOULD HAVE BASE 64 URL ENCODING
    println("BASE 64 URL ENCODED ROOT PRIVATE: " + copiedRootCaPemKey)
    //val encoded: ByteArray = Base64.getUrlDecoder().decode(rootCaPemKey)
    val decoded: ByteArray = Base64.getDecoder().decode(copiedRootCaPemKey)
    println("BASE 64 URL DECODED ROOT PRIVATE: " + decoded.joinToString(""){ String.format("%02X", it) })



    val kspec = PKCS8EncodedKeySpec(decoded)
    val kf: KeyFactory = KeyFactory.getInstance("Ed25519", "BC")
    val rootCaPrivateKey: PrivateKey = kf.generatePrivate(kspec)

    println("RECONSTRUCTED CA private key: " + rootCaPrivateKey)
    println("RECONSTRUCTED ROOT CA private key: " + rootCaPrivateKey.encoded.joinToString())
    println("RECONSTRUCTED ROOT CA private key encoded: " + rootCaPrivateKey.encoded.joinToString(""){ String.format("%02X", it) })

    /*
    rootCaPrivateKey.encoded


    val privateKeyBytes = Base64.getUrlDecoder().decode("nWGxne_9WmC6hEr0kuwsxERJxWl7MmkZcDusAxyuf2A")
    val publicKeyBytes = Base64.getUrlDecoder().decode("11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo")

    val keyFactory = KeyFactory.getInstance("Ed25519")


    // Wrap public key in ASN.1 format so we can use X509EncodedKeySpec to read it
    val pubKeyInfo: SubjectPublicKeyInfo =
        SubjectPublicKeyInfo(AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), publicKeyBytes)
    val x509KeySpec = X509EncodedKeySpec(pubKeyInfo.encoded)

    val jcaPublicKey: PublicKey = keyFactory.generatePublic(x509KeySpec)


    // Wrap private key in ASN.1 format so we can use
    val privKeyInfo =
        PrivateKeyInfo(AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), DEROctetString(privateKeyBytes))
    val pkcs8KeySpec = PKCS8EncodedKeySpec(privKeyInfo.encoded)

    val jcaPrivateKey: PrivateKey = keyFactory.generatePrivate(pkcs8KeySpec)

    println(jcaPublicKey)
    println(jcaPrivateKey?.encoded?.joinToString  ())

    val testKP = KeyPair(jcaPublicKey, jcaPrivateKey)
    println("TEST KP PRIVATE: " + testKP.private)

    // create CA certificate
    val rootCaCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CSP"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(testKP.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(testKP.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }*/



    val sw2 = StringWriter()
    val gen2 = JcaPKCS8Generator(rootCaKeyPair.private, null)
    val obj2 = gen2.generate()
    JcaPEMWriter(sw2).use { pw ->
        pw.writeObject(obj2)
    }

    // HERE WE HAVE PEM STRING, NOW WE WANT TO GENERATE PRIVATE KEY CLASS FROM IT
    val rootCaPemKey2 = sw2.toString()
    var copiedRootCaPemKey2 = rootCaPemKey


    println(copiedRootCaPemKey2)



    val jwkKey = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
    println("JWK: " + jwkKey.exportJWK())




    // Generate EC key pair with P-256 curve
    val gen = KeyPairGenerator.getInstance("EC")
    gen.initialize(Curve.P_256.toECParameterSpec())
    val keyPair = gen.generateKeyPair()


    // Convert to JWK format
    /*val jwk: JWK = ECKey.Builder(Curve.P_256, keyPair.public as ECPublicKey)
        .privateKey(keyPair.private as ECPrivateKey)
        .build()*/

    val jwkString = jwkKey.exportJWK()
    println("NEW JWK: " + jwkString)

    val ecKeyFromJwk = ECKey.parse(jwkString)
    println("EC FROM JWK: " + ecKeyFromJwk)



    val rootCaCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CSP"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(ecKeyFromJwk.toECPublicKey().encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(ecKeyFromJwk.toECPrivateKey())).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }

    println(rootCaCertificate)

    /*val jsonJwk = Json.parseToJsonElement(jwkKey.exportJWK())

    println("JWK \"d\": " + jsonJwk.jsonObject.get("d").toString().replace("\"",""))
    println("JWK \"x\": " + jsonJwk.jsonObject.get("x").toString().replace("\"",""))


    var privateKeyBytes = Base64.getUrlDecoder().decode(jsonJwk.jsonObject.get("d").toString());
    var publicKeyBytes = Base64.getUrlDecoder().decode(jsonJwk.jsonObject.get("x").toString());



    val testing = Ed25519PrivateKeyParameters(privateKeyBytes,0)
    println(testing.encoded.joinToString(""){ String.format("%02X", it) })

    val pkcs8KeySpec = PKCS8EncodedKeySpec(testing.encoded)
    //val pkcs8KeySpec = PKCS8EncodedKeySpec(privateKeyBase64)

    val keyFactory = KeyFactory.getInstance("Ed25519", "BC")
    val privkey = keyFactory.generatePrivate(pkcs8KeySpec)

    println(privkey)*/


}