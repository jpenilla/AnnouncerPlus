import net.kyori.indra.repository.sonatypeSnapshots

plugins {
  kotlin("jvm") version "1.5.0"
  id("com.github.johnrengelman.shadow") version "7.0.0"
  id("net.minecrell.plugin-yml.bukkit") version "0.4.0"
  val indraVersion = "2.0.4"
  id("net.kyori.indra.license-header") version indraVersion
  id("net.kyori.indra.git") version indraVersion
}

group = "xyz.jpenilla"
version = "1.2.5-SNAPSHOT+${getLastCommitHash()}"
description = "Announcement plugin with support for permissions. Supports Hex colors and clickable messages/hover text using MiniMessage."

repositories {
  //mavenLocal()
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

dependencies {
  compileOnly("com.destroystokyo.paper", "paper-api", "1.13.2-R0.1-SNAPSHOT")
  compileOnly("com.github.MilkBowl", "VaultAPI", "1.7")
  compileOnly("net.ess3", "EssentialsX", "2.18.2")
  compileOnly("me.clip", "placeholderapi", "2.10.9")

  platform(implementation("net.kyori", "adventure-bom", "4.7.0"))
  implementation("net.kyori", "adventure-extra-kotlin", "4.7.0")

  val cloudVersion = "1.5.0-SNAPSHOT"
  implementation("cloud.commandframework", "cloud-paper", cloudVersion)
  implementation("cloud.commandframework", "cloud-kotlin-extensions", cloudVersion)
  implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)

  implementation("org.spongepowered", "configurate-hocon", "4.1.1")
  implementation("org.koin", "koin-core", "2.1.6")
  implementation("xyz.jpenilla", "jmplib", "1.0.1+36-SNAPSHOT")
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
    archiveClassifier.set("")
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
}

bukkit {
  main = "xyz.jpenilla.announcerplus.AnnouncerPlus"
  apiVersion = "1.13"
  website = "https://github.com/jpenilla/AnnouncerPlus"
  authors = listOf("jmp")
  depend = listOf("Vault")
  softDepend = listOf("PlaceholderAPI", "Essentials", "ViaVersion")
}

fun getLastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7)
  ?: error("Failed to determine git hash.")
