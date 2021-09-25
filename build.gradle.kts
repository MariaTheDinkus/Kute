plugins {
    application
    
    // Java Virtual Machine
    kotlin("jvm") version "1.5.21"
    
    // Shadow
    id("com.github.johnrengelman.shadow") version "7.0.0"
    
    // KotlinX Serialization
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.21"
    
    // Maven Publish
    id("maven-publish")
}

group = "dev.vini2003"
version = "0.1.0"

application {
    mainClass.set("MainKt")
}

repositories {
    // Maven Central
    mavenCentral()

    // Mojang
    maven("https://libraries.minecraft.net")

    // JitPack
    maven("https://jitpack.io")

    // KotlinX
    maven("https://kotlin.bintray.com/kotlinx/")
	
    // Dv8tion
	maven("https://m2.dv8tion.net/releases")
}

dependencies {
    // Kotlin Standard Library
    implementation(
        group = "org.jetbrains.kotlin",
        name = "kotlin-stdlib",
        version = "1.5.30"
    )
    
    shadow(
        group = "org.jetbrains.kotlin",
        name = "kotlin-stdlib",
        version = "1.5.21"
    )

    // KotlinX Serialization JSON
    implementation(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-serialization-json",
        version = "1.3.0-RC"
    )
    
    shadow(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-serialization-json",
        version = "1.3.0-RC"
    )
    
    // KotlinX Coroutines
    implementation(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-jdk8",
        version = "1.5.1"
    )
    
    shadow(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-jdk8",
        version = "1.5.1"
    )
    
    // KotlinX Coroutines Reactor
    implementation(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-reactor",
        version = "1.5.1"
    )
    
    shadow(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-reactor",
        version = "1.5.1"
    )
    
    // KotlinX Coroutines Reactive
    implementation(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-reactive",
        version = "1.5.1"
    )
    
    shadow(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-reactive",
        version = "1.5.1"
    )
    
    
    // KotlinX DateTime
    implementation(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-datetime",
        version = "0.2.1"
    )
    
    shadow(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-datetime",
        version = "0.2.1"
    )
    
    // Ktor Server Core
    implementation(
        group = "io.ktor",
        name = "ktor-server-core",
        version = "1.6.2"
    )
    
    shadow(
        group = "io.ktor",
        name = "ktor-server-core",
        version = "1.6.2"
    )
    
    // Ktor Serialization
    implementation(
        group = "io.ktor",
        name = "ktor-serialization",
        version = "1.6.2"
    )
    
    shadow(
        group = "io.ktor",
        name = "ktor-serialization",
        version = "1.6.2"
    )
    
    // Ktor Server Nettty
    implementation(
        group = "io.ktor",
        name = "ktor-server-netty",
        version = "1.6.2"
    )
    
    shadow(
        group = "io.ktor",
        name = "ktor-server-netty",
        version = "1.6.2"
    )
    
    // Exposed Core
    implementation(
        group = "org.jetbrains.exposed",
        name = "exposed-core",
        version = "0.34.2"
    )
    
    shadow(
        group = "org.jetbrains.exposed",
        name = "exposed-core",
        version = "0.34.2"
    )
    
    // Exposed DAO
    implementation(
        group = "org.jetbrains.exposed",
        name = "exposed-dao",
        version = "0.34.2"
    )
    
    shadow(
        group = "org.jetbrains.exposed",
        name = "exposed-dao",
        version = "0.34.2"
    )
    
    // Exposed JDBC
    implementation(
        group = "org.jetbrains.exposed",
        name = "exposed-jdbc",
        version = "0.34.2"
    )
    
    shadow(
        group = "org.jetbrains.exposed",
        name = "exposed-jdbc",
        version = "0.34.2"
    )
    
    // HikariCP
    implementation(
        group = "com.zaxxer",
        name = "HikariCP",
        version = "5.0.0"
    )
    
    // PostgreSQL JDBC
    implementation(
        group = "org.postgresql",
        name = "postgresql",
        version = "42.2.23"
    )
    
    // H2
    implementation(
        group = "com.h2database",
        name = "h2",
        version = "1.4.200"
    )
    
    // Ktor Client CIO
    implementation(
        group = "io.ktor",
        name = "ktor-client-cio",
        version = "1.6.2"
    )
    
    shadow(
        group = "io.ktor",
        name = "ktor-client-cio",
        version = "1.6.2"
    )
    
    // Ktor Client Serialization
    implementation(
        group = "io.ktor",
        name = "ktor-client-serialization",
        version = "1.6.2"
    )
    
    shadow(
        group = "io.ktor",
        name = "ktor-client-serialization",
        version = "1.6.2"
    )
    
    // Logback Classic
    implementation(
        group = "ch.qos.logback",
        name = "logback-classic",
        version = "1.2.3"
    )
    
    shadow(
        group = "ch.qos.logback",
        name = "logback-classic",
        version = "1.2.3"
    )
    
    // Logback Classic
    implementation(
        group = "ch.qos.logback",
        name = "logback-classic",
        version = "1.2.3"
    )
    
    shadow(
        group = "ch.qos.logback",
        name = "logback-classic",
        version = "1.2.3"
    )
    
    // SLF4J
    implementation(
        group = "org.slf4j",
        name = "slf4j-api",
        version = "1.7.31"
    )
    
    shadow(
        group = "org.slf4j",
        name = "slf4j-api",
        version = "1.7.31"
    )
    
    // DiscordKt
    implementation(
        group = "com.discord4j",
        name = "discord4j-core",
        version = "3.1.7"
    )
    
    shadow(
        group = "com.discord4j",
        name = "discord4j-core",
        version = "3.1.7"
    )
    
    // LavaPlayer
    implementation(
        group = "com.github.Walkyst",
        name = "lavaplayer",
        version = "fix-age-restricted-videos-SNAPSHOT"
    )
    
    shadow(
        group = "com.github.Walkyst",
        name = "lavaplayer",
        version = "fix-age-restricted-videos-SNAPSHOT"
    )
    
    // Brigadier
    implementation(
        group = "com.mojang",
        name = "brigadier",
        version = "1.0.18"
    )
    
    shadow(
        group = "com.mojang",
        name = "brigadier",
        version = "1.0.18"
    )
    
    // Spotify Web API
    implementation(
        group = "se.michaelthelin.spotify",
        name = "spotify-web-api-java",
        version = "6.5.4"
    )
    
    shadow(
        group = "se.michaelthelin.spotify",
        name = "spotify-web-api-java",
        version = "6.5.4"
    )
    
    // Jsoup
    implementation(
        group = "org.jsoup",
        name = "jsoup",
        version = "1.13.1"
    )
    
    shadow(
        group = "org.jsoup",
        name = "jsoup",
        version = "1.13.1"
    )
}

tasks {
    jar {
        manifest {
            attributes(
                "Main-Class" to "MainKt",
                "Multi-Release" to true
            )
        }
    }
    
    shadowJar {
        archiveClassifier.set("")
    }
    
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

publishing {
    publications {
        create<MavenPublication>(name) {
            this.groupId = group as String
            this.artifactId = name
            this.version = version
            
            from(components["java"])
            
            val sourcesJar by tasks.creating(Jar::class) {
                val sourceSets: SourceSetContainer by project
                
                from(sourceSets["main"].allJava)
                classifier = "sources"
            }
            val javadocJar by tasks.creating(Jar::class) {
                from(tasks.get("javadoc"))
                classifier = "javadoc"
            }
            
            artifact(sourcesJar)
            artifact(javadocJar)
            
        }
    }
    
    repositories {
        repositories {
            maven {
                name = "GitHubPackages"
                setUrl("https://maven.pkg.github.com/innitGG/maven")
                
                credentials {
                    username = System.getenv("GPR_USERNAME")
                    password = System.getenv("GPR_TOKEN")
                }
            }
        }
    }
}