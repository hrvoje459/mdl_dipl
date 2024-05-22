import COSE.AlgorithmID
import COSE.OneKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.X509CertUtils
import com.upokecenter.cbor.CBORObject
import fer.dipl.mdl.IssuerSigned
import fer.dipl.mdl.MyObject
import fer.dipl.mdl.issuer_app.secrets_folder
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.dataretrieval.DeviceRequest
import id.walt.mdoc.dataretrieval.DeviceResponse
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.doc.MDocVerificationParams
import id.walt.mdoc.doc.VerificationType
import id.walt.mdoc.doc.and
import id.walt.mdoc.docrequest.MDocRequestBuilder
import id.walt.mdoc.docrequest.MDocRequestVerificationParams
import id.walt.mdoc.mdocauth.DeviceAuthentication
import id.walt.mdoc.mso.DeviceKeyInfo
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
import java.io.ByteArrayInputStream
import java.io.File
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

class MDocRequest {
}

suspend fun main(args: Array<String>) {


    val READER_KEY_ID = "READER_KEY"

    val rootJWK = "./$secrets_folder/root_ca_jwk.json"
    val rootCert = "./$secrets_folder/root_ca_cert.json"

    val intermediateJWK = "./$secrets_folder/intermediate_ca_jwk.json"
    val intermediateCert = "./$secrets_folder/intermediate_ca_cert.json"

    val readerJWK = "./$secrets_folder/reader_jwk.json"
    val readerCert = "./$secrets_folder/reader_cert.json"


    val rootJWKFile = File(rootJWK)
    val rootCertFile = File(rootCert)

    val intermediateJWKFile = File(intermediateJWK)
    val intermediateCertFile = File(intermediateCert)

    val readerJWKFile = File(readerJWK)
    val readerCertFile = File(readerCert)



    var rootECKey : ECKey? = null
    var intermediateECKey : ECKey? = null
    var readerECKey: ECKey? = null

    var rootCaCertificate: X509Certificate? = null;
    var intermediateCaCertificate: X509Certificate? = null;
    var readerCertificate: X509Certificate? = null


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
    // check for READER key and cert
    if (readerJWKFile.exists() && readerCertFile.exists()) {
        if (readerJWKFile.length() == 0L || intermediateCertFile.length() == 0L) {
            println("The file '$readerJWKFile' or '$readerCertFile' is empty.")
        } else {
            println("The files '$readerJWKFile' or '$readerCertFile' are not empty.")
            readerECKey = ECKey.parse(readerJWKFile.readText())
            //println(intermediateJWKFile.readText())
            readerCertificate = X509CertUtils.parse(readerCertFile.readText())
            //println(intermediateCertFile.readText())

        }
    }



    println("ROOT:" + rootCaCertificate)
    println("INTER:" + intermediateCaCertificate)
    println("READER:" + readerCertificate)


    val certs = listOf(readerCertificate!!, intermediateCaCertificate!!, rootCaCertificate!!)


    val cryptoProvider = SimpleCOSECryptoProvider(
        listOf(
            COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerECKey!!.toKeyPair().public, readerECKey!!.toKeyPair().private, certs, listOf(rootCaCertificate)),
        )
    )

    val deviceEngagementBytes = "engagement_bytes".toByteArray()
    val EReaderKeyBytes = readerECKey.toKeyPair().public.encoded

    val sessionTranscript = ListElement(listOf(deviceEngagementBytes.toDE(), EReaderKeyBytes.toDE()))

    val mdocRequest = MDocRequestBuilder("org.iso.18013.5.1.mDL")
        .addDataElementRequest("org.iso.18013.5.1", "family_name", true)
        //.addDataElementRequest("org.iso.18013.5.1", "birth_date", false)
        .addDataElementRequest("org.iso.18013.5.1", "given_name", true)
        .addDataElementRequest("org.iso.18013.5.1", "issuing_authority", true)
        .sign(sessionTranscript, cryptoProvider, READER_KEY_ID)


    val deviceRequest = DeviceRequest(listOf(mdocRequest))
    var devReqCbor = deviceRequest.toCBORHex()


    println(devReqCbor)




    val verifier_certs = CertificateFactory.getInstance("X509").generateCertificates(ByteArrayInputStream(deviceRequest.docRequests.first().readerAuth!!.x5Chain)).map { it as X509Certificate }


    //println("CERT CHAIN: " + verifier_certs)
    println()

    val cryptoProvider_verifier = SimpleCOSECryptoProvider(
        listOf(
            COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, verifier_certs.first().publicKey,  x5Chain = verifier_certs, trustedRootCAs =  listOf(rootCaCertificate)),
        )
    )

    val reqVerified = deviceRequest.docRequests.first().verify(
        MDocRequestVerificationParams(
            requiresReaderAuth = true,
            READER_KEY_ID,
            allowedToRetain = mapOf("org.iso.18013.5.1" to setOf("family_name")),
            ReaderAuthentication(sessionTranscript, deviceRequest.docRequests.first().itemsRequest)
        ), cryptoProvider_verifier
    )

    println(reqVerified)
    println("VERFIY CHAIN: " + cryptoProvider_verifier.verifyX5Chain(deviceRequest.docRequests.first().readerAuth!!, READER_KEY_ID))



    Security.addProvider(BouncyCastleProvider())
    val kpg = KeyPairGenerator.getInstance("EC")
    val rootCaKeyPair = kpg.generateKeyPair()
    val false_root = X509v3CertificateBuilder(
        X500Name("CN=MDOC ROOT CA"), BigInteger.valueOf(SecureRandom().nextLong()),
        Date(), Date(System.currentTimeMillis() + 24L * 3600 * 1000), X500Name("CN=MDOC ROOT CA"),
        SubjectPublicKeyInfo.getInstance(rootCaKeyPair.public.encoded)
    ) .addExtension(Extension.basicConstraints, true, BasicConstraints(true)) // TODO: Should be CA! Should not pass validation when false!
        .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
        .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(rootCaKeyPair.private)).let {
            JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
        }

    val cryptoProvider_false = SimpleCOSECryptoProvider(
        listOf(
            COSECryptoProviderKeyInfo(READER_KEY_ID, AlgorithmID.ECDSA_256, readerECKey!!.toKeyPair().public,  x5Chain = verifier_certs, trustedRootCAs =  listOf(false_root)),
        )
    )

    val reqVerified_false = deviceRequest.docRequests.first().verify(
        MDocRequestVerificationParams(
            requiresReaderAuth = true,
            READER_KEY_ID,
            allowedToRetain = mapOf("org.iso.18013.5.1" to setOf("family_name")),
            ReaderAuthentication(sessionTranscript, deviceRequest.docRequests.first().itemsRequest)
        ), cryptoProvider_false
    )

    println(reqVerified_false)
    println("VERFIY CHAIN: " + cryptoProvider_false.verifyX5Chain(deviceRequest.docRequests.first().readerAuth!!, READER_KEY_ID))





    lateinit var deviceKeyPair: KeyPair
    Security.addProvider(BouncyCastleProvider())
    deviceKeyPair = kpg.genKeyPair()

    println("DEVICE_PUBLIC: " + deviceKeyPair.public)

    val cryptoProvider_device = SimpleCOSECryptoProvider(
        listOf(
            COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256, deviceKeyPair.public, deviceKeyPair.private,  x5Chain = verifier_certs, trustedRootCAs =  listOf(false_root)),
        )
    )

    val mdoc_string = "a267646f6354797065756f72672e69736f2e31383031332e352e312e6d444c6c6973737565725369676e6564a26a6e616d65537061636573a1716f72672e69736f2e31383031332e352e318dd8185852a4686469676573744944006672616e646f6d501425bbe319e94a6742370903911d1fc071656c656d656e744964656e7469666965726b66616d696c795f6e616d656c656c656d656e7456616c756563526f6dd8185854a4686469676573744944016672616e646f6d50c3d280f17a887ecd3d0b6d3dcb2fa04971656c656d656e744964656e7469666965726a676976656e5f6e616d656c656c656d656e7456616c7565664872766f6a65d818590a1ea4686469676573744944026672616e646f6d5025fbd6ea4efcdca4299ea12f4a358b7b71656c656d656e744964656e74696669657268706f7274726169746c656c656d656e7456616c75657909d05f396a5f34414151536b5a4a5267414241674141415141424141445f327742444141674742676347425167484277634a4351674b4442514e4441734c44426b534577385548526f6648683061484277674a43346e49434973497877634b4463704c4441784e44513048796335505467795043347a4e444c5f3277424441516b4a4351774c4442674e44526779495277684d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a49794d6a4c5f7741415243414253414649444153494141684542417845425f38514148774141415155424151454241514541414141414141414141414543417751464267634943516f4c5f3851417452414141674544417749454177554642415141414146394151494441415152425249684d5545474531466842794a7846444b426b61454949304b78775256533066416b4d324a7967676b4b4668635947526f6c4a69636f4b536f304e5459334f446b3651305246526b644953557054564656575631685a576d4e6b5a575a6e61476c7163335231646e643465587144684957476834694a69704b546c4a57576c35695a6d714b6a704b576d7036697071724b7a744c57327437693575734c44784d584778386a4a79744c54314e585731396a5a32754869342d546c3575666f3665727838765030396662332d506e365f38514148774541417745424151454241514542415141414141414141414543417751464267634943516f4c5f385141745245414167454342415144424163464241514141514a3341414543417845454253457842684a425551646863524d694d6f454946454b526f62484243534d7a55764156596e4c524368596b4e4f456c3852635947526f6d4a7967704b6a55324e7a67354f6b4e4552555a4853456c4b55315256566c64595756706a5a47566d5a326870616e4e3064585a3365486c36676f4f456859614869496d4b6b704f556c5a61586d4a6d616f714f6b7061616e714b6d7173724f3074626133754c6d367773504578636248794d6e4b3074505531646258324e6e613475506b3565626e364f6e7138765030396662332d506e365f396f4144414d4241414952417845415077424430714e6a7855705565744a47712d634e33516331794a58646a51424447674a7548387635647935372d3374564a4e58676a6c634a355a597274446f4d7370397a2d6451363278764a49374f304c4e504b337a6e64785f6b65324b6d48676d58374f73596c78786e4a35796665746e4b4d4e427870796e716a4c6e385333595978786233584f50384161423961684f76584a51534c4c734b6b6e596563673572536a384e583970634b38694b35586a4f4f7453487761307a76495732377363564c724a476977386d5a74747136545478416772676774783047443072656a64574a5545626831476179377677644a4568654631456f47666d3647736e5339516b69316351584373732d37613462706a464b385a37455370796875645a7470757970514161436f7855456b4778665155564c745769693444367332734376424b376a4f634b42365f353471734f525768703759683274303335483556704434685059712d48744c5343396d757042756373514366317858526d5835716f5745676b6a7941514d384539366e646b516b4f79672d6d617771627337714b744644704a526e4f6168383952314e4d6c6b6a5041595641324f647a4469736a70516c7863426a6756794f7432364a4f4c78554446514e34365a776544585358456b4a2d56584763317a32714f466c45557552472d554a39446a69717058556a47765a775a707835654e584a4b3767446a3070345431596d6d776f45685256484371414b667a6a7057375050457839614b546e306f7058416344674770344761544b7870746546504d33673533453534782d4655664d487655386362797741713749676b486d6c54673766385f7a6f7530376d744b4b6b326e324e6d54646236544730492d6679384c39636461387a3166796c61535a72755f6c6d4c345a6f68387537366d765555327a5761524f6f2d5559724a764e4e616362464368656e536c7a63724f6851356b635a6f4d3938626b5772584e773257413279344f507872563854585633596f596758444f50764c79514b3644544e4674375358657a6235427a6a3071505872614f65564862487938486e394b7a62317561714455576a795f424d7269346e763841656e336d344957756c696a6e754e47557a50356a4a497056325033686e69747950527968334c735a505675314c4e424773506b494279527776317133557531597a396c61397953467730534d4d6a4b67344e535a4e5149437168636442696e354f4f394d346e612d672d696f364b4243676a46574c4f3757314d766d42696a4c30583171746e696d4d636a69727463714d75563352306b4c6859515433357857667157712d546c59314263384b50656f62532d533430394a496e3344473350726a6a2d6c5a4f6f797969645868514f37634145394b7a6b7462485a43576c79394671636d6d523733684d37794e756551483776746973323838544335755246486265596a4e383348414834306b375854516c5a4c4f527339634f7636566c737a526a4b57635f737a4d4d6e384b4f56574c35706d5f5936684a61784746323371423870507054466e335859644277446d7357306b6e6c6d506e782d574144337a6d744b794f357965796a3961584c71524f7037747a51427a7a526b2d74494478526e69744c6148454c525362714b6b51355932633756556b2d315674567a626150646b4547526f6d5559505467303234314b56687352634c302d586a465670356a63524f6a63676a42412d6c646b616474544e7a75542d484c6157507776616d516a63323568673969656c576f466a61344f572d6273445439446b55364848486e356f6955497176637269517376427a3172686437753536555575564f4a4e6432307a6f5553627938394436316d47786e5137705a4d6a324e4e766236366a4735675739314e5a452d7233626a61694563395454533741354c7157377955524d7359624c6e734b7762545672714458726e4a4a525732374f785556713230446c6a4e4d637974363971786269494a71736a44726a4a5f57744b5675617868582d4735313057727157514d67327430494e583472694f594859656e55455678775176416735365661746232574e73466963644748577568306b7a6b556d6a7139776f72495f744a5f5666306f725032506d567a50734f62714b502d57706f6f72714d69356f7633376e385036314e50315f4769697650715f477a314b4877497937766f395a615f384148324b4b4b6c6241397a516a2d38316335503841386843365f7742366969744b48786d57492d4645682d36507054455f316a305556327334796569696973537a5f396b3dd818585ba4686469676573744944036672616e646f6d508257c34beebb253ba60195cb51f7d25471656c656d656e744964656e7469666965726a62697274685f646174656c656c656d656e7456616c7565d903ec6a313937322d31322d3233d818585ba4686469676573744944046672616e646f6d500625370f091753ef5b76bd1db845762c71656c656d656e744964656e7469666965726a69737375655f646174656c656c656d656e7456616c7565d903ec6a323032342d31322d3233d818585ca4686469676573744944056672616e646f6d50f5b11052fd222948f5e7c1ba62fbef1a71656c656d656e744964656e7469666965726b6578706972795f646174656c656c656d656e7456616c7565d903ec6a323032372d31322d3233d8185855a4686469676573744944066672616e646f6d50b1c4a1bfec40087f5de2598d3f18f3c671656c656d656e744964656e7469666965726f69737375696e675f636f756e7472796c656c656d656e7456616c7565624852d8185858a4686469676573744944076672616e646f6d50d14869f06f9e531efaac98c87518717771656c656d656e744964656e7469666965727169737375696e675f617574686f726974796c656c656d656e7456616c7565634d5550d818584fa4686469676573744944086672616e646f6d50985173943e0f4fa5712f25a9c3dff5a771656c656d656e744964656e7469666965726b6167655f6f7665725f31386c656c656d656e7456616c7565f5d818584fa4686469676573744944096672616e646f6d508b5ac1a84cd66b456a59702ba5088a0071656c656d656e744964656e7469666965726b6167655f6f7665725f32316c656c656d656e7456616c7565f5d818584fa46864696765737449440a6672616e646f6d5083ce73ce16cdded63ed5df82bc65f58371656c656d656e744964656e7469666965726b6167655f6f7665725f32346c656c656d656e7456616c7565f5d818584fa46864696765737449440b6672616e646f6d502e44f2c80fb250b6b13ac4214c56cabe71656c656d656e744964656e7469666965726b6167655f6f7665725f36356c656c656d656e7456616c7565f4d81858d4a46864696765737449440c6672616e646f6d50ab714f5d0a44f4d9ee0065567316ffd071656c656d656e744964656e7469666965727264726976696e675f70726976696c656765736c656c656d656e7456616c756582a27576656869636c655f63617465676f72795f636f646561446a69737375655f64617465d903ec6a323031392d30312d3031a37576656869636c655f63617465676f72795f636f646561436a69737375655f64617465d903ec6a323031392d30312d30316b6578706972795f64617465d903ec6a323031372d30312d30316a697373756572417574688443a10126a118218359014d308201493081f0a0030201020208779861f163d32f56300a06082a8648ce3d040302301b3119301706035504030c104d444f4320497465726d204341204852301e170d3234303531383136343532315a170d3235303531383136313835315a30193117301506035504030c0e4d444f43204973737565722048523059301306072a8648ce3d020106082a8648ce3d03010703420004e1ea67b57f8f4ac522517305217107b40cde56b999d5dbce4e3910632c0ede2d1ab15b34f584dc761593fa89c4c4ac5eb4cc83d548845c88764e5435b96b6d96a320301e300c0603551d130101ff04023000300e0603551d0f0101ff040403020106300a06082a8648ce3d0403020348003045022100fa956904eb14a9ff98b3d8f901b880d877b781f510e36f37b68190dd4fd37fdb02203085b5da97e28369d60380d8de7891542d2c93470a9dd21db267b4f0f8b76da95901513082014d3081f4a003020102020854b730d7a567811b300a06082a8648ce3d040302301a3118301606035504030c0f4d444f4320524f4f54204341204852301e170d3234303531383136343532315a170d3235303531383136313835315a301b3119301706035504030c104d444f4320497465726d2043412048523059301306072a8648ce3d020106082a8648ce3d03010703420004a9f1685da3fdcc778f8bb7f29008ff317fc319f0196b5b3d1877017294e5812a1512f0787aef431835b7357333343eb478482eedcc314e9c885cac8495454762a3233021300f0603551d130101ff040530030101ff300e0603551d0f0101ff040403020106300a06082a8648ce3d04030203480030450220603c62bfd3dbfa1a24a0602d2d5f9f6d7d0fae1ae5f4dc421818ad96acc0e77a022100e54e7d48e8884661ed92f82bbbefb91d91580b8f2cbcb2b1e1c263de08d17cbe5901513082014d3081f3a003020102020850f94f4c1afe01d6300a06082a8648ce3d040302301a3118301606035504030c0f4d444f4320524f4f54204341204852301e170d3234303531383136343532315a170d3235303531383136313835315a301a3118301606035504030c0f4d444f4320524f4f542043412048523059301306072a8648ce3d020106082a8648ce3d030107034200049ff4ad7f2295a7650bcd86de69c8c23613b2d64b1a092cc38fbf27b6110e354b9867d5c3505cfa49176dd05f0859a97bb0e787429d43209b58292543309252bfa3233021300f0603551d130101ff040530030101ff300e0603551d0f0101ff040403020106300a06082a8648ce3d0403020349003046022100ffde5916571991c86e6397e8b0f043c83903f5655b37266d850ae6558145c4cb022100d0e283a1e9dec06bb15669c1a3ae17a151162685481d9927e796c5ff08706621590321d81859031ca66776657273696f6e63312e306f646967657374416c676f726974686d675348412d3235366c76616c756544696765737473a1716f72672e69736f2e31383031332e352e31ad0058203ea973b40a028277f8edb6402761b3f498fbab7c3741d961b515a9b369777074015820ebcf273784af127f14f432a9d5281c856fe1f9b2153aaf5651a69dbe056581f5025820fe264cfeda3c5d6d78c990686ded4fc9506fed6c816ff404aa47570fbfa6af40035820ec1ef533ce80631dc234d0bbf54f5cde5aa09684d1ef08d8e01b8d324ebe04f4045820032e80af2a90c789d4d1e17f52c88d278b5c9c57cb56f12cbd97e9b2a8a14fdd055820e0b0c39c97216bcd324e2f8a8f152039836db3a3bf24e1d1609af7e68ed324cd065820d271be70e1288ab3633b21f779449b6f8d92e48e7b7e96277a092ba92aed9c580758204bcf42491b9886da985a83aa6d6e2f84760cf351e4df478f49071111e09b354b085820aae64284701228a17f081a6dd19613b31437a471f40471696e6aa86282dd9b61095820da8094b1d9e78b5a8a4f4773648302d57fe1a2e8c7c0f021154ceeb14e8690d70a58203370745ab411df2f01e18b45dc3c6353673f33af650223f1c5445997f66fc0600b58206f7a00e032c09298a841bc0ed8758f62d40e9133798b5d895e538ed303284ee50c582051662a362569b0864239f91692a4fe38f11a6a88b0224ffbb14873d6d2cd414a6d6465766963654b6579496e666fa1696465766963654b6579a4010220012158204775e036d20f03657d43a19d89448c6aba01bdcf5d8562edc9f3fbe398cf2bd12258204f2f025a32abaca9df4c53a282bb465f055845c2ae27f46336ffd65478fe0e4f67646f6354797065756f72672e69736f2e31383031332e352e312e6d444c6c76616c6964697479496e666fa3667369676e6564c0781e323032342d30352d31385432303a31383a34302e3433393733383531335a6976616c696446726f6dc0781e323032342d30352d31385432303a31383a34302e3433393734303432355a6a76616c6964556e74696cc0781e323032352d30352d31385432303a31383a34302e3433393734303635375a5840c7eb6e8cff804f741a118ac73f430cfd71539f0c3ea63d7efc8d643412ea20e478bd5ef14f212af208cc38ba93e57b7096c064a288d2c1ae513aaa09eb93405c"
    //val mdocRespParsed = DeviceResponse.fromCBORHex(mdoc_string.replace("\"",""))
    //val mdoc = mdocRespParsed.documents[0]
    val mdoc = MDoc.fromCBORHex(mdoc_string)

    println("SESSION TRANS: " + sessionTranscript)

    val device_auth = DeviceAuthentication(sessionTranscript, "org.iso.18013.5.1.mDL", deviceRequest.docRequests.first().decodedItemsRequest.nameSpaces.toEncodedCBORElement())

    println(deviceRequest.docRequests.first().decodedItemsRequest.nameSpaces.toCBORHex())
    println("device_auth: " + device_auth.toDE().toCBORHex())

    println("REQUESTED ITEAMS:" + deviceRequest.docRequests.first().decodedItemsRequest.nameSpaces.value.values)
    deviceRequest.docRequests.first().decodedItemsRequest.nameSpaces.value.values.forEach {
        println("ITEAM:" + it.toCBORHex())
        println(decodeCborMap(it.toCBOR()))
    }



    val presentation = mdoc.presentWithDeviceSignature(deviceRequest.docRequests.first(), device_auth, cryptoProvider_device, "DEVICE_KEY_ID")

    println(presentation)
    println(presentation.toCBORHex())


    val presentation_chain = presentation.issuerSigned.issuerAuth!!.x5Chain!!
    val pres_chain = CertificateFactory.getInstance("X509").generateCertificates(ByteArrayInputStream(presentation_chain)).map { it as X509Certificate }
    println("PRESENTATION CERT CHAIN: " + pres_chain)



    val recreated_device_key = OneKey(CBORObject.DecodeFromBytes(presentation.MSO!!.deviceKeyInfo.deviceKey.toCBOR()))
    println(recreated_device_key)
    println("DEVICE_PUBLIC: " + recreated_device_key.AsPublicKey())


    val cryptoProvider_reader_side = SimpleCOSECryptoProvider(listOf(
        //COSECryptoProviderKeyInfo("ISSUER_KEY_ID", AlgorithmID.ECDSA_256, pres_chain.first().publicKey, x5Chain =  pres_chain, trustedRootCAs =  listOf(pres_chain.last())),
        COSECryptoProviderKeyInfo("ISSUER_KEY_ID", AlgorithmID.ECDSA_256, pres_chain.first().publicKey, x5Chain =  pres_chain, trustedRootCAs =  listOf(rootCaCertificate)),
        COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256, recreated_device_key.AsPublicKey(), x5Chain =  pres_chain, trustedRootCAs =  listOf(pres_chain.last()))

    ))


    println("presentation verified: " + presentation.verifySignature(cryptoProvider_reader_side, "ISSUER_KEY_ID"))
    println("presentation verified: " +  presentation.verifyDeviceSignature(device_auth, cryptoProvider_reader_side, "DEVICE_KEY_ID"))
    println("presentation verified:" + presentation.verifyCertificate(cryptoProvider_reader_side, "ISSUER_KEY_ID"))



    val mdocVerified = presentation.verify(
        MDocVerificationParams(
        VerificationType.DOC_TYPE and VerificationType.DEVICE_SIGNATURE and VerificationType.ISSUER_SIGNATURE and VerificationType.ITEMS_TAMPER_CHECK,
        issuerKeyID = "ISSUER_KEY_ID",
        deviceKeyID = "DEVICE_KEY_ID",
        deviceAuthentication = device_auth,
        mDocRequest = mdocRequest
    ), cryptoProvider_reader_side)

    println("presentation verified: " + mdocVerified)

}

fun decodeCborMap(data: ByteArray): MyMap {
    val mapper = ObjectMapper(CBORFactory())
    return mapper.readValue(data, MyMap::class.java)
}

data class MyMap(
    //val iteams_map: Map<String, Boolean>,
    val family_name: Boolean,
    val given_name: Boolean,
    val issuing_authority: Boolean,
    val issuing_country: Boolean,
){
    // Ensure a primary constructor is available
    constructor() : this(false, false, false, false)
}
