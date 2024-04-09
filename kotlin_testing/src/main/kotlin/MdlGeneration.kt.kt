package fer.dipl.mdl

import COSE.AlgorithmID
import COSE.OneKey
import cbor.Cbor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider

import id.walt.crypto.keys.jwk.JWKKeyMetadata
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.mdoc.dataelement.*
import id.walt.mdoc.doc.MDocBuilder
import id.walt.mdoc.mso.DeviceKeyInfo
import id.walt.mdoc.mso.ValidityInfo
import java.security.KeyPairGenerator
import java.security.Security
import kotlinx.datetime.LocalDate
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.json.Json
import java.math.BigInteger

import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.crypto.prng.FixedSecureRandom
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.security.SecureRandom
import java.util.*


class `MdlGeneration` {
}

suspend fun main() {

    val key = JWKKey.generate(KeyType.Ed25519, JWKKeyMetadata())

    Security.addProvider(BouncyCastleProvider())
    val kpg = KeyPairGenerator.getInstance("EC")
    kpg.initialize(256)
    // create key pair for test CA
    val rootCaKeyPair = kpg.genKeyPair()
    //intermCaKeyPair = kpg.genKeyPair()
    // create key pair for test signer/issuer
    val issuerKeyPair = kpg.genKeyPair()
    //intermIssuerKeyPair = kpg.genKeyPair()
    // create key pair for mdoc auth (device/holder key)
    val deviceKeyPair = kpg.genKeyPair()
    val readerKeyPair = kpg.genKeyPair()
    val ISSUER_KEY_ID = "ISSUER_KEY"
    val DEVICE_KEY_ID = "DEVICE_KEY"

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

    val issuerCertificate = X509v3CertificateBuilder(X500Name("CN=MDOC ROOT CA"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC Test Issuer"),
        SubjectPublicKeyInfo.getInstance(issuerKeyPair.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(false))
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.digitalSignature))
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootCaKeyPair.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }



    // instantiate simple cose crypto provider for issuer keys and certificates
    val cryptoProvider = SimpleCOSECryptoProvider(
        listOf(
            COSECryptoProviderKeyInfo(ISSUER_KEY_ID, AlgorithmID.ECDSA_256, issuerKeyPair.public, issuerKeyPair.private, listOf(issuerCertificate), listOf(rootCaCertificate)),
            COSECryptoProviderKeyInfo(DEVICE_KEY_ID, AlgorithmID.ECDSA_256, deviceKeyPair.public, deviceKeyPair.private)
        )
    )
    // create device key info structure of device public key, for holder binding
    val deviceKeyInfo = DeviceKeyInfo(DataElement.fromCBOR(OneKey(deviceKeyPair.public, null).AsCBOR().EncodeToBytes()))

    // build mdoc and sign using issuer key with holder binding to device key
    val mdoc = MDocBuilder("org.iso.18013.5.1.mDL")
        .addItemToSign("org.iso.18013.5.1", "family_name", "Doe".toDE())
        .addItemToSign("org.iso.18013.5.1", "given_name", "John".toDE())
        .addItemToSign("org.iso.18013.5.1", "birth_date", FullDateElement(LocalDate(1990, 1, 15)))
        .addItemToSign("org.iso.18013.5.1", "gender", "Male".toDE())
        .sign(
            ValidityInfo(Clock.System.now(), Clock.System.now(), Clock.System.now().plus(365*24, DateTimeUnit.HOUR)),
            deviceKeyInfo, cryptoProvider, ISSUER_KEY_ID
        )
    mdoc.MSO?.getValueDigestsFor("org.iso.18013.5.1")?.values?.forEach {
        val temp = it
        val cbor_temp = Cbor.encodeToHexString(temp)

        println( cbor_temp )
    }


    println("SIGNED MDOC:")
    println(Cbor.encodeToHexString(mdoc))
    println("ISSUER SIGNED")
    println(mdoc.issuerSigned)
    println("NAMESPACES")
    println(mdoc.issuerSigned.nameSpaces)
    println("VALUES")
    println(mdoc.issuerSigned?.nameSpaces?.get("org.iso.18013.5.1"))

    mdoc.issuerSigned?.nameSpaces?.get("org.iso.18013.5.1")?.forEachIndexed { index, element ->
        println("VALUE " + index + ":")
        println(element.decode())
        println(element.decode().value)
        println(element.decode().value)
        println("MAP")
        val map = element.toCBORHex()
        println(map)
        println(element.toCBORHex())
        println(element.toCBOR().joinToString())


        val inputStream = "A4686469676573744944036672616E646F6D50F479E4789A14BFD9A3F3008656BDA8B271656C656D656E744964656E7469666965726667656E6465726C656C656D656E7456616C7565644D616C65".byteInputStream()
        val mapper = ObjectMapper(CBORFactory())
        val text =  mapper.readValue(inputStream, String::class.java)

        println(text)


    }




}