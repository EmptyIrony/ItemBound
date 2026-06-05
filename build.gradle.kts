import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.izzel.taboolib.gradle.*
import io.izzel.taboolib.gradle.Basic
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.BukkitUI
import io.izzel.taboolib.gradle.BukkitUtil
import io.izzel.taboolib.gradle.BukkitHook
import io.izzel.taboolib.gradle.BukkitNMS
import io.izzel.taboolib.gradle.BukkitNMSUtil
import io.izzel.taboolib.gradle.BukkitNMSItemTag
import io.izzel.taboolib.gradle.AlkaidRedis
import io.izzel.taboolib.gradle.Database


plugins {
    java
    id("io.izzel.taboolib") version "2.0.37"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

taboolib {
    env {
        install(Basic)
        install(Bukkit)
        install(BukkitUI)
        install(BukkitUtil)
        install(BukkitHook)
        install(BukkitNMS)
        install(BukkitNMSUtil)
        install(BukkitNMSItemTag)
        install(AlkaidRedis)
        install(Database)
    }
    description {
        name = "ItemBound"
        contributors {
            name("存在")
        }
    }
    version { taboolib = "6.3.0-932e79c" }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.guava:guava:33.3.1-jre")
    compileOnly("ink.ptms.core:v12105:12105:mapped")
    compileOnly("ink.ptms.core:v12105:12105:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
