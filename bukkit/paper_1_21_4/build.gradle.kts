plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

repositories {
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.lunarclient.dev/") // Lunar Client Apollo
    maven("https://repo.viaversion.com/")
    maven {
        name = "luck-repo"
        url = uri("https://repo.lucko.me/")
        content {
            includeModule("me.lucko", "spark-api")
        }
    }
}

val version = "1.21.4-R0.1-SNAPSHOT"

dependencies {
    implementation(projects.bukkit)
    paperweight.paperDevBundle(version)
    compileOnly("io.papermc.paper:paper-api:${version}")
}

tasks.compileJava {
    options.release.set(21)
}

java {
    toolchain {
        // paperweight-userdev's bundled remapper can't parse class files from newer JDKs,
        // and this module doesn't need anything past 21 anyway (see release above).
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
