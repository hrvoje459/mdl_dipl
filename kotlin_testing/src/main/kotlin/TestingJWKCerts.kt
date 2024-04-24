package fer.dipl.mdl

import COSE.AlgorithmID
import COSE.OneKey
import cbor.Cbor
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.util.X509CertChainUtils
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.keys.jwk.JWKKeyMetadata
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.dataelement.DataElement
import id.walt.mdoc.dataelement.FullDateElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.doc.MDocBuilder
import id.walt.mdoc.mso.DeviceKeyInfo
import id.walt.mdoc.mso.ValidityInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.encodeToHexString
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

class TestingJWKCerts {
}

suspend fun main(){
    val issuer_key = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
    println("Issuer KEY: " + issuer_key.exportJWK())


    val ecKeyFromJwk = ECKey.parse(issuer_key.exportJWK())
    println("EC FROM JWK: " + ecKeyFromJwk)


    // create CA certificate
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

    val x509b64 = Base64.getEncoder().encodeToString(rootCaCertificate.encoded)




    val newKey = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
    println("New KEY: " + issuer_key.exportJWK())

    val newEC = ECKey.parse(newKey.exportJWK())
    println("EC FROM JWK: " + ecKeyFromJwk)

    // create CA certificate
    val issuerCertificate = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CSP"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(newEC.toECPublicKey().encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(newEC.toECPrivateKey())).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }

    val issuerx509b64 = Base64.getEncoder().encodeToString(issuerCertificate.encoded)

    val x509list = X509CertChainUtils.toBase64List(Arrays.asList(issuerx509b64, x509b64) as List<Any>?)
    println(x509list)


    val ecKeyBuilder = ECKey.Builder(newEC).x509CertChain(x509list)
    val ecKeyFromBuilder = ecKeyBuilder.build()

    println(ecKeyFromBuilder)

    val chain = X509CertChainUtils.parse(ecKeyFromBuilder.x509CertChain)
    println(chain)

    val deviceKey = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
    println("New KEY: " + deviceKey.exportJWK())

    val deviceKeyEC = ECKey.parse(newKey.exportJWK())
    println("EC FROM JWK: " + deviceKeyEC)

    // instantiate simple cose crypto provider for issuer keys and certificates
    val cryptoProvider = SimpleCOSECryptoProvider(
        listOf(
            COSECryptoProviderKeyInfo("ISSUER_KEY_ID", AlgorithmID.ECDSA_256, ecKeyFromBuilder.toECPublicKey(), ecKeyFromBuilder.toECPrivateKey(), chain, listOf(chain[1])),
            COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256, deviceKeyEC.toECPublicKey()/*, deviceKeyPair.private*/)
        )
    )
    // create device key info structure of device public key, for holder binding
    val deviceKeyInfo = DeviceKeyInfo(DataElement.fromCBOR(OneKey(deviceKeyEC.toECPublicKey(), null).AsCBOR().EncodeToBytes()))

    // build mdoc and sign using issuer key with holder binding to device key
    val mdoc = MDocBuilder("org.iso.18013.5.1.mDL")
        .addItemToSign("org.iso.18013.5.1", "family_name", "Doe".toDE())
        .addItemToSign("org.iso.18013.5.1", "given_name", "John".toDE())
        .addItemToSign("org.iso.18013.5.1", "birth_date", FullDateElement(LocalDate(1990, 1, 15)))
        .addItemToSign("org.iso.18013.5.1", "gender", "Male".toDE())
        .sign(
            ValidityInfo(Clock.System.now(), Clock.System.now(), Clock.System.now().plus(365*24, DateTimeUnit.HOUR)),
            deviceKeyInfo, cryptoProvider, "ISSUER_KEY_ID"
        )


    println("SIGNED MDOC:")
    println(Cbor.encodeToHexString(mdoc))

}