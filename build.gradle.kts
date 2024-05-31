import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.jpenilla.runpaper.task.RunServer

plugins {
  kotlin("jvm") version "2.0.0"
  alias(libs.plugins.indra) apply false
  alias(libs.plugins.indraGit)
  alias(libs.plugins.runPaper)
  alias(libs.plugins.shadow)
  alias(libs.plugins.indraSpotless) apply false
  alias(libs.plugins.spotless)
}

repositories {
  mavenCentral()
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://repo.jpenilla.xyz/snapshots/")
  maven("https://repo.essentialsx.net/releases/")
  maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
    content { includeGroup("me.clip") }
  }
  maven("https://jitpack.io") {
    content { includeGroupByRegex("com\\.github\\..*") }
  }
}

dependencies {
  implementation(platform(kotlin("bom")))

  compileOnly("dev.folia", "folia-api", "1.19.4-R0.1-SNAPSHOT")
  compileOnly("com.github.MilkBowl", "VaultAPI", "1.7.1")
  compileOnly("net.essentialsx", "EssentialsX", "2.20.1") {
    isTransitive = false
  }
  compileOnly("me.clip", "placeholderapi", "2.11.6")
  compileOnly("com.mojang:datafixerupper:7.0.14")

  implementation(platform("net.kyori:adventure-bom:4.17.0"))
  implementation("net.kyori", "adventure-extra-kotlin")
  implementation("net.kyori", "adventure-serializer-configurate4")

  implementation(platform("org.incendo:cloud-bom:2.0.0-rc.2"))
  implementation("org.incendo:cloud-kotlin-extensions")
  implementation(platform("org.incendo:cloud-minecraft-bom:2.0.0-beta.8"))
  implementation("org.incendo:cloud-paper")
  implementation("org.incendo:cloud-minecraft-extras")

  implementation(platform("org.spongepowered:configurate-bom:4.1.2"))
  implementation("org.spongepowered", "configurate-hocon")
  implementation("org.spongepowered", "configurate-extra-kotlin")

  implementation("io.insert-koin", "koin-core", "3.5.6")
  implementation("xyz.jpenilla", "legacy-plugin-base", "0.0.1+119-SNAPSHOT")
  implementation("org.bstats", "bstats-bukkit", "3.0.2")
  implementation("io.papermc", "paperlib", "1.0.8")

  implementation(variantOf(libs.reflection.remapper) { classifier("all") }) {
    isTransitive = false
  }
}

version = (version as String).decorateVersion()

kotlin {
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
  compilerOptions {
    jvmTarget = JvmTarget.JVM_1_8
    freeCompilerArgs = listOf("-Xjdk-release=1.8")
  }
}

java.disableAutoTargetJvm()

tasks {
  compileJava {
    options.release = 8
  }
  jar {
    archiveClassifier = "not-shadowed"
    manifest {
      attributes("paperweight-mappings-namespace" to "mojang")
    }
  }
  shadowJar {
    from(rootProject.file("license.txt")) {
      rename { "license_${rootProject.name.lowercase()}.txt" }
    }

    mergeServiceFiles()
    archiveClassifier.set(null as String?)
    archiveBaseName.set(project.name) // Use uppercase name for final jar

    val prefix = "${project.group}.${project.name.lowercase()}.lib"
    sequenceOf(
      "com.typesafe.config",
      "io.leangen.geantyref",
      "io.papermc.lib",
      "net.kyori",
      "xyz.jpenilla.pluginbase",
      "org.incendo",
      "org.koin",
      "co.touchlab.stately",
      "org.spongepowered.configurate",
      "org.bstats",
      "kotlin",
      "xyz.jpenilla.reflectionremapper",
    ).forEach { pkg ->
      relocate(pkg, "$prefix.$pkg")
    }

    dependencies {
      exclude(dependency("org.jetbrains:annotations"))
    }
  }
  assemble {
    dependsOn(shadowJar)
  }
  runServer {
    minecraftVersion("1.20.6")
  }
  withType<RunServer> {
    javaLauncher = project.javaToolchains.launcherFor {
      languageVersion.set(JavaLanguageVersion.of(21))
    }
  }
  register("format") {
    group = "formatting"
    description = "Formats source code according to project style."
    dependsOn(spotlessApply)
  }
  processResources {
    val props = mapOf(
      "version" to project.version,
      "website" to "https://github.com/jpenilla/AnnouncerPlus",
      "description" to project.description,
      "apiVersion" to "1.13",
    )
    inputs.properties(props)
    filesMatching("plugin.yml") {
      expand(props)
    }
  }
}

spotless {
  val overrides = mapOf(
    "ktlint_standard_filename" to "disabled",
    "ktlint_standard_trailing-comma-on-call-site" to "disabled",
    "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
  )
  kotlin {
    ktlint(libs.versions.ktlint.get()).editorConfigOverride(overrides)
  }
  kotlinGradle {
    ktlint(libs.versions.ktlint.get()).editorConfigOverride(overrides)
  }
}

// The following is to work around https://github.com/diffplug/spotless/issues/1599
// Ensure the ktlint step is before the license header step
plugins.apply(libs.plugins.indraSpotless.get().pluginId)

runPaper.folia.registerTask()

fun String.decorateVersion(): String =
  if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7)
  ?: error("Failed to determine git hash.")
