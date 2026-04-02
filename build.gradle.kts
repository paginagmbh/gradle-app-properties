plugins {
    id("gmbh.pagina.tools.gradle.release") version "1.3.0"
}

subprojects {
    group = "gmbh.pagina.tools.gradle"
    version = "1.1.1-SNAPSHOT"

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(21)
            }
        }
        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release = 11
        }
        tasks.withType<Javadoc>().configureEach {
            options.encoding = "UTF-8"
        }
    }

    plugins.withId("jacoco") {
        tasks.withType<Test>().configureEach {
            finalizedBy(tasks.named("jacocoTestReport"))
        }
        tasks.withType<JacocoReport>().configureEach {
            reports {
                xml.required = true
                html.required = true
            }
        }

        // Prints instruction coverage to stdout so GitLab can parse it for the pipeline badge.
        tasks.register("printCoverage") {
            group = "verification"
            description = "Prints instruction coverage % to stdout for CI badge parsing."
            dependsOn("jacocoTestReport")
            doLast {
                val report = layout.buildDirectory
                    .file("reports/jacoco/test/jacocoTestReport.xml").get().asFile
                if (!report.exists()) return@doLast
                val match = Regex("""type="INSTRUCTION" missed="(\d+)" covered="(\d+)"""")
                    .findAll(report.readText()).lastOrNull() ?: return@doLast
                val missed  = match.groupValues[1].toLong()
                val covered = match.groupValues[2].toLong()
                val total   = covered + missed
                if (total > 0) println("Coverage: ${"%.1f".format(covered * 100.0 / total)}%")
            }
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