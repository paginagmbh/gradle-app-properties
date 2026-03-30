package gmbh.pagina.tools.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gradle plugin that:
 *
 * <ol>
 *   <li>Applies the {@code java} plugin (if not already applied).
 *   <li>Registers the {@code createProperties} task, which generates {@code
 *       build/resources/main/meta.properties}.
 *   <li>Wires {@code classes} to depend on {@code createProperties}.
 *   <li>Adds <strong>only</strong> the {@code app-properties-runtime} jar as an {@code
 *       implementation} dependency to the consumer project, keeping the Gradle API off the
 *       consumer's runtime classpath.
 * </ol>
 *
 * <p>Configure via the {@code appProperties} extension:
 *
 * <pre>{@code
 * appProperties {
 *     displayname = "My Application"
 * }
 * }</pre>
 */
public class AppPropertiesPlugin implements Plugin<Project> {

    private static final String RUNTIME_GROUP = "gmbh.pagina.tools.gradle";
    private static final String RUNTIME_ARTIFACT = "app-properties-lib";

    @Override
    public void apply(Project project) {
        // Ensure the java plugin is present (provides processResources / classes / …)
        project.getPlugins().apply("java");

        // Register the extension
        var extension =
                project.getExtensions().create("appProperties", AppPropertiesExtension.class);

        // Default displayname to the project name
        extension.getDisplayName().convention(project.getName());
        extension.getCopyrightHolder().convention("");

        // Register createProperties task
        var createPropertiesTask = project.getTasks()
                .register("createProperties", CreatePropertiesTask.class, task -> {
                    task.setDescription(
                            "Generates build/resources/main/meta.properties.");
                    task.setGroup("build");

                    task.dependsOn(project.getTasks().named("processResources"));

                    task.getVersion()
                            .set(
                                    project.provider(
                                            () -> project.getVersion().toString()));
                    task.getDisplayName().set(extension.getDisplayName());
                    task.getCopyrightHolder().set(extension.getCopyrightHolder());
                    task.getCopyrightFromYear().set(extension.getCopyrightFromYear());
                    task.getOutputFile()
                            .set(
                                    project.getLayout()
                                            .getBuildDirectory()
                                            .file(
                                                    "resources/main/meta.properties"));
                });

        // Make 'classes' depend on createProperties
        project.getTasks().named("classes").configure(t -> t.dependsOn(createPropertiesTask));

        // Add ONLY the slim runtime jar as an implementation dependency.
        // The plugin jar (which carries the Gradle API) is NOT added here,
        // keeping the consumer's runtime classpath clean.
        project.getDependencies()
                .add(
                        "implementation",
                        RUNTIME_GROUP + ":" + RUNTIME_ARTIFACT + ":" + resolvePluginVersion());
    }

    /** Reads the plugin version from the filtered resource file bundled in this jar. */
    private static String resolvePluginVersion() {
        var props = new Properties();
        try (InputStream is =
                AppPropertiesPlugin.class.getResourceAsStream(
                        "/gmbh/pagina/tools/gradle/plugin.properties")) {
            if (is == null) {
                throw new IllegalStateException(
                        "plugin.properties not found in app-properties-plugin jar");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read plugin.properties", e);
        }
        return props.getProperty("version");
    }
}
