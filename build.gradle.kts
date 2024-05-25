import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.benreynolds"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val kotlinVersion = "2.0.0"
val kspVersion = "2.0.0-1.0.21"
val shadowVersion = "8.1.1"
val arrowCoreVersion = "1.2.4"
val http4kVersion = "5.13.0.0"
val kotshiVersion = "2.15.0"
val striktVersion = "0.34.0"
val mockkVersion = "1.13.11"

dependencies {
    implementation("io.arrow-kt:arrow-core:$arrowCoreVersion")

    implementation("org.http4k:http4k-connect-amazon-dynamodb:$http4kVersion")
    implementation("org.http4k:http4k-serverless-lambda:$http4kVersion")
    implementation("org.http4k:http4k-format-moshi:$http4kVersion") {
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }

    implementation("se.ansman.kotshi:api:$kotshiVersion")

    testImplementation(kotlin("test", kotlinVersion))
    testImplementation("io.strikt:strikt-core:$striktVersion")
    testImplementation("io.strikt:strikt-arrow:$striktVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.http4k:http4k-connect-amazon-dynamodb-fake:$http4kVersion")

    ksp("se.ansman.kotshi:compiler:$kotshiVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveFileName.set("postie.jar")
    minimize()
}

kotlin {
    jvmToolchain(17)
}
