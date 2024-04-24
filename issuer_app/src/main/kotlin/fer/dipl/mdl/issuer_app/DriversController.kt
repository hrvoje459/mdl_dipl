package fer.dipl.mdl.issuer_app

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLDecoder

class StringResponse( val credential_offer_uri: String)

@RestController
class InitRestController {
    @Value("\${issuer.country_code}")
    private val cc: String? = null

    @Autowired
    private val key = KeyCertGenerator()


    //@Autowired
    //private val ct: AppProperties?= null

    @GetMapping("/")
    suspend fun index(): String {
        return "Greetings from Spring Boot!"
    }

    @GetMapping("/drivers", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    suspend fun drivers(@RequestParam("subject_did") subject_did: String, auth: OAuth2AuthenticationToken): StringResponse {

        val issuer_did = key.getDid()
        val issuer_jwk = key.getIssuerJwk()
        var token: String = ""

        var authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null){
            token = authentication.details.toString()
        }

        println(token)
        println("1" + auth)
        println("2" + auth.name)
        println("3" + auth.details)
        println("4" + auth.credentials)
        println("5" + auth.authorizedClientRegistrationId)
        println("6" + auth.authorities)
        println("7" + auth.principal)
        println("8" + auth.isAuthenticated)

        println()
        println("7.1" + auth.principal.attributes)


        val family_name = auth.principal.attributes.get("family_name")
        val given_name = auth.principal.attributes.get("given_name")
        val birth_date = auth.principal.attributes.get("birth_date")
        val issue_date = auth.principal.attributes.get("issue_date")
        val expiry_date = auth.principal.attributes.get("expiry_date")
        val issuing_country = auth.principal.attributes.get("issuing_country")
        val issuing_authority = auth.principal.attributes.get("issuing_authority")
        val document_number = auth.principal.attributes.get("document_number")
        val driving_privileges = auth.principal.attributes.get("driving_privileges")
        val portrait = auth.principal.attributes.get("portrait")
        val age_over_18 = auth.principal.attributes.get("age_over_18")
        val age_over_21 = auth.principal.attributes.get("age_over_21")
        val age_over_24 = auth.principal.attributes.get("age_over_24")
        val age_over_65 = auth.principal.attributes.get("age_over_65")


        println("Prezime: " + family_name)
        println("Ime: " + given_name)
        println("Datum rodenja: " + birth_date)
        println("Datum izdavanja: " + issue_date)
        println("Datum isteka: " + expiry_date)
        println("Drzava: " + issuing_country)
        println("Izdavac: " + issuing_authority)
        println("Broj dokumenta: " + document_number)
        println("Privilegije: " + driving_privileges)
        println("Portret: " + portrait)
        println("Preko 18: " + age_over_18)
        println("Preko 21: " + age_over_21)
        println("Preko 24: " + age_over_24)
        println("Preko 65: " + age_over_65)


        println("HRVOJE DID: " + issuer_did)

        val driving_license_credential = "{\n" +
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
                "    \"name\": \"Croatian Department of Motor Vehicles\",\n" +
                "    \"url\": \"https://dmv.utopia.example/\",\n" +
                "    \"image\": \"https://dmv.utopia.example/logo.png\"\n" +
                "  },\n" +
                "  \"issuanceDate\": \"$issue_date\",\n" +
                "  \"expirationDate\": \"$expiry_date\",\n" +
                "  \"name\": \"Croatia Driver's License\",\n" +
                "  \"image\": \"$portrait\",\n" +
                "  \"description\": \"A license granting driving privileges in Croatia.\",\n" +
                "  \"credentialSubject\": {\n" +
                "    \"id\": \"$subject_did\",\n" +
                "    \"type\": \"LicensedDriver\",\n" +
                "    \"driversLicense\": {\n" +
                "      \"type\": \"Iso18013DriversLicense\",\n" +
                "      \"document_number\": \"$document_number\",\n" +
                "      \"family_name\": \"$family_name\",\n" +
                "      \"given_name\": \"$given_name\",\n" +
                "      \"portrait\": \"$portrait\",\n" +
                "      \"birth_date\": \"$birth_date\",\n" +
                "      \"issue_date\": \"$issue_date\",\n" +
                "      \"expiry_date\": \"$expiry_date\",\n" +
                "      \"issuing_country\": \"$issuing_country\",\n" +
                "      \"issuing_authority\": \"$issuing_authority\",\n" +
                "      \"driving_privileges\": $driving_privileges,\n" +
                "      \"age_over_18\": $age_over_18,\n" +
                "      \"age_over_21\": $age_over_21,\n" +
                "      \"age_over_24\": $age_over_24,\n" +
                "      \"age_over_65\": $age_over_65\n" +
                "    }\n" +
                "  }\n" +
                "}"

        val drivingLicenseCredentailRequest = "{\n" +
                "  \"issuerKey\": {\n" +
                "    \"type\": \"jwk\",\n" +
                "    \"jwk\": \"${issuer_jwk.replace("\"","\\\"")}\"\n" +
                "  },\n" +
                "  \"issuerDid\": \"$issuer_did\",\n" +
                "  \"vc\": $driving_license_credential,\n" +
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

        println("DRIVING: " + drivingLicenseCredentailRequest)
        println("SUBJECT DID: " + subject_did)

        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        //val body = "{\n  \"issuerKey\": {\n    \"type\": \"jwk\",\n    \"jwk\": \"{\\\"kty\\\":\\\"OKP\\\",\\\"d\\\":\\\"jQGuwJkF6umOyX4dSv_bRVozjKRc2NvPg1JSt39PaHo\\\",\\\"crv\\\":\\\"Ed25519\\\",\\\"kid\\\":\\\"ByUineP2PKbozXLH8nJiYrXUIxX5CjvinipFBBBCOi4\\\",\\\"x\\\":\\\"6zAA6R5vwH-WnAFh8ZSEIrF7sqG7T8BiuiQQhxvB4OQ\\\"}\"\n  },\n  \"issuerDid\": \"did:jwk:eyJrdHkiOiJPS1AiLCJjcnYiOiJFZDI1NTE5Iiwia2lkIjoiQnlVaW5lUDJQS2JvelhMSDhuSmlZclhVSXhYNUNqdmluaXBGQkJCQ09pNCIsIngiOiI2ekFBNlI1dndILVduQUZoOFpTRUlyRjdzcUc3VDhCaXVpUVFoeHZCNE9RIn0\",\n  \"vc\": {\n    \"@context\": [\n      \"https://www.w3.org/2018/credentials/v1\",\n      \"https://www.w3.org/2018/credentials/examples/v1\"\n    ],\n    \"id\": \"http://example.gov/credentials/3732\",\n    \"type\": [\n      \"VerifiableCredential\",\n      \"UniversityDegree\"\n    ],\n    \"issuer\": {\n      \"id\": \"did:web:vc.transmute.world\"\n    },\n    \"issuanceDate\": \"2024-03-27T00:14:12.164Z\",\n    \"credentialSubject\": {\n      \"id\": \"did:example:ebfeb1f712ebc6f1c276e12ec21\",\n      \"degree\": {\n        \"type\": \"BachelorDegree\",\n        \"name\": \"Bachelor of Science and Arts\"\n      }\n    }\n  },\n  \"mapping\": {\n    \"id\": \"<uuid>\",\n    \"issuer\": {\n      \"id\": \"<issuerDid>\"\n    },\n    \"credentialSubject\": {\n      \"id\": \"<subjectDid>\"\n    },\n    \"issuanceDate\": \"<timestamp>\",\n    \"expirationDate\": \"<timestamp-in:365d>\"\n  }\n}".toRequestBody(mediaType)
        //val body2 = "{\n  \"issuerKey\": {\n    \"type\": \"jwk\",\n    \"jwk\": \"{\\\"kty\\\":\\\"OKP\\\",\\\"d\\\":\\\"jQGuwJkF6umOyX4dSv_bRVozjKRc2NvPg1JSt39PaHo\\\",\\\"crv\\\":\\\"Ed25519\\\",\\\"kid\\\":\\\"ByUineP2PKbozXLH8nJiYrXUIxX5CjvinipFBBBCOi4\\\",\\\"x\\\":\\\"6zAA6R5vwH-WnAFh8ZSEIrF7sqG7T8BiuiQQhxvB4OQ\\\"}\"\n  },\n  \"issuerDid\": \"did:jwk:eyJrdHkiOiJPS1AiLCJjcnYiOiJFZDI1NTE5Iiwia2lkIjoiQnlVaW5lUDJQS2JvelhMSDhuSmlZclhVSXhYNUNqdmluaXBGQkJCQ09pNCIsIngiOiI2ekFBNlI1dndILVduQUZoOFpTRUlyRjdzcUc3VDhCaXVpUVFoeHZCNE9RIn0\",\n  \"vc\": {\n    \"@context\": [\n      \"https://www.w3.org/2018/credentials/v1\",\n      \"https://www.w3.org/2018/credentials/examples/v1\"\n    ],\n    \"id\": \"http://example.gov/credentials/3732\",\n    \"type\": [\n      \"VerifiableCredential\",\n      \"UniversityDegree\"\n    ],\n    \"issuer\": {\n      \"id\": \"did:web:vc.transmute.world\"\n    },\n    \"issuanceDate\": \"2024-03-27T00:14:12.164Z\",\n    \"credentialSubject\": {\n      \"id\": \"did:example:ebfeb1f712ebc6f1c276e12ec21\",\n      \"degree\": {\n        \"type\": \"BachelorDegree\",\n        \"name\": \"Bachelor of Science and Arts\"\n      }\n    }\n  },\n  \"mapping\": {\n    \"id\": \"<uuid>\",\n    \"issuer\": {\n      \"id\": \"<issuerDid>\"\n    },\n    \"credentialSubject\": {\n      \"id\": \"<subjectDid>\"\n    },\n    \"issuanceDate\": \"<timestamp>\",\n    \"expirationDate\": \"<timestamp-in:365d>\"\n  },\n  \"selectiveDisclosure\": {\n  \"fields\": {\n    \"issuanceDate\": {\n      \"sd\": true\n    },\n    \"credentialSubject\": {\n      \"sd\": false,\n      \"children\": {\n        \"fields\": {\n          \"degree\": {\n            \"sd\": false,\n            \"children\": {\n              \"fields\": {\n                \"name\": {\n                  \"sd\": true\n                }\n              }\n            }\n          }\n        }\n      }\n    }\n  }\n}\n}"
        //println(body2)
        val request = Request.Builder()
            .url("http://localhost:7002/openid4vc/mdoc/issue")
            .post(drivingLicenseCredentailRequest.toRequestBody())
            .addHeader("accept", "text/plain")
            .addHeader("Content-Type", "application/json")
            .build()
        var response = client.newCall(request).execute()

        println("RESPONSE: " + response)
        val response_body = response.body?.string()
        println(response_body)

        println(response_body?.split("?")?.get(1)?.split("=")?.get(1))

        println(URLDecoder.decode(response_body?.split("?")?.get(1)?.split("=")?.get(1),"UTF-8"))

        val credential_offer_uri = URLDecoder.decode(response_body?.split("?")?.get(1)?.split("=")?.get(1),"UTF-8")

        println("OFFER URI: " + credential_offer_uri)



        //return "Drivers " + credential_offer_uri
        return StringResponse(credential_offer_uri)
    }
}