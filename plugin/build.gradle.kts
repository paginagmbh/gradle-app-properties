plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.3.0"
    jacoco
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app-properties-lib"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    website = "https://github.com/paginagmbh/gradle-app-properties"
    vcsUrl = "https://github.com/paginagmbh/gradle-app-properties"

    plugins {
        create("appProperties") {
            id = "gmbh.pagina.tools.gradle.app-properties"
            implementationClass = "gmbh.pagina.tools.gradle.AppPropertiesPlugin"
            displayName = "App Properties Plugin"
            description = "Provides build date, version and project name at runtime."
            tags.set(listOf("meta", "utility", "metadata", "version", "build"))
        }
    }
}

// Expand ${version} inside plugin.properties at build time
tasks.processResources {
    filesMatching("**/plugin.properties") {
        expand("version" to project.version)
    }
}

tasks.test {
    useJUnitPlatform()
}

