plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"

}

group = "fer.dipl.mdl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.walt.id/repository/waltid/") }


}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("id.walt.did:waltid-did:1.0.2403291506-SNAPSHOT")
    implementation("id.walt.crypto:waltid-crypto:1.0.2403291506-SNAPSHOT")
    implementation("id.walt.credentials:waltid-verifiable-credentials:1.0.2403291506-SNAPSHOT")
    implementation("id.walt:waltid-sdjwt-jvm:1.0.2403291506-SNAPSHOT")
    implementation("id.walt:waltid-sdjwt-jvm:1.0.2403291506-SNAPSHOT")
    implementation("id.walt:waltid-mdoc-credentials-jvm:1.0.2403291506-SNAPSHOT")

    implementation("com.augustcellars.cose:cose-java:1.1.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.16.0")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.bouncycastle:bcprov-lts8on:2.73.4")

    implementation("org.bouncycastle:bcpkix-lts8on:2.73.4")


    implementation("com.squareup.okhttp3:okhttp:4.12.0")


    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")


    // JOSE
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")




}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}