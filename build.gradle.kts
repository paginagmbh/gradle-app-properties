plugins {
    id("gmbh.pagina.tools.gradle.release") version "1.3.0-SNAPSHOT"
}

subprojects {
    group = "gmbh.pagina.tools.gradle"
    version = "1.0-SNAPSHOT"

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(11)
            }
        }
        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
        }
        tasks.withType<Javadoc>().configureEach {
            options.encoding = "UTF-8"
        }
    }

    val artifactoryUrl = "https://bintray.pagina.gmbh/artifactory"
    plugins.withType<MavenPublishPlugin> {
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    url = uri(if (version.toString().endsWith("SNAPSHOT")) "$artifactoryUrl/plugins-snapshot-local" else "$artifactoryUrl/plugins-release-local")
                    name = if (version.toString().endsWith("SNAPSHOT")) "PaginaArtifactorySnapshots" else "PaginaArtifactoryReleases"
                    credentials {
                        username = System.getenv("ARTIFACTORY_USER")
                        password = System.getenv("ARTIFACTORY_PASSWORD")
                    }
                }
            }
        }
    }
}

tasks.named("updateVersionNumberInReadme") {
    extra["pluginId"] = "gmbh.pagina.tools.gradle.app-properties"
}