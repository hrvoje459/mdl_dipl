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