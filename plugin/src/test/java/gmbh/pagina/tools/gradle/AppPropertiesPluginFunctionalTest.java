package gmbh.pagina.tools.gradle;

import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for {@link AppPropertiesPlugin} using the Gradle TestKit.
 *
 * <p>Each test spins up an isolated Gradle project in a temporary directory, applies the plugin
 * via {@code withPluginClasspath()}, runs the {@code createProperties} task and asserts the
 * contents of the generated {@code build/resources/main/meta.properties} file.
 *
 * <p>Only {@code createProperties} (and its {@code processResources} dependency) are executed in
 * these tests. The {@code implementation} dependency on {@code app-properties-lib} that the plugin
 * adds to consumer projects is therefore never resolved, so no local Maven publish is required
 * before running the tests.
 */
class AppPropertiesPluginFunctionalTest {

    @TempDir
    Path projectDir;

    @BeforeEach
    void writeSettings() throws IOException {
        Files.writeString(
                projectDir.resolve("settings.gradle.kts"),
                "rootProject.name = \"test-project\"\n");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static final String APPLY_PLUGIN =
            "plugins { id(\"gmbh.pagina.tools.gradle.app-properties\") }\n";

    private void writeBuildFile(String content) throws IOException {
        Files.writeString(projectDir.resolve("build.gradle.kts"), content);
    }

    private void writeBuildFile(String... lines) throws IOException {
        writeBuildFile(String.join("\n", lines) + "\n");
    }

    private GradleRunner runner(String... args) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments(args);
    }

    private Properties loadMetaProperties() throws IOException {
        var propsFile = projectDir.resolve("build/resources/main/meta.properties");
        assertTrue(propsFile.toFile().exists(), "meta.properties was not generated");
        var props = new Properties();
        try (var reader = new FileReader(propsFile.toFile())) {
            props.load(reader);
        }
        return props;
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void createProperties_withDefaults_writesProjectNameAsDisplayName() throws IOException {
        writeBuildFile(
                APPLY_PLUGIN,
                "version = \"1.0.0\"");

        var result = runner("createProperties").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":createProperties").getOutcome());
        var props = loadMetaProperties();
        assertEquals("1.0.0",        props.getProperty("version"));
        assertEquals("test-project", props.getProperty("name"));
        assertFalse(props.getProperty("buildDate").isBlank());
        assertEquals("",             props.getProperty("copyrightHolder"));
        assertEquals("",             props.getProperty("copyrightFromYear"));
    }

    @Test
    void createProperties_withCustomDisplayName_writesCustomName() throws IOException {
        writeBuildFile(
                APPLY_PLUGIN,
                "version = \"1.0.0\"",
                "appProperties {",
                "    displayName = \"My Custom App\"",
                "}");

        runner("createProperties").build();

        assertEquals("My Custom App", loadMetaProperties().getProperty("name"));
    }

    @Test
    void createProperties_withCopyrightHolder_writesCopyrightHolder() throws IOException {
        writeBuildFile(
                APPLY_PLUGIN,
                "version = \"1.0.0\"",
                "appProperties {",
                "    copyrightHolder = \"pagina GmbH, Tübingen\"",
                "}");

        runner("createProperties").build();

        assertEquals("pagina GmbH, Tübingen", loadMetaProperties().getProperty("copyrightHolder"));
    }

    @Test
    void createProperties_withAllPropertiesConfigured_writesAllValues() throws IOException {
        writeBuildFile(
                APPLY_PLUGIN,
                "version = \"3.0.0\"",
                "appProperties {",
                "    displayName       = \"Full Config App\"",
                "    copyrightHolder   = \"Acme Corp\"",
                "    copyrightFromYear = 2019",
                "}");

        runner("createProperties").build();

        var props = loadMetaProperties();
        assertEquals("3.0.0",           props.getProperty("version"));
        assertEquals("Full Config App", props.getProperty("name"));
        assertEquals("Acme Corp",       props.getProperty("copyrightHolder"));
        assertEquals("2019",            props.getProperty("copyrightFromYear"));
    }

    @Test
    void createProperties_secondRunWithSameInputs_isUpToDate() throws IOException {
        writeBuildFile(
                APPLY_PLUGIN,
                "version = \"1.0.0\"");

        runner("createProperties").build();
        var secondResult = runner("createProperties").build();

        assertEquals(TaskOutcome.UP_TO_DATE, secondResult.task(":createProperties").getOutcome());
    }

    @Test
    void createProperties_afterInputChange_reruns() throws IOException {
        writeBuildFile(
                APPLY_PLUGIN,
                "version = \"1.0.0\"",
                "appProperties { displayName = \"Before\" }");
        runner("createProperties").build();

        writeBuildFile(
                APPLY_PLUGIN,
                "version = \"1.0.0\"",
                "appProperties { displayName = \"After\" }");
        var result = runner("createProperties").build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":createProperties").getOutcome());
        assertEquals("After", loadMetaProperties().getProperty("name"));
    }
}
