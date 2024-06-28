package fer.dipl.mdl.mdl_invalid_app.helpers

import COSE.AlgorithmID
import COSE.OneKey
import android.content.Context
import android.os.StrictMode
import com.android.identity.util.Logger
import com.nimbusds.jose.jwk.ECKey
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.keys.jwk.JWKKeyMetadata
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.dataelement.BooleanElement
import id.walt.mdoc.dataelement.DataElement
import id.walt.mdoc.dataelement.FullDateElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.MapKey
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.dataelement.toDE
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.doc.MDocBuilder
import id.walt.mdoc.mso.DeviceKeyInfo
import id.walt.mdoc.mso.ValidityInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.InputStreamReader
import java.math.BigInteger
import java.security.SecureRandom
import java.security.Security
import java.util.Date


@Serializable
data class JwtPayload(
    val aud: String,
    val iat: Long,
    val nonce: String
)
@Serializable
data class JWTProofPayload(
    val proof_type: String,
    val jwt: String
)
@Serializable
data class CredentialRequestPayload(
    val credentialIdentifier: String,
    val proof: JWTProofPayload,
    val format: String,
    val types: Array<String>
)

class DrivingCredentialRequest(context: Context){
    lateinit var driving_credential: MDoc
    lateinit var user_key: JWKKey

    var policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()


    fun getCredential(context: Context): MDoc?{

        var bufferString: String = ""
        try {
            val readFile = File(context.filesDir, "mdoc_dir/mdoc.txt")
            val fIn: FileInputStream = FileInputStream(readFile)
            val myReader = BufferedReader(InputStreamReader(fIn))

            bufferString = myReader.readText()

            myReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Logger.d("Credential", bufferString)

        if (bufferString != ""){
            return MDoc.fromCBORHex(bufferString)
        }else{
            return null
        }

    }
    fun deleteCredential(context: Context): Boolean{
        try {
            val mdocFile = File(context.filesDir, "mdoc_dir/mdoc.txt")
            //mdocFile.delete()
            Logger.d("CREDENTIAL DELETED:", mdocFile.delete().toString())

            val userKeyFile = File(context.filesDir, "mdoc_dir/user_key.txt")
            //userKeyFile.delete()
            Logger.d("USER KEY DELETED:", userKeyFile.delete().toString())


            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun requestCredential(username: String, password:String, country:String, context: Context):MDoc?{
        StrictMode.setThreadPolicy(policy)
        when(country){
            "Croatia" -> {
                val client_id = "issuer_application"
                val client_secret = "LOLYAla7PWDEQsrCehkMbo6YLZzswoJ8"
                val issuer_backend_url = "https://issuer-backend-hr.mdlissuer.xyz"
                val issuer_api_url = "https://issuer-api-hr.mdlissuer.xyz"
                val idp_url = "https://hrv-idp.mdlissuer.xyz/auth/realms/MDL"

                init_user_key(context)

                val received_mdoc = request_flow(
                    username,
                    password,
                    client_id,
                    client_secret,
                    issuer_backend_url,
                    issuer_api_url,
                    idp_url,
                    user_key,
                    context
                )

                Logger.d("MDOC RECEIVED", received_mdoc!!.toCBORHex())

                return received_mdoc

            }
            "Slovenia" -> {
                val client_id = "issuer_application"
                val client_secret = "4icLik5qsQPNih5Vf2fB8hXKOXzlWoi2"
                val issuer_backend_url = "https://issuer-backend-slo.mdlissuer.xyz"
                val issuer_api_url = "https://issuer-api-slo.mdlissuer.xyz"
                val idp_url = "https://slo-idp.mdlissuer.xyz/auth/realms/MDL"

                init_user_key(context)

                val received_mdoc = request_flow(
                    username,
                    password,
                    client_id,
                    client_secret,
                    issuer_backend_url,
                    issuer_api_url,
                    idp_url,
                    user_key,
                    context
                )

                return received_mdoc

                Logger.d("MDOC RECEIVED", received_mdoc!!.toCBORHex())
            }
            else -> {
                Logger.d("UNRECOGNIZED COUNTRY", "")
                return null
            }
        }
    }

     suspend fun init_user_key(context: Context){

        var bufferString: String = ""

        try {
            val readFile = File(context.filesDir, "mdoc_dir/user_key.txt")
            val fIn: FileInputStream = FileInputStream(readFile)
            val myReader = BufferedReader(InputStreamReader(fIn))

            bufferString = myReader.readText()

            myReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (bufferString != ""){
            this.user_key = JWKKey.importJWK(bufferString).getOrNull()!!
        }else{
            // Create end user key (wallet)
            val user_key = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())

            this.user_key = user_key


            val mdoc_dir: File = File(context.filesDir, "mdoc_dir")

            if (!mdoc_dir.exists()) {
                mdoc_dir.mkdir()
            }
            try {
                val userKeyFile = File(mdoc_dir, "user_key.txt")
                val writer = FileWriter(userKeyFile)
                writer.append(user_key.exportJWK())
                writer.flush()
                writer.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Logger.d("USER KEY", this.user_key.exportJWK())
    }

    private suspend fun request_flow(username: String, password: String, client_id: String, client_secret: String, issuer_backend_url: String, issuer_api_url: String, idp_url: String, user_did:JWKKey, context: Context):MDoc?{

        val pirate_ec_key = ECKey.parse(user_key.exportJWK())


        val issuer_jwk = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
        val issuer_ec_key = ECKey.parse(issuer_jwk.exportJWK())


        //Security.addProvider(BouncyCastleProvider())
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(BouncyCastleProvider())
        //Security.insertProviderAt(BouncyCastleProvider(), 1)

        val issuerCertificate = X509v3CertificateBuilder(
            X500Name("CN=MDOC INVALID ISSUER"), BigInteger.valueOf(SecureRandom().nextLong()),
            Date(), Date(1747585131000L), X500Name("CN=MDOC INVALID ISSUER"),
            SubjectPublicKeyInfo.getInstance(issuer_ec_key.toECPublicKey().encoded)
        ) .addExtension(Extension.basicConstraints, true, BasicConstraints(true))
            .addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)) // Key usage not validated.
            .build(JcaContentSignerBuilder("SHA256withECDSA").setProvider("BC").build(issuer_ec_key.toECPrivateKey())).let {
                JcaX509CertificateConverter().setProvider("BC").getCertificate(it)
            }


        // instantiate simple cose crypto provider for issuer keys and certificates
        val cryptoProvider = SimpleCOSECryptoProvider(
            listOf(
                COSECryptoProviderKeyInfo("ISSUER_KEY_ID", AlgorithmID.ECDSA_256, issuer_ec_key.toECPublicKey(), issuer_ec_key.toECPrivateKey(), listOf(issuerCertificate), listOf(issuerCertificate)),
                COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256, pirate_ec_key.toECPublicKey()/*, deviceKeyPair.private*/)
            )
        )

        // create device key info structure of device public key, for holder binding
        val deviceKeyInfo = DeviceKeyInfo(DataElement.fromCBOR(OneKey(pirate_ec_key.toECPublicKey(), null).AsCBOR().EncodeToBytes()))


        val driving_privileges_json = Json.parseToJsonElement("[{ \"codes\": [{\"code\": \"B\"}], \"vehicle_category_code\": \"B\", \"issue_date\": \"2019-01-01\" }]")
        var driving_privileges_list = listOf<MapElement>()
        var map: Map<MapKey, StringElement>  = mapOf()

        driving_privileges_json.jsonArray.forEach { it ->
            val vehicle_category_code = Pair(
                MapKey("vehicle_category_code"), StringElement(it.jsonObject?.get("vehicle_category_code")
                    .toString().replace("\"",""))
            )
            val issue_date = Pair(MapKey("issue_date"), FullDateElement(LocalDate.parse(it.jsonObject?.get("issue_date").toString().replace("\"",""))))

            var expiry_date: Pair<MapKey, FullDateElement>? = null

            if (it.jsonObject?.get("expiry_date").toString().replace("\"","") != "null"){
                expiry_date = Pair(MapKey("expiry_date"), FullDateElement(LocalDate.parse(it.jsonObject?.get("expiry_date").toString().replace("\"",""))))
            }

            println("EXPIRY: " + expiry_date.toString())
            if (expiry_date != null){
                driving_privileges_list = driving_privileges_list.plus(MapElement(mapOf(vehicle_category_code, issue_date, expiry_date)))
            }else{
                driving_privileges_list = driving_privileges_list.plus(MapElement(mapOf(vehicle_category_code, issue_date)))
            }
        }

        val driving_privileges_list_element  = ListElement(driving_privileges_list)


        // build mdoc and sign using issuer key with holder binding to device key
        val mdoc = MDocBuilder("org.iso.18013.5.1.mDL")
            .addItemToSign("org.iso.18013.5.1", "family_name", "Bob".replace("\"","").toDE())
            .addItemToSign("org.iso.18013.5.1", "given_name", "Spuzva".replace("\"","").toDE())
            .addItemToSign("org.iso.18013.5.1", "portrait",
                "_9j_4AAQSkZJRgABAgAAAQABAAD_2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL_2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL_wAARCABIAEgDASIAAhEBAxEB_8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL_8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4-Tl5ufo6erx8vP09fb3-Pn6_8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL_8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3-Pn6_9oADAMBAAIRAxEAPwDp7DSrnxH9qu5b5FlD4w4LE9_wFXLfwVKz_wClahAif9MlLE_njFReEA6zX05yYYYt7AdSeeB-Vb2k311fwzSXKRqnGwICNvtnv2rzcxzfGYbE1KUZWV9PQ8vL8swVfD06s4Xk1dvXfrcyrrwT3sdQVvVbhcfqP8KW28FxqhN9fMW_u2y8D8T_AIVp6fq9tqMOp-R53madO8E6SLtIZRu49iCMHvVCDxdbL4N07xDeW0sa3_lJHbRHe2-RsADOM-v0rjWb5lyct3-uv4nb_Y-A5-fkX6fdsQyeCXEoMeop5J_vxncPyOKfN4Lt_LHkahIH7-bGCp_LkVu310unWN1eXG8wW0bSSbF3NtUEnA78CszUfElpp-jadqcETXUF_cQQxFW28Snhu_TPSojnOYztaT_r-uo_7GwCv-7Wv9fIq2fguIc3t8WHZbdcfqf8KiuPBMpm_wBEvomjPaYEMPy4Na8ms2sXiFNFy5vJLdrkAL8qoG25J7ZPSrdxczW0lssNs0zTPtPUBR3Jpf23j4z5nJ6_1sN5LgeTl5F-v37nHahplx4amtbiO9DyOTjYpXBH8xRUvjD7QNWiEpBj8vMeOnXB_lRX2-SueLwkatezbPis3j9VxTpYd8se12M8LXs1vc3MEMPnNIm9EzjLLnH866y9upbOx86ePdJhQIYuN8jHAUe5YgVyfhKWGHWHaT75hKx_UkZ_Sukvp44dS0iW5dVhW8y5Y9P3cm0_QMR-lfH5vRg8zlTvo2rn2ORzlLAQk1smvuZJaeDtNgkvNU1lY5bu72tdHeVgGxcAbc4IA43NyfbpVZvDXhPXFg_sWezjuLCZbiD7HIGjjkXO0tEDtI59B7EVF401AXtvYCxlt7u3WZmkiLAo8qqTEj-xYHr3Arg_Ct34m1DxIuqalpqafNJfRraxpF5RI3YlTHUrtBOT9fp7dPDQdPTb_IupXcZ8rXb8T0ZL-4vNunRQRrqcjPFcRyZZIQuN7npuXDLtHG7eOnOKkfh3wXoVjZaNqd-kxtgphS_vSSmDlSFyAuM8HAq9Z6josfxD1WFb62F_PbW8QjMwyzKZNygf3gCuR1xivL9YufFm2LTtK0mC6lvJ_MvLp4w0iTKx3o5PCrgD8Olc-EwNKmnbv_X3F1q7VvM9NuPDEOl6jJ4h0tZrmVoFimgkmaUvEp3Dy2JJBHUDOD0461rJOlxbJPA4eGVQysP4geQayfBt3Fp_h-SO5uY0t1uHFpvcDMfH3fUBtwH0p-gTRvoqiMqUE821c_dTzWKj2-XFeTnOHhC0473sdOGlKUVK2jOW8U3bXGqiEjCwJgD6nP8AhRSeJ3hfxDK0JzlF3_72MfyxRX2uRUV9QgfnWf1G8fP5EfhiMSeILcscBFd_rgV0s81xZG8uoraSa5TcwVMbmTttJ4AA_XPXNcdpTuNas4opAs0sm1QT19f0r0hRiTJPPrXyXErcMdzW3SPseGJL6jZd2YtzdWga1uNWRTDKCszSLuQoVztZgMYztIz6cU2DUdEtDIfDtl9pv5EKI8IZ9mfWRuFXOD17dDWgDcaU2yOB7rTzyFi5kg9gP4l9AOR0wRjE9rfWGoOfsd5DIw4KB8OvsVPI_EVw0cwq0qTjBX8_-AexUhGpJOWljm9e8uHwyuhWtqPtGEWBMcibIKsD_e3fNu69TV3UL7Q7lpJ9W0nytQCkEXFu_wA5A4G5AVcfj-Vav2CddWF064VR949uMUTa3p6TGCGf7Xc_88LT96_444X6sQKjD42tRTSje-4TpwluYrTtp2mrdWsAnlYKo5CF2OAByOOcAKBx04q8rTSXttKYil0TmQcHCbTkEjgjOMe_Sp4YZp7xLy-VFePmG2RsrESMFmP8T44z0AOBnJJvclD8oGe471x1Krel79zo59NjzvXYfI127QHILhh9CAaKq3spl1G73sGlSVkcZzgg4waK_Usti44SmvI_J80aljKj8zb8OwJYaAviF13PcXgAHcQIWUAfVsufqPQV1n2iUz2YtoA8E2TJIQflH-f5V5Yq3NrYhLfUbyK2N4tslqsmYwvlNI-Ac45I4FekzajYW-ntDDfxKDHiIhskHHBr4bPqcnX53rq19x-gZXOm6EY09FZP5M00jDZ5BAODg5xXOa5aWM-pafd3WkPq1rH5qyeVAJWUlQBkdcZz9Kt6JLY6Zp5LXVuPMbOFfPt-Jp96-klvtP25rd26tE-N31HSvGoTlSqKaWx6llNWkcpZ6ZpVt4kvr-fwjqEumSqq28Btd5RgFydhPGTn9a6bw5CtnpJj-zNYrNczvFbugRgrSMyjaO-CKa0GirH5r38hY_8ALTzmDVatX0iyjNyLwSMRjzZZNzY9Oa6cTjJV6fI11uKNOENUyTUpZrXT3eD_AFpYLuIyEB6tUWkyzwadPPdzSumdytKcnA6n6U9NYsmladNSjWJVIMZA5OepP9Khudc02ewut10jkxspU9SSDiuKFOTdglK2rOZ16zSXSLfxAiCOR7sxSj-9FIx2Z9wxGPZjRXPM9_eW0C3uo3E1sZ5ojB8qLmMoyZ2gZ4P6UV-nZZCr7C0emn3H5_nNTD_WLtatJ_eWLy3ns5bS1uI2RhdXUpVhjosaA_TrXReC9Z8NWfh6EX5zfLLOshNtI_SV8chSOmKKK8ytN_VufrzP8z38vpx-uOn05UbM-reBrjf5sKvvGGH2GXkf981iy3ugWkjHTLmN1bhTeWU7NBn-JWC_OB_dbn_a7UUV5ftn2R9D9Xj3ZxGqLoY8WRxxXNy_h8zRfbJ38zzN-PnGOuC20kgcEnFdst7os7iK4vo1hT_l7hspRcTr23HZhD2LDJPUbaKKbqcqdkjor4eLUNX8K_U2YNZ8D28cccUIVYxhMWUvH47f1rnvGetaBqS6XFpYYXP23czG3ePKCOTPLAZ5xxRRXTg6rlXgmlujyMxpRjhajX8r_IyLO2mvI1t4I2eQamQAo7Pbg8-2Uooor2KOIqUpTjHbmZ8pUwlOvCnOe_KvyP_Z"
                    .replace("\"","").toDE())
            .addItemToSign("org.iso.18013.5.1", "birth_date", FullDateElement(LocalDate.parse("1999-06-01".replace("\"",""))))
            .addItemToSign("org.iso.18013.5.1", "issue_date", FullDateElement(LocalDate.parse("1999-06-01".replace("\"",""))))
            .addItemToSign("org.iso.18013.5.1", "expiry_date", FullDateElement(LocalDate.parse("1999-06-01".replace("\"",""))))
            .addItemToSign("org.iso.18013.5.1", "issuing_country", "Bikini dolina".replace("\"","").toDE())
            .addItemToSign("org.iso.18013.5.1", "issuing_authority", "MUP".replace("\"","").toDE())
            .addItemToSign("org.iso.18013.5.1", "age_over_18", BooleanElement(true))
            .addItemToSign("org.iso.18013.5.1", "age_over_21", BooleanElement(true))
            .addItemToSign("org.iso.18013.5.1", "age_over_24", BooleanElement(false))
            .addItemToSign("org.iso.18013.5.1", "age_over_65", BooleanElement(false))
            .addItemToSign("org.iso.18013.5.1", "document_number", "1234987".toString().replace("\"","").toDE())
            .addItemToSign("org.iso.18013.5.1", "driving_privileges", driving_privileges_list_element)

            .sign(
                ValidityInfo(Clock.System.now(), Clock.System.now(), Clock.System.now().plus(365*24, DateTimeUnit.HOUR)),
                deviceKeyInfo, cryptoProvider, "ISSUER_KEY_ID"
            )

        Logger.d("CHAIN", mdoc.issuerSigned.issuerAuth!!.x5Chain!!.toString())

        this.driving_credential = mdoc


        val dir: File = File(context.filesDir, "mdoc_dir")
        if (!dir.exists()) {
            dir.mkdir()
        }
        try {
            val mdocFile = File(dir, "mdoc.txt")
            val writer = FileWriter(mdocFile)
            writer.append(mdoc.toCBORHex())
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mdoc
    }

}