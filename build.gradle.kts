import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "2.0.4"
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
}

group = ""
version = "1.0"

repositories {
    jcenter()
    mavenCentral()
    maven {
        name = "sonatype-oss"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "aikar"
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        name = "velocity"
        url = uri("https://nexus.velocitypowered.com/repository/maven-public/")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(group = "com.uchuhimo", name = "konf", version = "0.22.1")
    implementation(group = "net.luckperms", name = "api", version = "5.1")
    implementation(group = "net.kyori", name = "adventure-api", version = "4.9.2")
    implementation(group = "net.kyori", name = "adventure-text-serializer-legacy", version = "4.9.2")
    implementation(group = "net.kyori", name = "adventure-text-serializer-plain", version = "4.9.2")
    implementation(group = "co.aikar", name = "acf-velocity", version = "0.5.1-SNAPSHOT")
    implementation(group = "org.javacord", name = "javacord", version = "3.3.2")
    implementation(group = "com.velocitypowered", name = "velocity-api", version = "3.0.1")
    annotationProcessor(group = "com.velocitypowered", name = "velocity-api", version = "3.0.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.javaParameters = true
}

tasks.shadowJar {
    relocate("co.aikar.commands", "chattore.acf")
    relocate("co.aikar.locales", "chattore.locales")
    dependencies {
        exclude(
            dependency(
                "net.luckperms:api:.*"
            )
        )
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}