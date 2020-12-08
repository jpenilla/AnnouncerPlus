import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import org.apache.commons.io.output.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.4.20"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("kr.entree.spigradle") version "2.2.3"
}

val projectName = "AnnouncerPlus"
group = "xyz.jpenilla"
version = "1.2.2.1+${getLastCommitHash()}-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://repo.spongepowered.org/maven")
    maven(url = "https://repo.jpenilla.xyz/snapshots")
    maven(url = "https://ci.ender.zone/plugin/repository/everything/")
    maven(url = "https://repo.codemc.org/repository/maven-public")
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc", "spigot-api", "1.13.2-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7")
    compileOnly("net.ess3", "EssentialsX", "2.17.2")

    implementation("org.koin", "koin-core", "2.1.6")
    implementation("xyz.jpenilla", "jmplib", "1.0.1+26-SNAPSHOT")
    implementation("com.okkero", "skedule", "1.2.7-SNAPSHOT")
    implementation("org.spongepowered", "configurate-hocon", "4.0.0")
    implementation("org.bstats", "bstats-bukkit", "1.7")

    val cloudVersion = "1.2.0"
    implementation("cloud.commandframework", "cloud-paper", cloudVersion)
    implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)
}

spigot {
    name = projectName
    apiVersion = "1.13"
    description = "Announcement plugin with support for permissions. Supports Hex colors and clickable messages/hover text using MiniMessage."
    website = "https://github.com/jmanpenilla/AnnouncerPlus"
    authors("jmp")
    depends("Vault")
    softDepends("PlaceholderAPI", "Prisma", "Essentials")
}

val autoRelocate by tasks.register("configureShadowRelocation", ConfigureShadowRelocation::class) {
    target = tasks.shadowJar.get()
    val packageName = "${project.group}.${project.name.toLowerCase()}"
    prefix = "$packageName.shaded"
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    shadowJar {
        minimize()
        dependsOn(autoRelocate)
        archiveClassifier.set("")
        archiveFileName.set("$projectName-${project.version}.jar")
    }
}

fun getLastCommitHash(): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "rev-parse", "--short", "HEAD")
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8).trim()
}