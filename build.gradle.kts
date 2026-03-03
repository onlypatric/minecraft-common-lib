plugins {
    `java-library`
    jacoco
}

group = "dev.patric"
version = "0.1.11-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.18.0")
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

val verifyCoreDependencyPolicy = tasks.register("verifyCoreDependencyPolicy") {
    group = "verification"
    description = "Enforces RC core dependency policy (no external adapters in core)."

    doLast {
        val forbiddenConfigurations = listOf("api", "implementation", "runtimeOnly")
        forbiddenConfigurations.forEach { configurationName ->
            val configuration = configurations.getByName(configurationName)
            if (!configuration.dependencies.isEmpty()) {
                val found = configuration.dependencies.joinToString(", ") { "${it.group}:${it.name}:${it.version}" }
                throw GradleException(
                    "Configuration '$configurationName' must not declare core dependencies. Found: $found"
                )
            }
        }

        val compileOnlyConfiguration = configurations.getByName("compileOnly")
        val compileOnlyDependencies = compileOnlyConfiguration.dependencies.toList()
        if (compileOnlyDependencies.size != 1) {
            val found = compileOnlyDependencies.joinToString(", ") { "${it.group}:${it.name}:${it.version}" }
            throw GradleException(
                "Configuration 'compileOnly' must contain only io.papermc.paper:paper-api. Found: $found"
            )
        }

        val dependency = compileOnlyDependencies.first()
        val valid = dependency.group == "io.papermc.paper" && dependency.name == "paper-api"
        if (!valid) {
            throw GradleException(
                "Configuration 'compileOnly' must contain only io.papermc.paper:paper-api. Found: " +
                        "${dependency.group}:${dependency.name}:${dependency.version}"
            )
        }
    }
}

tasks.check {
    dependsOn(verifyCoreDependencyPolicy)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
