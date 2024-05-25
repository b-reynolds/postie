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

dependencies {
    implementation("io.arrow-kt:arrow-core:1.2.4")

    implementation("org.http4k:http4k-connect-amazon-dynamodb:5.13.0.0")
    implementation("org.http4k:http4k-serverless-lambda:5.13.0.0")
    implementation("org.http4k:http4k-format-moshi") {
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }

    implementation("se.ansman.kotshi:api:2.15.0")

    testImplementation(kotlin("test"))
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("io.strikt:strikt-arrow:0.34.0")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("org.http4k:http4k-connect-amazon-dynamodb-fake:5.13.0.0")

    ksp("se.ansman.kotshi:compiler:2.15.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
