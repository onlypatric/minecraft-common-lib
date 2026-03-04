val invuiVersion = rootProject.property("invuiVersion") as String

dependencies {
    implementation(project(":"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("xyz.xenondevs.invui:invui-core:$invuiVersion")

    testImplementation("xyz.xenondevs.invui:invui-core:$invuiVersion")
}
