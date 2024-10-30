/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    id("io.github.goooler.shadow") version "8.1.8"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly(libs.io.papermc.paper.paper.api)
    compileOnly("io.lumine:Mythic-Dist:5.7.2")
    implementation("com.zaxxer:HikariCP:6.0.0")
    implementation("com.github.Be4rJP:ArtGUI:v1.0.4") {
        exclude("org.jetbrains", "annotations")
    }
}

group = "net.azisaba"
version = "1.0.0"
description = "NPCShop"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks {
    withType<JavaCompile> { options.encoding = "UTF-8" }
    withType<Javadoc> { options.encoding = "UTF-8" }
    base.archivesName.set("NPCShop")

    shadowJar {
        relocate("org.jetbrains", "net.azisaba.mmocore.lib.org.jetbrains")
        relocate("com.zaxxer.hikari", "net.azisaba.mmocore.lib.com.zaxxer")
        relocate("com.github.Be4rJP", "net.azisaba.mmocore.lib.com.github.Be4rJP")
    }
}
