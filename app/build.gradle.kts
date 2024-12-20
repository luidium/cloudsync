/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.11/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {

    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.2"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit test framework.
    testImplementation(libs.junit)

    // This dependency is used by the application.
    implementation(libs.guava)

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.logging)

    // Database
    runtimeOnly(libs.postgresql)

    // Minio
    implementation(libs.minio)
    implementation(libs.okhttp)

    // JSON
    implementation(libs.jackson.module)

    // Utilities
    implementation(libs.commons.io)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    // Define the main class for the application.
    mainClass.set("com.luidium.cloudsync.CloudSyncApplication")
}
