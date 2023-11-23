import xyz.jpenilla.runpaper.task.RunServer

plugins {
  kotlin("jvm") version "1.9.21"
  alias(libs.plugins.indra)
  alias(libs.plugins.indraGit)
  alias(libs.plugins.runPaper)
  alias(libs.plugins.shadow)
  alias(libs.plugins.indraSpotless)
  alias(libs.plugins.spotless)
}

repositories {
  mavenCentral()
  sonatype.s01Snapshots()
  sonatype.ossSnapshots()
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
  compileOnly("me.clip", "placeholderapi", "2.11.5")

  implementation(platform("net.kyori:adventure-bom:4.14.0"))
  implementation("net.kyori", "adventure-extra-kotlin")
  implementation("net.kyori", "adventure-serializer-configurate4")

  implementation(platform("cloud.commandframework:cloud-bom:1.8.4"))
  implementation("cloud.commandframework", "cloud-paper")
  implementation("cloud.commandframework", "cloud-kotlin-extensions")
  implementation("cloud.commandframework", "cloud-minecraft-extras")

  implementation(platform("org.spongepowered:configurate-bom:4.1.2"))
  implementation("org.spongepowered", "configurate-hocon")
  implementation("org.spongepowered", "configurate-extra-kotlin")

  implementation("io.insert-koin", "koin-core", "3.5.0")
  implementation("xyz.jpenilla", "legacy-plugin-base", "0.0.1+98-SNAPSHOT")
  implementation("org.bstats", "bstats-bukkit", "3.0.2")
  implementation("io.papermc", "paperlib", "1.0.8")

  implementation("xyz.jpenilla:reflection-remapper:0.1.0-SNAPSHOT")
}

version = (version as String).decorateVersion()

java {
  disableAutoTargetJvm()
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  jar {
    archiveClassifier.set("not-shadowed")
  }
  shadowJar {
    from(rootProject.file("license.txt")) {
      rename { "license_${rootProject.name.lowercase()}.txt" }
    }

    minimize()
    archiveClassifier.set(null as String?)
    archiveBaseName.set(project.name) // Use uppercase name for final jar

    val prefix = "${project.group}.${project.name.lowercase()}.lib"
    sequenceOf(
      "com.typesafe.config",
      "io.leangen.geantyref",
      "io.papermc.lib",
      "net.kyori",
      "xyz.jpenilla.pluginbase",
      "cloud.commandframework",
      "org.koin",
      "org.spongepowered.configurate",
      "org.bstats",
      "kotlin",
      "xyz.jpenilla.reflectionremapper",
      "net.fabricmc.mappingio"
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
    minecraftVersion("1.20.2")
  }
  withType<RunServer> {
    javaLauncher.set(
      project.javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
      }
    )
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

runPaper.folia.registerTask()

fun String.decorateVersion(): String =
  if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7)
  ?: error("Failed to determine git hash.")
