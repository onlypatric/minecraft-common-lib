plugins {
    `java-library`
    jacoco
}

group = "dev.patric"
version = "0.8.0"

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

subprojects {
    apply(plugin = "java-library")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.fancyinnovations.com/releases")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        "testImplementation"("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
        "testImplementation"(platform("org.junit:junit-bom:5.12.2"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testImplementation"("org.mockito:mockito-core:5.18.0")
        "testImplementation"("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.106.1")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.106.1")
    testImplementation(project(":adapter-commandapi"))
    testImplementation(project(":adapter-fastboard"))
    testImplementation(project(":adapter-fancyholograms"))
    testImplementation(project(":adapter-fancynpcs"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val integrationTestSourceSet = sourceSets.create("integrationTest") {
    java.srcDir("src/integrationTest/java")
    resources.srcDir("src/integrationTest/resources")
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

configurations[integrationTestSourceSet.implementationConfigurationName]
    .extendsFrom(configurations.testImplementation.get())
configurations[integrationTestSourceSet.runtimeOnlyConfigurationName]
    .extendsFrom(configurations.testRuntimeOnly.get())

val adapterIntegrationTestSourceSet = sourceSets.create("adapterIntegrationTest") {
    java.srcDir("src/adapterIntegrationTest/java")
    resources.srcDir("src/adapterIntegrationTest/resources")
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

configurations[adapterIntegrationTestSourceSet.implementationConfigurationName]
    .extendsFrom(configurations.testImplementation.get())
configurations[adapterIntegrationTestSourceSet.runtimeOnlyConfigurationName]
    .extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    add(adapterIntegrationTestSourceSet.implementationConfigurationName, project(":adapter-commandapi"))
    add(adapterIntegrationTestSourceSet.implementationConfigurationName, project(":adapter-fastboard"))
    add(adapterIntegrationTestSourceSet.implementationConfigurationName, project(":adapter-fancyholograms"))
    add(adapterIntegrationTestSourceSet.implementationConfigurationName, project(":adapter-fancynpcs"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.test {
    useJUnitPlatform()
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs opt-in integration harness tests."
    group = "verification"
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    useJUnitPlatform()
    onlyIf {
        providers.gradleProperty("runIntegrationHarness")
                .map(String::toBoolean)
                .orElse(false)
                .get()
    }
}

val adapterIntegrationTest = tasks.register<Test>("adapterIntegrationTest") {
    description = "Runs opt-in adapter integration smoke tests."
    group = "verification"
    testClassesDirs = adapterIntegrationTestSourceSet.output.classesDirs
    classpath = adapterIntegrationTestSourceSet.runtimeClasspath
    useJUnitPlatform()
    onlyIf {
        providers.gradleProperty("runAdapterIntegration")
                .map(String::toBoolean)
                .orElse(false)
                .get()
    }
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

        val guardedApiDirs = listOf(
            file("src/main/java/dev/patric/commonlib/api/adapter"),
            file("src/main/java/dev/patric/commonlib/api/capability"),
            file("src/main/java/dev/patric/commonlib/api/match"),
            file("src/main/java/dev/patric/commonlib/api/hud"),
            file("src/main/java/dev/patric/commonlib/api/gui"),
            file("src/main/java/dev/patric/commonlib/api/arena"),
            file("src/main/java/dev/patric/commonlib/api/team"),
            file("src/main/java/dev/patric/commonlib/api/persistence"),
            file("src/main/java/dev/patric/commonlib/api/port/noop")
        )
        val allowedImportPrefixes = listOf("java.", "javax.", "org.bukkit.", "dev.patric.commonlib.")
        val importPattern = Regex("""^\s*import\s+([A-Za-z0-9_.]+);""", setOf(RegexOption.MULTILINE))

        guardedApiDirs.forEach { dir ->
            if (!dir.exists()) {
                return@forEach
            }

            dir.walkTopDown()
                .filter { it.isFile && it.extension == "java" }
                .forEach { source ->
                    val text = source.readText()
                    importPattern.findAll(text).forEach { match ->
                        val imported = match.groupValues[1]
                        val allowed = allowedImportPrefixes.any(imported::startsWith)
                        if (!allowed) {
                            throw GradleException(
                                "Unexpected third-party import '$imported' in guarded API source: ${source.path}"
                            )
                        }
                    }
                }
        }
    }
}

val verifyAdapterDependencyPolicy = tasks.register("verifyAdapterDependencyPolicy") {
    group = "verification"
    description = "Ensures adapter modules only depend on core and do not cross-link each other."

    doLast {
        val adapterProjects = setOf(
            ":adapter-commandapi",
            ":adapter-fastboard",
            ":adapter-fancyholograms",
            ":adapter-fancynpcs"
        )

        adapterProjects.forEach { projectPath ->
            val adapterProject = project(projectPath)
            val invalidProjectDeps = mutableListOf<String>()

            listOf("api", "implementation", "runtimeOnly", "compileOnly").forEach { configurationName ->
                val configuration = adapterProject.configurations.findByName(configurationName) ?: return@forEach
                configuration.dependencies.forEach { dependency ->
                    if (dependency is org.gradle.api.artifacts.ProjectDependency) {
                        val target = dependency.path
                        if (target != ":") {
                            invalidProjectDeps += "$projectPath:$configurationName -> $target"
                        }
                    }
                }
            }

            if (invalidProjectDeps.isNotEmpty()) {
                throw GradleException(
                    "Adapter modules may only depend on core project ':'. Invalid project dependencies: " +
                            invalidProjectDeps.joinToString(", ")
                )
            }
        }
    }
}

tasks.check {
    dependsOn(verifyCoreDependencyPolicy)
    dependsOn(verifyAdapterDependencyPolicy)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
