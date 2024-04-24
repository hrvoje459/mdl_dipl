package fer.dipl.mdl.issuer_app


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication()
class IssuerAppApplication

suspend fun main(args: Array<String>) {
	runApplication<IssuerAppApplication>(*args)
}
