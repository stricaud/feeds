plugins {
    jacoco
    application
    kotlin("jvm") version Versions.kotlinVersion
    kotlin("plugin.serialization") version Versions.kotlinVersion
    id("org.jetbrains.dokka") version Versions.kotlinVersion
    id("io.gitlab.arturbosch.detekt") version "1.14.2"
    id("io.wusa.semver-git-plugin") version "2.3.0"
    id("com.adarshr.test-logger") version "2.1.1"
}

group = "com.devo"
version = semver.info

val defaultVersionFormatter = Transformer<Any, io.wusa.Info> { info ->
    "${info.version.major}.${info.version.minor}.${info.version.patch}+build.${info.count}.sha.${info.shortCommit}"
}

semver {
    initialVersion = "0.0.0"
    branches {
        branch {
            regex = "master"
            incrementer = "MINOR_VERSION_INCREMENTER"
            formatter = Transformer<Any, io.wusa.Info> { info ->
                "${info.version.major}.${info.version.minor}.${info.version.patch}"
            }
        }
        branch {
            regex = "develop"
            incrementer = "PATCH_VERSION_INCREMENTER"
            formatter = defaultVersionFormatter
        }
        branch {
            regex = ".+"
            incrementer = "NO_VERSION_INCREMENTER"
            formatter = defaultVersionFormatter
        }
    }
}

val javaVersion = JavaVersion.VERSION_1_8.toString()

tasks.compileKotlin {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    kotlinOptions {
        jvmTarget = javaVersion
    }
}

tasks.compileTestKotlin {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    kotlinOptions {
        jvmTarget = javaVersion
    }
}

tasks.detekt {
    jvmTarget = javaVersion
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

application {
    mainClass.set("com.devo.feeds.FeedsServiceKt")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    // Logging dependencies
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.13.1")
    implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
    implementation("org.slf4j:log4j-over-slf4j:${Versions.slf4j}")
    implementation("org.slf4j:jcl-over-slf4j:${Versions.slf4j}")
    implementation("org.slf4j:jul-to-slf4j:${Versions.slf4j}")
    implementation("io.github.microutils:kotlin-logging:1.8.3")

    implementation("com.github.ajalt.clikt:clikt:3.0.1")
    implementation("com.cloudbees:syslog-java-client:1.1.7")
    implementation("io.github.config4k:config4k:0.4.2")

    implementation("io.ktor:ktor-client-core:${Versions.ktor}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktor}")

    implementation("org.mapdb:mapdb:3.0.8")
    implementation("com.fasterxml.uuid:java-uuid-generator:4.0.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    testImplementation("com.natpryce:hamkrest:1.7.0.3")
    testImplementation("io.mockk:mockk:1.10.2")
    testImplementation("io.ktor:ktor-server-core:${Versions.ktor}")
    testImplementation("io.ktor:ktor-server-netty:${Versions.ktor}")
    testImplementation("org.awaitility:awaitility:4.0.2")
}
