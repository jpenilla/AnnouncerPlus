import me.modmuss50.mpp.ReleaseType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.jpenilla.runpaper.task.RunServer

plugins {
  kotlin("jvm") version "2.2.20"
  alias(libs.plugins.indra) apply false
  alias(libs.plugins.indraGit)
  alias(libs.plugins.runPaper)
  alias(libs.plugins.shadow)
  alias(libs.plugins.indraSpotless)
  alias(libs.plugins.spotless)
  alias(libs.plugins.modPublishPlugin)
}

repositories {
  mavenCentral {
    mavenContent { releasesOnly() }
  }
  maven("https://central.sonatype.com/repository/maven-snapshots/") {
    mavenContent { snapshotsOnly() }
  }
  maven("https://repo.jpenilla.xyz/snapshots/") {
    mavenContent {
      snapshotsOnly()
      includeGroup("xyz.jpenilla")
      includeModule("net.kyori", "adventure-text-feature-pagination")
    }
  }
  maven("https://repo.essentialsx.net/releases/") {
    mavenContent {
      releasesOnly()
      includeGroup("net.essentialsx")
    }
  }
  maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
    mavenContent {
      releasesOnly()
      includeGroup("me.clip")
    }
  }
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://jitpack.io") {
    mavenContent { includeGroupByRegex("com\\.github\\..*") }
  }
}

dependencies {
  implementation(platform(kotlin("bom")))

  compileOnly("dev.folia", "folia-api", "1.19.4-R0.1-SNAPSHOT")
  compileOnly("com.github.MilkBowl", "VaultAPI", "1.7.1")
  compileOnly("net.essentialsx", "EssentialsX", "2.21.2") {
    isTransitive = false
  }
  compileOnly("me.clip", "placeholderapi", "2.11.7")
  compileOnly("com.mojang:datafixerupper:7.0.14")

  implementation("net.kyori:adventure-platform-bukkit:4.4.1")
  implementation(platform("net.kyori:adventure-bom:4.25.0"))
  implementation("net.kyori", "adventure-extra-kotlin")
  implementation("net.kyori", "adventure-serializer-configurate4")

  implementation(platform("org.incendo:cloud-bom:2.0.0"))
  implementation("org.incendo:cloud-kotlin-extensions")
  implementation(platform("org.incendo:cloud-minecraft-bom:2.0.0-beta.12"))
  implementation("org.incendo:cloud-paper")
  implementation("org.incendo:cloud-minecraft-extras")
  implementation(platform("org.incendo:cloud-translations-bom:1.0.0-SNAPSHOT"))
  implementation("org.incendo:cloud-translations-core")
  implementation("org.incendo:cloud-translations-bukkit")
  implementation("org.incendo:cloud-translations-minecraft-extras")

  implementation(platform("org.spongepowered:configurate-bom:4.2.0"))
  implementation("org.spongepowered", "configurate-hocon")
  implementation("org.spongepowered", "configurate-extra-kotlin")

  implementation("io.insert-koin", "koin-core", "4.1.1")
  implementation("xyz.jpenilla", "legacy-plugin-base", "0.0.1+161-SNAPSHOT")
  implementation("org.bstats", "bstats-bukkit", "3.1.0")
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
    jvmTarget = JvmTarget.JVM_17
    freeCompilerArgs = listOf("-Xjdk-release=17")
  }
}

java.disableAutoTargetJvm()

tasks {
  compileJava {
    options.release = 17
  }
  jar {
    archiveClassifier = "not-shadowed"
    manifest {
      attributes("paperweight-mappings-namespace" to "mojang")
    }
  }
  shadowJar {
    val name = rootProject.name.lowercase()
    from(rootProject.file("license.txt")) {
      rename { "license_$name.txt" }
    }

    mergeServiceFiles()
    // Needed for mergeServiceFiles to work properly in Shadow 9+
    filesMatching("META-INF/services/**") {
      duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    archiveClassifier.set(null as String?)
    archiveBaseName.set(project.name) // Use uppercase name for final jar

    val prefix = "${project.group}.${project.name.lowercase()}.lib"
    sequenceOf(
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
    minecraftVersion("1.21.10")
  }
  withType<RunServer> {
    javaLauncher = project.javaToolchains.launcherFor {
      languageVersion = JavaLanguageVersion.of(21)
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

publishMods.modrinth {
  projectId = "g8XCro6n"
  type = ReleaseType.STABLE
  file = tasks.shadowJar.flatMap { it.archiveFile }
  minecraftVersions = listOf(
    "1.8.8",
    "1.8.9",
    "1.9.4",
    "1.10.2",
    "1.11.2",
    "1.12.2",
    "1.13.2",
    "1.14.4",
    "1.15.2",
    "1.16.5",
    "1.17.1",
    "1.18.2",
    "1.19.4",
    "1.20.6",
    "1.21.8",
    "1.21.10",
  )
  modLoaders = listOf("paper", "folia")
  changelog = providers.environmentVariable("RELEASE_NOTES")
  accessToken = providers.environmentVariable("MODRINTH_TOKEN")
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

runPaper.folia.registerTask()

fun String.decorateVersion(): String =
  if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this

fun lastCommitHash(): String = indraGit.commit().orNull?.name?.substring(0, 7)
  ?: error("Failed to determine git hash.")
