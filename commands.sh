colima start --network-address

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
openid-credential-offer://localhost:7002/?credential_offer_uri=http%3A%2F%2Flocalhost%3A7002%2Fopenid4vc%2FcredentialOffer%3Fid%3D2b19f1a6-a689-411f-8508-7c3b81dffea5
openid-credential-offer://localhost:7002/?credential_offer_uri=http%3A%2F%2Flocalhost%3A7002%2Fopenid4vc%2FcredentialOffer%3Fid%3D01dc5af9-fede-4f89-a613-6fef8b1d5e02
### url decode 
python3 -c "import sys, urllib.parse as ul; \
    print(ul.unquote_plus('openid-credential-offer://localhost:7002/?credential_offer_uri=http%3A%2F%2Flocalhost%3A7002%2Fopenid4vc%2FcredentialOffer%3Fid%3Dc2a5ae3e-363e-4e0e-a890-b2a1a74af05d% '))"

python3 -c "import sys, urllib.parse as ul; \
    print(ul.unquote_plus('openid-credential-offer://localhost:7002/?credential_offer_uri=http%3A%2F%2Flocalhost%3A7002%2Fopenid4vc%2FcredentialOffer%3Fid%3D01dc5af9-fede-4f89-a613-6fef8b1d5e02'))"


#result 
openid-credential-offer://localhost:7002/?credential_offer_uri=http://localhost:7002/openid4vc/credentialOffer?id=c2a5ae3e-363e-4e0e-a890-b2a1a74af05d

openid-credential-offer://localhost:7002/?credential_offer_uri=http://localhost:7002/openid4vc/credentialOffer?id=2b19f1a6-a689-411f-8508-7c3b81dffea5
openid-credential-offer://localhost:7002/?credential_offer_uri=http://localhost:7002/openid4vc/credentialOffer?id=01dc5af9-fede-4f89-a613-6fef8b1d5e02

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
            "issuer_state": "01dc5af9-fede-4f89-a613-6fef8b1d5e02"
        },
        "urn:ietf:params:oauth:grant-type:pre-authorized_code": {
            "pre-authorized_code": "eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiIwMWRjNWFmOS1mZWRlLTRmODktYTYxMy02ZmVmOGIxZDVlMDIiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjcwMDIiLCJhdWQiOiJUT0tFTiJ9.u2DlW-dKHkJTx5OvnJTR6EuT4Ee3xFvI1mqyjNcFgBP48XnP6vLCSFPRq-B7OVW-5Ly7bkzhthJSq4sCKrQTAA",
            "user_pin_required": false
        }
    }
}


### after server restart if we try to request same credential offer we get following error
# No active issuance session found by the given id



### get token
POST /token HTTP/1.1
Host: localhost:7002
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code
&pre-authorized_code=eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiJjMmE1YWUzZS0zNjNlLTRlMGUtYTg5MC1iMmExYTc0YWYwNWQiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjcwMDIiLCJhdWQiOiJUT0tFTiJ9.zrGBu2kLfI0K2wNuWopvTyq97Z-dunwoIHyd8xGLrOZFy46TFa5AkF8nMHFlH_ykIk-rkYYucDJ717cIow-pAA
&tx_code=493536


curl --location 'localhost:7002/token' \
--data-urlencode 'grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code' \
--data-urlencode 'pre-authorized_code=eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiIwMWRjNWFmOS1mZWRlLTRmODktYTYxMy02ZmVmOGIxZDVlMDIiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjcwMDIiLCJhdWQiOiJUT0tFTiJ9.u2DlW-dKHkJTx5OvnJTR6EuT4Ee3xFvI1mqyjNcFgBP48XnP6vLCSFPRq-B7OVW-5Ly7bkzhthJSq4sCKrQTAA'

#response 
{
    "access_token": "eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiIwMWRjNWFmOS1mZWRlLTRmODktYTYxMy02ZmVmOGIxZDVlMDIiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjcwMDIiLCJhdWQiOiJBQ0NFU1MifQ.-oeNqx38ubj3oITU78BSnr4z2NpH0Jsndw6lFdfaz7w7dP0Osvp0CyIlRLAJ9nySCoE4xtLzzNq0-PQ5O33BCA",
    "token_type": "bearer",
    "c_nonce": "b35d48b9-b10e-406d-bac0-74311eafddb0",
    "c_nonce_expires_in": 233
}


### now we should exchange access token for the credential
### proof of possesion is required, embed nonce and sign jwt
### example request (https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-request):


POST /credential HTTP/1.1
Host: server.example.com
Content-Type: application/json
Authorization: BEARER czZCaGRSa3F0MzpnWDFmQmF0M2JW

{
   "credential_identifier": "CivilEngineeringDegree-2023",
   "proof": {
      "proof_type": "jwt",
      "jwt":
      "eyJ0eXAiOiJvcGVuaWQ0dmNpLXByb29mK2p3dCIsImFsZyI6IkVTMjU2IiwiandrI
      jp7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoiblVXQW9BdjNYWml0aDhFN2k
      xOU9kYXhPTFlGT3dNLVoyRXVNMDJUaXJUNCIsInkiOiJIc2tIVThCalVpMVU5WHFpN
      1N3bWo4Z3dBS18weGtjRGpFV183MVNvc0VZIn19.eyJhdWQiOiJodHRwczovL2NyZW
      RlbnRpYWwtaXNzdWVyLmV4YW1wbGUuY29tIiwiaWF0IjoxNzAxOTYwNDQ0LCJub25j
      ZSI6IkxhclJHU2JtVVBZdFJZTzZCUTR5bjgifQ.-a3EDsxClUB4O3LeDD5DVGEnNMT
      01FCQW4P6-2-BNBqc_Zxf0Qw4CWayLEpqkAomlkLb9zioZoipdP-jvh1WlA"
   }
}
