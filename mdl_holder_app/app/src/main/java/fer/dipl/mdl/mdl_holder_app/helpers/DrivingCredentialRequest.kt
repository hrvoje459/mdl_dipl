package fer.dipl.mdl.mdl_holder_app.helpers

import android.content.Context
import android.os.StrictMode
import com.android.identity.util.Logger
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.keys.jwk.JWKKeyMetadata
import id.walt.did.dids.registrar.dids.DidJwkCreateOptions
import id.walt.did.dids.registrar.local.jwk.DidJwkRegistrar
import id.walt.mdoc.doc.MDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit


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

        // Create end user did
        val user_options = DidJwkCreateOptions(
            keyType = KeyType.secp256r1
        )

        val registrar = DidJwkRegistrar()
        val user_didResult = registrar.registerByKey(user_key, user_options)

        val user_did = user_didResult.did

        var client = OkHttpClient.Builder()
            .connectTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(35, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .build()
        var mediaType = "application/x-www-form-urlencoded".toMediaType()


        var body = "client_id=${client_id}&username=${username}&password=${password}&grant_type=password&client_secret=${client_secret}".toRequestBody(mediaType)
        var request = Request.Builder()
            .url("${idp_url}/protocol/openid-connect/token")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")

            .build()
        var response = client.newCall(request).execute()
        val token_response_body = response.body?.string()
        val token_response_json = token_response_body?.let { Json.parseToJsonElement(it) }
        val access_token = token_response_json?.jsonObject
            ?.get("access_token")

        request = Request.Builder()
            .url("${issuer_backend_url}/drivers?subject_did="+ user_did)
            .get()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Authorization", "Bearer " + access_token.toString().replace("\"",""))
            .build()
        response = client.newCall(request).execute()
        
        val response_body = response.body?.string()!!
        val credential_offer_uri = Json.parseToJsonElement(response_body).jsonObject.get("credential_offer_uri").toString().replace("\"","")

        Logger.d("OFFER URI:", credential_offer_uri)

        val request_offer_uri = Request.Builder()
            .url(credential_offer_uri)
            .get()
            .build()
        response = client.newCall(request_offer_uri).execute()
        val response_offer_body = response.body?.string()
        val response_offer_json = response_offer_body?.let { Json.parseToJsonElement(it) }


        val pre_auth_code = response_offer_json?.jsonObject
            ?.get("grants")?.jsonObject
            ?.get("urn:ietf:params:oauth:grant-type:pre-authorized_code")?.jsonObject
            ?.get("pre-authorized_code")
            ?.toString()?.replace("\"", "")

        val token_mediaType = "application/x-www-form-urlencoded".toMediaType()
        val token_body = "grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code&pre-authorized_code=$pre_auth_code".toRequestBody(token_mediaType)
        val token_request = Request.Builder()
            .url("${issuer_api_url}/token")
            .post(token_body)
            .build()
        response = client.newCall(token_request).execute()

        val response_token_body = response.body?.string()
        val response_token_json = response_token_body?.let { Json.parseToJsonElement(it) }

        val c_nonce = response_token_json?.jsonObject
            ?.get("c_nonce")

        val access_token2 = response_token_json?.jsonObject
            ?.get("access_token").toString().replace("\"","")

        val headers = mapOf(
            "typ" to "openid4vci-proof+jwt",
            "kid" to user_didResult.did
            //THIS SHOULD FAIL SINCE PROOF IS WRONG//"kid" to "did:jwk:eyJrdHkiOiJFQyIsImNydiI6IlAtMjU2Iiwia2lkIjoiLWdZeUlCY3NSTHZEQjZBQ1JNdXdUbjd2b2tvMHA0dDUzMHJNSGFGNWwwZyIsIngiOiJlc1YzVmJ0NnFoamtPeTVHeWJ2Qnd0MUVfUmxFRDNiZXdGT0NMaTBEai1rIiwieSI6ImlyNmJNdmNidXFlR0tlM3puYlVpWUlOcW90RkRXd0ZDYmxpVndhU0tCaEEifQ"
        )


        val payload = JwtPayload(
            aud = "${issuer_api_url}",
            iat = (System.currentTimeMillis() / 1000),
            nonce = c_nonce.toString().replace("\"", "")
        )

        val json = Json.encodeToString(JwtPayload.serializer(), payload)
        val signature = user_key.signJws(json.encodeToByteArray(), headers = headers)

        val jwt_proof_payload = JWTProofPayload(
            proof_type = "jwt",
            jwt = signature
        )

        val credential_request_payload = CredentialRequestPayload(
            credentialIdentifier = "Iso18013DriversLicenseCredential",
            format = "mso_mdoc",
            proof = jwt_proof_payload,
            types = arrayOf("Iso18013DriversLicenseCredential")
        )
        val credential_json = Json.encodeToString(CredentialRequestPayload.serializer(), credential_request_payload)
        val credential_request_body = credential_json.toRequestBody()

        //val credential_mediaType = "application/json".toMediaType()

        val credential_request = Request.Builder()
            .url("${issuer_api_url}/credential")
            .post(credential_request_body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $access_token2")
            .build()
        val credential_response = client.newCall(credential_request).execute()

        val mdoc = credential_response.body?.string()?.let { Json.parseToJsonElement(it) }
        println("MDOC: " + mdoc?.jsonObject?.get("credential"))

        val mdoc_mdoc = MDoc.fromCBORHex(mdoc?.jsonObject?.get("credential").toString().replace("\"",""))

        this.driving_credential = mdoc_mdoc


        val dir: File = File(context.filesDir, "mdoc_dir")
        if (!dir.exists()) {
            dir.mkdir()
        }
        try {
            val mdocFile = File(dir, "mdoc.txt")
            val writer = FileWriter(mdocFile)
            writer.append(mdoc_mdoc.toCBORHex())
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mdoc_mdoc
    }

}