import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"
}

group = "fer.dipl.mdl"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
	maven { url = uri("https://maven.walt.id/repository/waltid/") }
}



dependencies {
	//implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	//testImplementation("org.springframework.security:spring-security-test")

	// OAUTH
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.security:spring-security-web")
	implementation("org.springframework.security:spring-security-config")
	implementation("org.springframework.boot:spring-boot-starter-web-services")
	implementation("org.springframework.security:spring-security-core")


	// JOSE
	implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
	// WALT ID
	implementation("id.walt.crypto:waltid-crypto:1.0.2403291506-SNAPSHOT")
	implementation("id.walt.did:waltid-did:1.0.2403291506-SNAPSHOT")
	// BOUNCY CASTLE CRYPTO
	implementation("org.bouncycastle:bcprov-lts8on:2.73.4")
	implementation("org.bouncycastle:bcpkix-lts8on:2.73.4")
	// HTTP
	implementation("com.squareup.okhttp3:okhttp:4.12.0")

	// COROUTINES - necessary for having suspend functions in REST Mappers
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.0")

	implementation("org.springframework.boot:spring-boot-starter-log4j2")
	modules {
		module("org.springframework.boot:spring-boot-starter-logging") {
			replacedBy("org.springframework.boot:spring-boot-starter-log4j2", "Use Log4j2 instead of Logback")
		}
	}

}



tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
