package fer.dipl.mdl.issuer_app

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.stereotype.Component

@ConfigurationProperties("issuer")
data class MyProperties(
    var country_code: String = ""
){
    init {
        println("TESTING PROP 1: $country_code")
    }
}

@ConfigurationProperties(prefix = "credential.issuer")
data class CredentialIssuer(
    var base_url: String = "",
){
    init {
        println("TESTING PROP 1: $base_url")
    }
}