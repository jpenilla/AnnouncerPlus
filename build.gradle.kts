import org.apache.commons.io.output.ByteArrayOutputStream

plugins {
  kotlin("jvm") version "1.4.32"
  id("com.github.johnrengelman.shadow") version "7.0.0"
  id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
  id("net.kyori.indra.license-header") version "1.3.1"
}

group = "xyz.jpenilla"
version = "1.2.5+${getLastCommitHash()}-SNAPSHOT"
description = "Announcement plugin with support for permissions. Supports Hex colors and clickable messages/hover text using MiniMessage."

repositories {
  //mavenLocal()
  mavenCentral()
  maven("https://papermc.io/repo/repository/maven-public/")
  maven("https://oss.sonatype.org/content/groups/public/")
  maven("https://repo.spongepowered.org/maven")
  maven("https://repo.jpenilla.xyz/snapshots")
  maven("https://ci.ender.zone/plugin/repository/everything/")
  maven("https://repo.codemc.org/repository/maven-public")
  maven("https://jitpack.io") {
    content { includeGroupByRegex("com\\.github\\..*") }
  }
}

dependencies {
  compileOnly("com.destroystokyo.paper", "paper-api", "1.13.2-R0.1-SNAPSHOT")
  compileOnly("com.github.MilkBowl", "VaultAPI", "1.7")
  compileOnly("net.ess3", "EssentialsX", "2.17.2")

  platform(implementation("net.kyori", "adventure-bom", "4.7.0"))
  implementation("net.kyori", "adventure-extra-kotlin", "4.7.0")

  val cloudVersion = "1.4.0"
  implementation("cloud.commandframework", "cloud-paper", cloudVersion)
  implementation("cloud.commandframework", "cloud-kotlin-extensions", cloudVersion)
  implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)

  implementation("org.spongepowered", "configurate-hocon", "4.1.0-SNAPSHOT")
  implementation("org.koin", "koin-core", "2.1.6")
  implementation("xyz.jpenilla", "jmplib", "1.0.1+33-SNAPSHOT")
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

    exclude("org/jetbrains/annotations/*")
    exclude("org/intellij/lang/annotations/*")
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

fun getLastCommitHash(): String = ByteArrayOutputStream().apply {
  exec {
    commandLine = listOf("git", "rev-parse", "--short", "HEAD")
    standardOutput = this@apply
  }
}.toString(Charsets.UTF_8).trim()
