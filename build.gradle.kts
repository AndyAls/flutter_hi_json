import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

group = "com.andy.hijson.flutter"
version = "1.0.2"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2022.3")
        bundledPlugin("org.jetbrains.plugins.terminal")
    }
    implementation("com.google.code.gson:gson:2.10.1")
}

intellijPlatform {

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "223"
        }
        description = file("src/main/resources/META-INF/description.html").readText()
        changeNotes = file("src/main/resources/META-INF/changelog.html").readText()
    }

    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }

    // Plugin verification against the oldest and newest supported IDE builds.
    // Run with: ./gradlew verifyPlugin
    pluginVerification {
        ides {
            // Compatibility floor (sinceBuild = 223)
            ide(IntelliJPlatformType.IntellijIdeaCommunity, "2022.3")
            // Latest IntelliJ IDEA 2026.3 EAP line
            ide(IntelliJPlatformType.IntellijIdeaUltimate, "263.6800.24")
        }
    }
}

// The IntelliJ Plugin Verifier caches extracted plugins under ~/.pluginVerifier by
// default. Redirect it into the build directory so verification also works when the
// home directory is not writable (sandboxed environments, some CI runners).
tasks.withType<VerifyPluginTask>().configureEach {
    systemProperty(
        "plugin.verifier.home.dir",
        layout.buildDirectory.dir("pluginVerifierHome").get().asFile.absolutePath,
    )
}


kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
