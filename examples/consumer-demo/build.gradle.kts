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

    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.106.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.test {
    useJUnitPlatform()
}
