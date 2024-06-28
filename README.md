# mdl_dipl
Master thesis repositroy



<!-- ABOUT  -->
## About

This repository contains source code, instructions and documentation related to my master thesis "Mobile driving license based on the EUDI framework".

There is an additional component which is in a separate repository because its a fork of a opensource project used for verifiable credentials issuance via OID4VCI protocol which we extended to support issuance of mobile driving licenses (mDLs), source code can be found <a href="https://github.com/hrvoje459/waltid-identity">here</a>

There are multiple folders in this repository each representing a component of the overall system.
  - <a href="./mdl_holder_app/">mdl_holder_app</a> is an Android application representing digital wallet solution for working with digital driving licenses
  - <a href="./mdl_verifier_app/">mdl_verifier_app</a> is an Android application that would be used by the policemen during the traffic stop to verify the authenticity of digital driving license
  - <a href="./mdl_invalid_app/">mdl_invalid_app</a> is an Android application used for generating invalid digital driving license to demonstrate verifiers ability to differentiate between valid and invalid digital driving licenses
  - <a href="./issuer_app/">issuer_app</a> is an Spring/Kotlin backend application that acts as a middleware between end users digital wallets and mDL Issuer, it handles PKI infrastructure creation and end user authentication/authorization for driving license issuance
  - <a href="./kotlin_testing/">kotlin_testing</a> contains Kotlin code snippets used for testing purposes


In the root of this repository you can also find a Docker Compose file used for deploying issuance infrastructure:
  - first deploy PostgreSQL and Keycloak, modify the PostgreSQL database and configure Keycloak as described in issuer_app README file
  - then you will be able to populate required values in the remaining components

