val protocolLibCoordinate = providers
    .gradleProperty("protocolLibCoordinate")
    .orElse("com.comphenix.protocol:ProtocolLib:5.3.0")

dependencies {
    implementation(project(":"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(protocolLibCoordinate.get())
}
