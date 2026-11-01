plugins {
    kotlin("jvm") version "2.0.21"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

// 実行用の単一jar（Kotlin標準ライブラリを同梱）
tasks.jar {
    archiveFileName.set("nesupe-server.jar")
    manifest {
        attributes["Main-Class"] = "nesupe.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
