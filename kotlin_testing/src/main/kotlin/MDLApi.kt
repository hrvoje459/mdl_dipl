package fer.dipl.mdl

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.keys.jwk.JWKKeyMetadata
import id.walt.did.dids.registrar.dids.DidJwkCreateOptions
import id.walt.did.dids.registrar.local.jwk.DidJwkRegistrar
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class MDLApi {

}

suspend fun main (){



    val registrar = DidJwkRegistrar()

    // Create end user key (wallet)
    val user_key = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
    println("Wallet KEY: " + user_key.exportJWK())

    // Create end user did
    val user_options = DidJwkCreateOptions(
        keyType = KeyType.secp256r1
    )
    val user_didResult = registrar.registerByKey(user_key, user_options)
    println("User DiD: " + user_didResult)
    println("User DiD: " + user_didResult.did)

    val user_did = user_didResult.did




    var client = OkHttpClient()
    var mediaType = "application/x-www-form-urlencoded".toMediaType()
    var body = "client_id=<client_id>&username=<username>&password=<password>&grant_type=password&client_secret=<client_secret>".toRequestBody(mediaType)
    
    var request = Request.Builder()
        .url("https://keycloak2.oauthkafka.xyz/auth/realms/MDL/protocol/openid-connect/token")
        .post(body)
        .addHeader("Content-Type", "application/x-www-form-urlencoded")
        .build()
    var response = client.newCall(request).execute()

    //println("LOGIN: " + response.body?.string())

    val token_response_body = response.body?.string()

    //println("LOGIN: " + token_response_body)

    val token_response_json = token_response_body?.let { Json.parseToJsonElement(it) }

    //println(token_response_json)

    val access_token = token_response_json?.jsonObject
        ?.get("access_token")

    println("ACCESS TOKEN: " + access_token)

    request = Request.Builder()
        .url("http://localhost:8080/drivers?subject_did=" + user_did)
        .get()
        .addHeader("Content-Type", "application/x-www-form-urlencoded")
        .addHeader("Authorization", "Bearer " + access_token.toString().replace("\"",""))
        .build()

    //println("REQUST: " + request)
    response = client.newCall(request).execute()

    println("GET OFFER: " + response.code)
    //println("GET OFFER: " + response.headers)
    //println("GET OFFER: " + response.body)
    //println("GET OFFER: " + response.body?.string())


    //val credential_offer_uri = response.body?.string()?.substring(8)
    val credential_offer_uri = Json.parseToJsonElement(response.body?.string()!!).jsonObject.get("credential_offer_uri").toString().replace("\"","")

    println("OFFER URI: " + credential_offer_uri)

    val request_offer_uri = Request.Builder()
        .url(credential_offer_uri)
        .get()
        .build()
    response = client.newCall(request_offer_uri).execute()

    println("RESPONSE OFFER: " + response)

    val response_offer_body = response.body?.string()
    val response_offer_json = response_offer_body?.let { Json.parseToJsonElement(it) }

    println("RESPONSE OFFER: " + response_offer_body)
    //println(response.body?.string()?.let { Json.decodeFromString(it) })
    //val response_body_json = response.body?.string()?.let { Json.decodeFromString<Json>(it) }
    println("JSON BODY: " + response_offer_json)


    val issuer_state = response_offer_json?.jsonObject
        ?.get("grants")?.jsonObject
        ?.get("authorization_code")?.jsonObject
        ?.get("issuer_state")

    val pre_auth_code = response_offer_json?.jsonObject
        ?.get("grants")?.jsonObject
        ?.get("urn:ietf:params:oauth:grant-type:pre-authorized_code")?.jsonObject
        ?.get("pre-authorized_code")
        ?.toString()?.replace("\"", "")

    val auth_code = response_offer_json?.jsonObject
        ?.get("grants")?.jsonObject
        ?.get("authorization_code")?.jsonObject
        ?.get("issuer_state")

    println("Issuer state : " +
            response_offer_json?.jsonObject
                ?.get("grants")?.jsonObject
                ?.get("authorization_code")?.jsonObject
                ?.get("issuer_state")
    )
    println("Pre auth code : " +
            response_offer_json?.jsonObject
                ?.get("grants")?.jsonObject
                ?.get("urn:ietf:params:oauth:grant-type:pre-authorized_code")?.jsonObject
                ?.get("pre-authorized_code")
    )


    // Get access token
    val token_mediaType = "application/x-www-form-urlencoded".toMediaType()
    val token_body = "grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code&pre-authorized_code=$pre_auth_code".toRequestBody(token_mediaType)
    val token_request = Request.Builder()
        .url("http://localhost:7002/token")
        .post(token_body)
        .build()
    response = client.newCall(token_request).execute()

    println("TOKEN RESPONSE: " + response)

    val response_token_body = response.body?.string()
    println("TOKEN BODY: " + response_token_body)

    val response_token_json = response_token_body?.let { Json.parseToJsonElement(it) }

    val c_nonce = response_token_json?.jsonObject
        ?.get("c_nonce")

    val access_token2 = response_token_json?.jsonObject
        ?.get("access_token").toString().replace("\"","")


    println("ACCESS TOKEN: " + access_token2)
    println("NONCENCE: " + c_nonce)


    val headers = mapOf(
        "typ" to "openid4vci-proof+jwt",
        "kid" to user_didResult.did
        //THIS SHOULD FAIL SINCE PROOF IS WRONG//"kid" to "did:jwk:eyJrdHkiOiJFQyIsImNydiI6IlAtMjU2Iiwia2lkIjoiLWdZeUlCY3NSTHZEQjZBQ1JNdXdUbjd2b2tvMHA0dDUzMHJNSGFGNWwwZyIsIngiOiJlc1YzVmJ0NnFoamtPeTVHeWJ2Qnd0MUVfUmxFRDNiZXdGT0NMaTBEai1rIiwieSI6ImlyNmJNdmNidXFlR0tlM3puYlVpWUlOcW90RkRXd0ZDYmxpVndhU0tCaEEifQ"
    )

    println("HEADERS" + headers)

    // Create an instance of your payload data class
    val payload = JwtPayload(
        aud = "http://localhost:7002",
        iat = (System.currentTimeMillis() / 1000),
        nonce = c_nonce.toString().replace("\"", "")
    )

    println(payload)

    // Serialize the payload to JSON
    val json = Json.encodeToString(JwtPayload.serializer(), payload)
    val signature = user_key.signJws(json.encodeToByteArray(), headers = headers)

    println(signature)

    // REQUEST CREDENTIAL

    val jwt_proof_payload = JWTProofPayload(
        proof_type = "jwt",
        jwt = signature
    )

    val credential_request_payload = CredentialRequestPayload(
        //credentialIdentifier = "UniversityDegree",
        credentialIdentifier = "Iso18013DriversLicenseCredential",
        //format = "jwt_vc_json",
        format = "mso_mdoc",
        proof = jwt_proof_payload,
        types = arrayOf("Iso18013DriversLicenseCredential")
    )

    val credential_json = Json.encodeToString(CredentialRequestPayload.serializer(), credential_request_payload)
    val credential_request_body = credential_json.toRequestBody()

    //val client = OkHttpClient()
    val credential_mediaType = "application/json".toMediaType()
    //val credential_request_body = "{\n  \"issuanceKey\": {\n    \"type\": \"jwk\",\n    \"jwk\": \"{\\\"kty\\\":\\\"OKP\\\",\\\"d\\\":\\\"jQGuwJkF6umOyX4dSv_bRVozjKRc2NvPg1JSt39PaHo\\\",\\\"crv\\\":\\\"Ed25519\\\",\\\"kid\\\":\\\"ByUineP2PKbozXLH8nJiYrXUIxX5CjvinipFBBBCOi4\\\",\\\"x\\\":\\\"6zAA6R5vwH-WnAFh8ZSEIrF7sqG7T8BiuiQQhxvB4OQ\\\"}\"\n  },\n  \"issuerDid\": \"did:jwk:eyJrdHkiOiJPS1AiLCJjcnYiOiJFZDI1NTE5Iiwia2lkIjoiQnlVaW5lUDJQS2JvelhMSDhuSmlZclhVSXhYNUNqdmluaXBGQkJCQ09pNCIsIngiOiI2ekFBNlI1dndILVduQUZoOFpTRUlyRjdzcUc3VDhCaXVpUVFoeHZCNE9RIn0\",\n  \"vc\": {\n    \"@context\": [\n      \"https://www.w3.org/2018/credentials/v1\",\n      \"https://www.w3.org/2018/credentials/examples/v1\"\n    ],\n    \"id\": \"http://example.gov/credentials/3732\",\n    \"type\": [\n      \"VerifiableCredential\",\n      \"UniversityDegree\"\n    ],\n    \"issuer\": {\n      \"id\": \"did:web:vc.transmute.world\"\n    },\n    \"issuanceDate\": \"2024-03-27T00:14:12.164Z\",\n    \"credentialSubject\": {\n      \"id\": \"did:example:ebfeb1f712ebc6f1c276e12ec21\",\n      \"degree\": {\n        \"type\": \"BachelorDegree\",\n        \"name\": \"Bachelor of Science and Arts\"\n      }\n    }\n  },\n  \"mapping\": {\n    \"id\": \"<uuid>\",\n    \"issuer\": {\n      \"id\": \"<issuerDid>\"\n    },\n    \"credentialSubject\": {\n      \"id\": \"<subjectDid>\"\n    },\n    \"issuanceDate\": \"<timestamp>\",\n    \"expirationDate\": \"<timestamp-in:365d>\"\n  },\n  \"selectiveDisclosure\": {\n  \"fields\": {\n    \"issuanceDate\": {\n      \"sd\": true\n    },\n    \"credentialSubject\": {\n      \"sd\": false,\n      \"children\": {\n        \"fields\": {\n          \"degree\": {\n            \"sd\": false,\n            \"children\": {\n              \"fields\": {\n                \"name\": {\n                  \"sd\": true\n                }\n              }\n            }\n          }\n        }\n      }\n    }\n  }\n}\n}".toRequestBody(credential_mediaType)
    val credential_request = Request.Builder()
        .url("http://localhost:7002/credential")
        .post(credential_request_body)
        .addHeader("Content-Type", "application/json")
        .addHeader("Authorization", "Bearer $access_token2")
        .build()
    val credential_response = client.newCall(credential_request).execute()

    println("REQUEST: " + credential_request)

    println("RESPONSE: " + credential_response)
    println("RESPONSE: " + credential_response.body?.string())

    //val verificationResult = key.getPublicKey().verifyJws(signature)

    //print(verificationResult)

}