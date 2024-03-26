docker build -t waltid/issuer-api -f waltid-issuer-api/Dockerfile .
docker run -p 7002:7002 waltid/issuer-api --webHost=0.0.0.0 --webPort=7002 --baseUrl=http://localhost:7002

### create key and "DID"
curl -X 'POST' \
  'http://localhost:7002/onboard/issuer' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "issuanceKeyConfig": {
    "type": "local",
    "algorithm": "Ed25519"
  },
  "issuerDidConfig": {
    "method": "jwk"
  }
}'
# response 
{
    "issuanceKey": {
        "type": "local",
        "jwk": {
            "kty": "OKP",
            "d": "jQGuwJkF6umOyX4dSv_bRVozjKRc2NvPg1JSt39PaHo",
            "crv": "Ed25519",
            "kid": "ByUineP2PKbozXLH8nJiYrXUIxX5CjvinipFBBBCOi4",
            "x": "6zAA6R5vwH-WnAFh8ZSEIrF7sqG7T8BiuiQQhxvB4OQ"
        }
    },
    "issuerDid": "did:jwk:eyJrdHkiOiJPS1AiLCJjcnYiOiJFZDI1NTE5Iiwia2lkIjoiQnlVaW5lUDJQS2JvelhMSDhuSmlZclhVSXhYNUNqdmluaXBGQkJCQ09pNCIsIngiOiI2ekFBNlI1dndILVduQUZoOFpTRUlyRjdzcUc3VDhCaXVpUVFoeHZCNE9RIn0"
}


### issue credential with the previosly created key
curl -X 'POST' \
  'http://localhost:7002/openid4vc/sdjwt/issue' \
  -H 'accept: text/plain' \
  -H 'Content-Type: application/json' \
  -d '{
  "issuanceKey": {
    "type": "jwk",
    "jwk": "{\"kty\":\"OKP\",\"d\":\"jQGuwJkF6umOyX4dSv_bRVozjKRc2NvPg1JSt39PaHo\",\"crv\":\"Ed25519\",\"kid\":\"ByUineP2PKbozXLH8nJiYrXUIxX5CjvinipFBBBCOi4\",\"x\":\"6zAA6R5vwH-WnAFh8ZSEIrF7sqG7T8BiuiQQhxvB4OQ\"}"
  },
  "issuerDid": "did:jwk:eyJrdHkiOiJPS1AiLCJjcnYiOiJFZDI1NTE5Iiwia2lkIjoiQnlVaW5lUDJQS2JvelhMSDhuSmlZclhVSXhYNUNqdmluaXBGQkJCQ09pNCIsIngiOiI2ekFBNlI1dndILVduQUZoOFpTRUlyRjdzcUc3VDhCaXVpUVFoeHZCNE9RIn0",
  "vc": {
    "@context": [
      "https://www.w3.org/2018/credentials/v1",
      "https://www.w3.org/2018/credentials/examples/v1"
    ],
    "id": "http://example.gov/credentials/3732",
    "type": [
      "VerifiableCredential",
      "UniversityDegree"
    ],
    "issuer": {
      "id": "did:web:vc.transmute.world"
    },
    "issuanceDate": "2024-03-27T00:14:12.164Z",
    "credentialSubject": {
      "id": "did:example:ebfeb1f712ebc6f1c276e12ec21",
      "degree": {
        "type": "BachelorDegree",
        "name": "Bachelor of Science and Arts"
      }
    }
  },
  "mapping": {
    "id": "<uuid>",
    "issuer": {
      "id": "<issuerDid>"
    },
    "credentialSubject": {
      "id": "<subjectDid>"
    },
    "issuanceDate": "<timestamp>",
    "expirationDate": "<timestamp-in:365d>"
  },
  "selectiveDisclosure": {
  "fields": {
    "issuanceDate": {
      "sd": true
    },
    "credentialSubject": {
      "sd": false,
      "children": {
        "fields": {
          "degree": {
            "sd": false,
            "children": {
              "fields": {
                "name": {
                  "sd": true
                }
              }
            }
          }
        }
      }
    }
  }
}
}'

# response 
openid-credential-offer://localhost:7002/?credential_offer_uri=http%3A%2F%2Flocalhost%3A7002%2Fopenid4vc%2FcredentialOffer%3Fid%3Dc2a5ae3e-363e-4e0e-a890-b2a1a74af05d% 


### url decode 
python3 -c "import sys, urllib.parse as ul; \
    print(ul.unquote_plus('openid-credential-offer://localhost:7002/?credential_offer_uri=http%3A%2F%2Flocalhost%3A7002%2Fopenid4vc%2FcredentialOffer%3Fid%3Dc2a5ae3e-363e-4e0e-a890-b2a1a74af05d% '))"

#result 
openid-credential-offer://localhost:7002/?credential_offer_uri=http://localhost:7002/openid4vc/credentialOffer?id=c2a5ae3e-363e-4e0e-a890-b2a1a74af05d


### fetch credenti offer uri
curl 'http://localhost:7002/openid4vc/credentialOffer?id=c2a5ae3e-363e-4e0e-a890-b2a1a74af05d'

# response 

{
    "credential_issuer": "http://localhost:7002",
    "credentials": [
        {
            "format": "jwt_vc_json",
            "types": [
                "VerifiableCredential",
                "UniversityDegree"
            ],
            "credential_definition": {
                "@context": [
                    "https://www.w3.org/2018/credentials/v1",
                    "https://www.w3.org/2018/credentials/examples/v1"
                ],
                "types": [
                    "VerifiableCredential",
                    "UniversityDegree"
                ]
            }
        }
    ],
    "grants": {
        "authorization_code": {
            "issuer_state": "c2a5ae3e-363e-4e0e-a890-b2a1a74af05d"
        },
        "urn:ietf:params:oauth:grant-type:pre-authorized_code": {
            "pre-authorized_code": "eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiJjMmE1YWUzZS0zNjNlLTRlMGUtYTg5MC1iMmExYTc0YWYwNWQiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjcwMDIiLCJhdWQiOiJUT0tFTiJ9.zrGBu2kLfI0K2wNuWopvTyq97Z-dunwoIHyd8xGLrOZFy46TFa5AkF8nMHFlH_ykIk-rkYYucDJ717cIow-pAA",
            "user_pin_required": false
        }
    }
}










