import net.kyori.indra.repository.sonatypeSnapshots

plugins {
  kotlin("jvm") version "1.5.30"
  id("net.kyori.indra.license-header")
  id("net.kyori.indra.git")
  id("xyz.jpenilla.run-paper")
  id("org.jlleitschuh.gradle.ktlint")
  id("com.github.johnrengelman.shadow")
  id("net.minecrell.plugin-yml.bukkit")
}

group = "xyz.jpenilla"
version = "1.3.0-SNAPSHOT"
  .run { if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this }
description = "Announcement plugin with support for permissions. Supports Hex colors and clickable messages/hover text using MiniMessage."

repositories {
  // mavenLocal()
  mavenCentral()
  sonatypeSnapshots()
  maven("https://papermc.io/repo/repository/maven-public/")
  maven("https://repo.spongepowered.org/repository/maven-public/")
  maven("https://repo.incendo.org/content/repositories/snapshots/")
  maven("https://repo.jpenilla.xyz/snapshots")
  maven("https://ci.ender.zone/plugin/repository/everything/")
  maven("https://repo.codemc.org/repository/maven-public")
  maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
    content { includeGroup("me.clip") }
  }
  maven("https://jitpack.io") {
    content { includeGroupByRegex("com\\.github\\..*") }
  }
}

configurations.all {
  resolutionStrategy {
    force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.30")
  }
}

dependencies {
  compileOnly("com.destroystokyo.paper", "paper-api", "1.13.2-R0.1-SNAPSHOT")
  compileOnly("com.github.MilkBowl", "VaultAPI", "1.7")
  compileOnly("net.ess3", "EssentialsX", "2.18.2")
  compileOnly("me.clip", "placeholderapi", "2.10.9")

  platform(implementation("net.kyori", "adventure-bom", "4.9.1"))
  implementation("net.kyori", "adventure-extra-kotlin")
  implementation("net.kyori", "adventure-serializer-configurate4")

  val cloudVersion = "1.5.0"
  implementation("cloud.commandframework", "cloud-paper", cloudVersion)
  implementation("cloud.commandframework", "cloud-kotlin-extensions", cloudVersion)
  implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)

  platform(implementation("org.spongepowered", "configurate-bom", "4.1.2"))
  implementation("org.spongepowered", "configurate-hocon")
  implementation("org.spongepowered", "configurate-extra-kotlin")

  implementation("org.koin", "koin-core", "2.1.6")
  implementation("xyz.jpenilla", "jmplib", "1.0.1+44-SNAPSHOT")
  implementation("org.bstats", "bstats-bukkit", "2.2.1")
  implementation("io.papermc", "paperlib", "1.0.6")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  shadowJar {
    from(rootProject.file("license.txt")) {
      rename { "license_${rootProject.name.toLowerCase()}.txt" }
    }

    minimize()
    archiveClassifier.set(null as String?)
    archiveFileName.set("${project.name}-${project.version}.jar")

    val prefix = "${project.group}.${project.name.toLowerCase()}.lib"
    sequenceOf(
      "com.typesafe.config",
      "io.leangen.geantyref",
      "io.papermc.lib",
      "net.kyori",
      "xyz.jpenilla.jmplib",
      "cloud.commandframework",
      "org.koin",
      "org.spongepowered.configurate",
      "org.bstats",
      "kotlin",
    ).forEach { pkg ->
      relocate(pkg, "$prefix.$pkg")
    }

    dependencies {
      exclude(dependency("org.jetbrains:annotations"))
    }
  }
  build {
    dependsOn(shadowJar)
  }
  runServer {
    minecraftVersion("1.17.1")
  }
  register("format") {
    group = "formatting"
    description = "Formats source code according to project style."
    dependsOn(licenseFormat, ktlintFormat)
  }
}

bukkit {
  main = "xyz.jpenilla.announcerplus.AnnouncerPlus"
  apiVersion = "1.13"
  website = "https://github.com/jpenilla/AnnouncerPlus"
  authors = listOf("jmp")
  depend = listOf("Vault")
  softDepend = listOf("PlaceholderAPI", "Essentials", "ViaVersion")
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7)
  ?: error("Failed to determine git hash.")
