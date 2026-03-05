plugins {
    java
}

val commonLibJarPath = providers.gradleProperty("commonLibJar").orNull
    ?: throw GradleException("Missing required property: -PcommonLibJar=<path-to-minecraft-common-lib-jar>")

val commonLibJar = file(commonLibJarPath)
if (!commonLibJar.exists()) {
    throw GradleException("Configured commonLibJar does not exist: ${commonLibJar.absolutePath}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(files(commonLibJar))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(
        zipTree(commonLibJar).matching {
            exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        }
    )
}
