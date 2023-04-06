pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://repo.jpenilla.xyz/snapshots")
  }
}

plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "6.0.1"
}

rootProject.name = "AnnouncerPlus"
