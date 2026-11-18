pluginManagement {
    repositories { gradlePluginPortal() }
    plugins {
        kotlin("jvm") version providers.gradleProperty("kotlinVersion").get()
        kotlin("plugin.serialization") version providers.gradleProperty("kotlinVersion").get()
        id("com.google.devtools.ksp") version providers.gradleProperty("kspVersion").get()
    }
}
rootProject.name = "plugin-torrent-download"
