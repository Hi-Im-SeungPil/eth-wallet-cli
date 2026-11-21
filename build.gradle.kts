plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "dev.study"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // secp256k1 곡선 연산과 Keccak-256 해시
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}
