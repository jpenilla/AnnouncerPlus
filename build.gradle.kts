import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import org.apache.commons.io.output.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("kr.entree.spigradle") version "2.1.1"
}

configurations.all {
    exclude(group = "org.checkerframework")
}

val projectName = "AnnouncerPlus"
group = "xyz.jpenilla"
version = "1.1.3+${getLastCommitHash()}-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://repo.spongepowered.org/maven")
    maven(url = "https://repo.aikar.co/content/groups/aikar/")
    maven(url = "https://nexus.okkero.com/repository/maven-releases/")
    maven(url = "https://repo.jpenilla.xyz/snapshots")
    maven(url = "https://ci.ender.zone/plugin/repository/everything/")
    maven(url = "https://repo.codemc.org/repository/maven-public")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("xyz.jpenilla", "jmplib", "1.0.0+99-SNAPSHOT")
    implementation("co.aikar", "acf-paper", "0.5.0-SNAPSHOT")
    implementation("com.github.jmanpenilla", "Skedule", "7ae098d404")
    implementation("com.okkero.skedule", "skedule", "1.2.6")
    implementation("org.bstats", "bstats-bukkit", "1.7")
    compileOnly("org.spigotmc", "spigot-api", "1.13.2-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7")
    compileOnly("net.ess3", "EssentialsX", "2.17.2")

    implementation("org.spongepowered", "configurate-hocon", "3.7.1") {
        exclude(group = "com.google.guava", module = "guava")
    }
    implementation("org.spongepowered", "configurate-ext-kotlin", "3.7.1") {
        exclude(group = "com.google.guava", module = "guava")
    }
}

spigot {
    name = projectName
    apiVersion = "1.13"
    description = "Announcement plugin with support for permissions. Supports Hex colors and clickable messages/hover text using MiniMessage."
    website = "https://www.spigotmc.org/resources/announcer-plus.81005/"
    authors("jmp")
    depends("Vault")
    softDepends("PlaceholderAPI", "Prisma", "Essentials")
}

val autoRelocate by tasks.register<ConfigureShadowRelocation>("configureShadowRelocation", ConfigureShadowRelocation::class) {
    target = tasks.getByName("shadowJar") as ShadowJar
    val packageName = "${project.group}.${project.name.toLowerCase()}"
    prefix = "$packageName.shaded"
}

tasks {
    compileJava {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.forkOptions.executable = "javac"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.javaParameters = true
    }
    withType<ShadowJar> {
        archiveClassifier.set("")
        archiveFileName.set("$projectName-${project.version}.jar")
        dependsOn(autoRelocate)
        minimize()
    }
}

fun getLastCommitHash(): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "rev-parse", "--short", "HEAD")
        standardOutput = byteOut
    }
    return byteOut.toString().trim()
}