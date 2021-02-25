import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import org.apache.commons.io.output.ByteArrayOutputStream

plugins {
  kotlin("jvm") version "1.4.30"
  id("com.github.johnrengelman.shadow") version "6.1.0"
  id("kr.entree.spigradle") version "2.2.3"
  id("net.kyori.indra.license-header") version "1.3.1"
}

group = "xyz.jpenilla"
version = "1.2.3.1+${getLastCommitHash()}-SNAPSHOT"
description = "Announcement plugin with support for permissions. Supports Hex colors and clickable messages/hover text using MiniMessage."

repositories {
  mavenLocal()
  mavenCentral()
  jcenter()
  maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
  maven("https://oss.sonatype.org/content/groups/public/")
  maven("https://repo.spongepowered.org/maven")
  maven("https://repo.jpenilla.xyz/snapshots")
  maven("https://ci.ender.zone/plugin/repository/everything/")
  maven("https://repo.codemc.org/repository/maven-public")
  maven("https://jitpack.io")
}

dependencies {
  compileOnly("org.spigotmc", "spigot-api", "1.13.2-R0.1-SNAPSHOT")
  compileOnly("com.github.MilkBowl", "VaultAPI", "1.7")
  compileOnly("net.ess3", "EssentialsX", "2.17.2")

  implementation("org.koin", "koin-core", "2.1.6")
  implementation("net.kyori", "adventure-api", "4.5.1")
  implementation("xyz.jpenilla", "jmplib", "1.0.1+30-SNAPSHOT")
  implementation("org.spongepowered", "configurate-hocon", "4.1.0-SNAPSHOT")
  implementation("org.bstats", "bstats-bukkit", "2.2.1")

  val cloudVersion = "1.4.0"
  implementation("cloud.commandframework", "cloud-paper", cloudVersion)
  implementation("cloud.commandframework", "cloud-kotlin-extensions", cloudVersion)
  implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  register<ConfigureShadowRelocation>("autoRelocate") {
    target = shadowJar.get()
    val packageName = "${project.group}.${project.name.toLowerCase()}"
    prefix = "$packageName.lib"
  }
  shadowJar {
    dependsOn(withType<ConfigureShadowRelocation>())
    minimize()
    archiveClassifier.set("")
    archiveFileName.set("${project.name}-${project.version}.jar")
  }
  build {
    dependsOn(shadowJar)
  }
}

spigot {
  apiVersion = "1.13"
  website = "https://github.com/jmanpenilla/AnnouncerPlus"
  authors("jmp")
  depends("Vault")
  softDepends("PlaceholderAPI", "Prisma", "Essentials")
}

fun getLastCommitHash(): String = ByteArrayOutputStream().apply {
  exec {
    commandLine = listOf("git", "rev-parse", "--short", "HEAD")
    standardOutput = this@apply
  }
}.toString(Charsets.UTF_8).trim()
