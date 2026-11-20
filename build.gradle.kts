import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.util.jar.Manifest

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "org.gameyfin.plugins"
version = Manifest(file("src/main/resources/MANIFEST.MF").inputStream()).mainAttributes.getValue("Plugin-Version")

repositories {
    mavenLocal()
    mavenCentral()
}
dependencyLocking { lockAllConfigurations() }
val javaVersion = providers.gradleProperty("javaVersion").get()
val kotlinLanguageVersion = KotlinVersion.fromVersion(providers.gradleProperty("kotlinLanguageVersion").get())

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}
kotlin.compilerOptions {
    languageVersion = kotlinLanguageVersion
    apiVersion = kotlinLanguageVersion
    jvmTarget = JvmTarget.fromTarget(javaVersion)
    progressiveMode = true
    freeCompilerArgs.add("-Xjsr305=strict")
}

val pluginApiVersion = providers.gradleProperty("pluginApiVersion").get()

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("org.gameyfin:plugin-api:$pluginApiVersion")
    testImplementation("org.gameyfin:plugin-api:$pluginApiVersion")
    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
configurations.named("runtimeClasspath") { exclude(group = "org.slf4j") }

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    isZip64 = true
    dependsOn("kspKotlin")
    manifest { from("src/main/resources/MANIFEST.MF") }
    from(configurations.runtimeClasspath.get().map { zipTree(it) }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.EC")
    }
    from(layout.buildDirectory.dir("generated/ksp/main/resources"))
    from("src/main/resources") {
        include("logo.*")
        into("META-INF/resources")
    }
}

tasks.register<Copy>("copyDependencyClasses") {
    group = "build"
    description = "Copies runtime dependency classes for PF4J development loading"
    dependsOn(tasks.jar)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { zipTree(it) }) { include("**/*.class") }
    into(layout.buildDirectory.dir("classes/kotlin/main"))
}
tasks.register<Copy>("prepareDevPlugin") {
    group = "build"
    description = "Prepares dependency classes and extension indexes for PF4J development loading"
    dependsOn("copyDependencyClasses")
    from(layout.buildDirectory.dir("generated/ksp/main/resources"))
    into(layout.buildDirectory.dir("resources/main"))
}
tasks.build { dependsOn("prepareDevPlugin") }
tasks.test {
    useJUnitPlatform()
    dependsOn("prepareDevPlugin")
}

repositories {
    maven {
        setUrl("https://dl.frostwire.com/maven")
        content {
            includeGroup("com.frostwire")
        }
    }
}

dependencies {
    ksp("care.better.pf4j:pf4j-kotlin-symbol-processing:${rootProject.extra["pf4jKspVersion"]}")

    implementation("com.frostwire:jlibtorrent:${rootProject.extra["frostwireJlibTorrentVersion"]}")
    implementation("com.frostwire:jlibtorrent-windows:${rootProject.extra["frostwireJlibTorrentVersion"]}")
    implementation("com.frostwire:jlibtorrent-macosx-x86_64:${rootProject.extra["frostwireJlibTorrentVersion"]}")
    implementation("com.frostwire:jlibtorrent-macosx-arm64:${rootProject.extra["frostwireJlibTorrentVersion"]}")
    implementation("com.frostwire:jlibtorrent-linux-x86_64:${rootProject.extra["frostwireJlibTorrentVersion"]}")
    implementation("com.frostwire:jlibtorrent-linux-arm64:${rootProject.extra["frostwireJlibTorrentVersion"]}")
}

// Extract native libraries from jlibtorrent JARs for local debugging
tasks.register<Copy>("extractNativeLibraries") {
    description = "Extracts the native libraries"
    group = JavaBasePlugin.DOCUMENTATION_GROUP

    dependsOn(tasks.named("compileKotlin"))

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { project.zipTree(it) }) {
        include("lib/**")
    }
    into(layout.buildDirectory.get().asFile.resolve("classes/kotlin/main"))
}

tasks.named("classes") {
    dependsOn("extractNativeLibraries")
}

tasks.named("test") {
    dependsOn("extractNativeLibraries")
}

