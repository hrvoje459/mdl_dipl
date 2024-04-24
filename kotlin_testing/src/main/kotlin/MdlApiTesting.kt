package fer.dipl.mdl

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.keys.jwk.JWKKeyMetadata
import id.walt.did.dids.registrar.dids.DidJwkCreateOptions
import id.walt.did.dids.registrar.local.jwk.DidJwkRegistrar
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLDecoder

class MdlApiTesting {
}

suspend fun main (){

    println("Hello, " + "Hrvoje" + "!")


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


    // Create ISSUER KEY

    val issuer_key = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())
    println("Issuer KEY: " + issuer_key.exportJWK())
    val issuer_key_export = issuer_key.exportJWK()

    val issuer_options = DidJwkCreateOptions(
        keyType = KeyType.secp256r1
    )
    val issuer_didResult = registrar.registerByKey(user_key, issuer_options)
    println("Issuer DiD: " + issuer_didResult)
    println("Issuer DiD: " + issuer_didResult.did)

    val issuer_did = issuer_didResult.did

    val drivingLicenseVerificableCredentail = "{\n" +
            "  \"@context\": [\n" +
            "    \"https://www.w3.org/2018/credentials/v1\",\n" +
            "    \"https://w3id.org/vdl/v1\",\n" +
            "    \"https://w3id.org/vdl/aamva/v1\"\n" +
            "  ],\n" +
            "  \"type\": [\n" +
            "    \"VerifiableCredential\",\n" +
            "    \"Iso18013DriversLicenseCredential\"\n" +
            "  ],\n" +
            "  \"issuer\": {\n" +
            "    \"id\": \"$issuer_did\",\n" +
            "    \"name\": \"Utopia Department of Motor Vehicles\",\n" +
            "    \"url\": \"https://dmv.utopia.example/\",\n" +
            "    \"image\": \"https://dmv.utopia.example/logo.png\"\n" +
            "  },\n" +
            "  \"issuanceDate\": \"2023-11-15T10:00:00-07:00\",\n" +
            "  \"expirationDate\": \"2028-11-15T12:00:00-06:00\",\n" +
            "  \"name\": \"Utopia Driver's License\",\n" +
            "  \"image\": \"data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUg...kSuQmCC\",\n" +
            "  \"description\": \"A license granting driving privileges in Utopia.\",\n" +
            "  \"credentialSubject\": {\n" +
            "    \"id\": \"$user_did\",\n" +
            "    \"type\": \"LicensedDriver\",\n" +
            "    \"driversLicense\": {\n" +
            "      \"type\": \"Iso18013DriversLicense\",\n" +
            "      \"document_number\": \"542426814\",\n" +
            "      \"family_name\": \"TURNER\",\n" +
            "      \"given_name\": \"SUSAN\",\n" +
            "      \"portrait\": \"data:image/jpeg;base64,/9j/4AAQSkZJR...RSClooooP/2Q==\",\n" +
            "      \"birth_date\": \"1998-08-28\",\n" +
            "      \"issue_date\": \"2023-01-15T10:00:00-07:00\",\n" +
            "      \"expiry_date\": \"2028-08-27T12:00:00-06:00\",\n" +
            "      \"issuing_country\": \"UA\",\n" +
            "      \"issuing_authority\": \"UADMV\",\n" +
            "      \"driving_privileges\": [{\n" +
            "        \"codes\": [{\"code\": \"D\"}],\n" +
            "        \"vehicle_category_code\": \"D\",\n" +
            "        \"issue_date\": \"2019-01-01\",\n" +
            "        \"expiry_date\": \"2027-01-01\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"codes\": [{\"code\": \"C\"}],\n" +
            "        \"vehicle_category_code\": \"C\",\n" +
            "        \"issue_date\": \"2019-01-01\",\n" +
            "        \"expiry_date\": \"2017-01-01\"\n" +
            "      }],\n" +
            "      \"un_distinguishing_sign\": \"UTA\",\n" +
            "      \"aamva_aka_suffix\": \"1ST\",\n" +
            "      \"sex\": 2,\n" +
            "      \"aamva_family_name_truncation\": \"N\",\n" +
            "      \"aamva_given_name_truncation\": \"N\"\n" +
            "    }\n" +
            "  }\n" +
            "}"

    println("HRVOJE JWK: " + issuer_key_export)

    val drivingLicenseCredentailRequest = "{\n" +
            "  \"issuerKey\": {\n" +
            "    \"type\": \"jwk\",\n" +
            "    \"jwk\": \"${issuer_key_export.replace("\"","\\\"")}\"\n" +
            "  },\n" +
            "  \"issuerDid\": \"$issuer_did\",\n" +
            "  \"vc\": $drivingLicenseVerificableCredentail,\n" +
            "  \"mapping\": {\n" +
            "    \"id\": \"<uuid>\",\n" +
            "    \"issuer\": {\n" +
            "      \"id\": \"<issuerDid>\"\n" +
            "    },\n" +
            "    \"credentialSubject\": {\n" +
            "      \"id\": \"<subjectDid>\"\n" +
            "    },\n" +
            "    \"issuanceDate\": \"<timestamp>\",\n" +
            "    \"expirationDate\": \"<timestamp-in:365d>\"\n" +
            "  }"+
            "}"

    println("HRVOJE REQ: " + drivingLicenseCredentailRequest)

    val drivingLicenseVerificableCredentailRequestBody = drivingLicenseCredentailRequest.toRequestBody("application/json".toMediaType())




    val credential_issuance_request_body = "{\n" +
            "   \"issuanceKey\":{\n" +
            "      \"type\":\"jwk\",\n" +
            "      \"jwk\":\"$issuer_key_export\"\n" +
            "   },\n" +
            "   \"issuerDid\":\"$issuer_did\",\n" +
            "   \"vc\":{\n" +
            "      \"@context\":[\n" +
            "         \"https://www.w3.org/2018/credentials/v1\",\n" +
            "         \"https://www.w3.org/2018/credentials/examples/v1\"\n" +
            "      ],\n" +
            "      \"id\":\"http://example.gov/credentials/3732\",\n" +
            "      \"type\":[\n" +
            "         \"VerifiableCredential\",\n" +
            "         \"UniversityDegree\"\n" +
            "      ],\n" +
            "      \"issuer\":{\n" +
            "         \"id\":\"$issuer_did\"\n" +
            "      },\n" +
            "      \"issuanceDate\":\"2024-03-27T00:14:12.164Z\",\n" +
            "      \"credentialSubject\":{\n" +
            "         \"id\":\"$user_did\",\n" +
            "         \"degree\":{\n" +
            "            \"type\":\"BachelorDegree\",\n" +
            "            \"name\":\"Bachelor of Science and Arts\"\n" +
            "         }\n" +
            "      }\n" +
            "   },\n" +
            "   \"mapping\":{\n" +
            "      \"id\":\"<uuid>\",\n" +
            "      \"issuer\":{\n" +
            "         \"id\":\"<issuerDid>\"\n" +
            "      },\n" +
            "      \"credentialSubject\":{\n" +
            "         \"id\":\"<subjectDid>\"\n" +
            "      },\n" +
            "      \"issuanceDate\":\"<timestamp>\",\n" +
            "      \"expirationDate\":\"<timestamp-in:365d>\"\n" +
            "   },\n" +
            "   \"selectiveDisclosure\":{\n" +
            "      \"fields\":{\n" +
            "         \"issuanceDate\":{\n" +
            "            \"sd\":true\n" +
            "         },\n" +
            "         \"credentialSubject\":{\n" +
            "            \"sd\":false,\n" +
            "            \"children\":{\n" +
            "               \"fields\":{\n" +
            "                  \"degree\":{\n" +
            "                     \"sd\":false,\n" +
            "                     \"children\":{\n" +
            "                        \"fields\":{\n" +
            "                           \"name\":{\n" +
            "                              \"sd\":true\n" +
            "                           }\n" +
            "                        }\n" +
            "                     }\n" +
            "                  }\n" +
            "               }\n" +
            "            }\n" +
            "         }\n" +
            "      }\n" +
            "   }\n" +
            "}"

    println("ISSUANCE REQUEST BODY: " + credential_issuance_request_body)

    val client = OkHttpClient()
    val mediaType = "application/json".toMediaType()
    val body = "{\n  \"issuerKey\": {\n    \"type\": \"jwk\",\n    \"jwk\": \"{\\\"kty\\\":\\\"OKP\\\",\\\"d\\\":\\\"jQGuwJkF6umOyX4dSv_bRVozjKRc2NvPg1JSt39PaHo\\\",\\\"crv\\\":\\\"Ed25519\\\",\\\"kid\\\":\\\"ByUineP2PKbozXLH8nJiYrXUIxX5CjvinipFBBBCOi4\\\",\\\"x\\\":\\\"6zAA6R5vwH-WnAFh8ZSEIrF7sqG7T8BiuiQQhxvB4OQ\\\"}\"\n  },\n  \"issuerDid\": \"did:jwk:eyJrdHkiOiJPS1AiLCJjcnYiOiJFZDI1NTE5Iiwia2lkIjoiQnlVaW5lUDJQS2JvelhMSDhuSmlZclhVSXhYNUNqdmluaXBGQkJCQ09pNCIsIngiOiI2ekFBNlI1dndILVduQUZoOFpTRUlyRjdzcUc3VDhCaXVpUVFoeHZCNE9RIn0\",\n  \"vc\": {\n    \"@context\": [\n      \"https://www.w3.org/2018/credentials/v1\",\n      \"https://www.w3.org/2018/credentials/examples/v1\"\n    ],\n    \"id\": \"http://example.gov/credentials/3732\",\n    \"type\": [\n      \"VerifiableCredential\",\n      \"UniversityDegree\"\n    ],\n    \"issuer\": {\n      \"id\": \"did:web:vc.transmute.world\"\n    },\n    \"issuanceDate\": \"2024-03-27T00:14:12.164Z\",\n    \"credentialSubject\": {\n      \"id\": \"did:example:ebfeb1f712ebc6f1c276e12ec21\",\n      \"degree\": {\n        \"type\": \"BachelorDegree\",\n        \"name\": \"Bachelor of Science and Arts\"\n      }\n    }\n  },\n  \"mapping\": {\n    \"id\": \"<uuid>\",\n    \"issuer\": {\n      \"id\": \"<issuerDid>\"\n    },\n    \"credentialSubject\": {\n      \"id\": \"<subjectDid>\"\n    },\n    \"issuanceDate\": \"<timestamp>\",\n    \"expirationDate\": \"<timestamp-in:365d>\"\n  }\n}".toRequestBody(mediaType)
    val body2 = "{\n  \"issuerKey\": {\n    \"type\": \"jwk\",\n    \"jwk\": \"{\\\"kty\\\":\\\"OKP\\\",\\\"d\\\":\\\"jQGuwJkF6umOyX4dSv_bRVozjKRc2NvPg1JSt39PaHo\\\",\\\"crv\\\":\\\"Ed25519\\\",\\\"kid\\\":\\\"ByUineP2PKbozXLH8nJiYrXUIxX5CjvinipFBBBCOi4\\\",\\\"x\\\":\\\"6zAA6R5vwH-WnAFh8ZSEIrF7sqG7T8BiuiQQhxvB4OQ\\\"}\"\n  },\n  \"issuerDid\": \"did:jwk:eyJrdHkiOiJPS1AiLCJjcnYiOiJFZDI1NTE5Iiwia2lkIjoiQnlVaW5lUDJQS2JvelhMSDhuSmlZclhVSXhYNUNqdmluaXBGQkJCQ09pNCIsIngiOiI2ekFBNlI1dndILVduQUZoOFpTRUlyRjdzcUc3VDhCaXVpUVFoeHZCNE9RIn0\",\n  \"vc\": {\n    \"@context\": [\n      \"https://www.w3.org/2018/credentials/v1\",\n      \"https://www.w3.org/2018/credentials/examples/v1\"\n    ],\n    \"id\": \"http://example.gov/credentials/3732\",\n    \"type\": [\n      \"VerifiableCredential\",\n      \"UniversityDegree\"\n    ],\n    \"issuer\": {\n      \"id\": \"did:web:vc.transmute.world\"\n    },\n    \"issuanceDate\": \"2024-03-27T00:14:12.164Z\",\n    \"credentialSubject\": {\n      \"id\": \"did:example:ebfeb1f712ebc6f1c276e12ec21\",\n      \"degree\": {\n        \"type\": \"BachelorDegree\",\n        \"name\": \"Bachelor of Science and Arts\"\n      }\n    }\n  },\n  \"mapping\": {\n    \"id\": \"<uuid>\",\n    \"issuer\": {\n      \"id\": \"<issuerDid>\"\n    },\n    \"credentialSubject\": {\n      \"id\": \"<subjectDid>\"\n    },\n    \"issuanceDate\": \"<timestamp>\",\n    \"expirationDate\": \"<timestamp-in:365d>\"\n  },\n  \"selectiveDisclosure\": {\n  \"fields\": {\n    \"issuanceDate\": {\n      \"sd\": true\n    },\n    \"credentialSubject\": {\n      \"sd\": false,\n      \"children\": {\n        \"fields\": {\n          \"degree\": {\n            \"sd\": false,\n            \"children\": {\n              \"fields\": {\n                \"name\": {\n                  \"sd\": true\n                }\n              }\n            }\n          }\n        }\n      }\n    }\n  }\n}\n}"
    println(body2)
    val request = Request.Builder()
        .url("http://localhost:7002/openid4vc/mdoc/issue")
        .post(drivingLicenseVerificableCredentailRequestBody)
        .addHeader("accept", "text/plain")
        .addHeader("Content-Type", "application/json")
        .build()
    var response = client.newCall(request).execute()

    println("RESPONSE: " + response)

    val response_body = response.body?.string()
    println(response_body)

    println(response_body?.split("?")?.get(1)?.split("=")?.get(1))

    println(URLDecoder.decode(response_body?.split("?")?.get(1)?.split("=")?.get(1),"UTF-8"))

    //val credential_offer_uri = URLDecoder.decode(response_body?.split("?")?.get(1)?.split("=")?.get(1),"UTF-8")
    val credential_offer_uri = "http://localhost:7002/openid4vc/credentialOffer?id=f7e6fe37-694e-45b6-84dc-5b19d35f9ec9"

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

    val access_token = response_token_json?.jsonObject
        ?.get("access_token").toString().replace("\"","")


    println("ACCESS TOKEN: " + access_token)
    println("NONCENCE: " + c_nonce)


    val headers = mapOf(
        "typ" to "openid4vci-proof+jwt",
        "kid" to issuer_didResult.did
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
        credentialIdentifier = "UniversityDegree",
        format = "jwt_vc_json",
        proof = jwt_proof_payload,
        types = arrayOf("testhrv")
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
        .addHeader("Authorization", "Bearer $access_token")
        .build()
    val credential_response = client.newCall(credential_request).execute()

    println("REQUEST: " + credential_request)

    println("RESPONSE: " + credential_response)
    println("RESPONSE: " + credential_response.body?.string())

    //val verificationResult = key.getPublicKey().verifyJws(signature)

    //print(verificationResult)




}