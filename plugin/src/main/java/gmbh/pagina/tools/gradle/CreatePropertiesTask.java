package gmbh.pagina.tools.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Gradle task that generates a {@code meta.properties} file containing
 * the application's {@code version}, {@code name}, and {@code buildDate}.
 */
@CacheableTask
public abstract class CreatePropertiesTask extends DefaultTask {

    /** The application version (defaults to {@code project.version}). */
    @Input
    public abstract Property<String> getVersion();

    /** The application display name (from the {@code appProperties} extension). */
    @Input
    public abstract Property<String> getDisplayName();

    /**
     * The copyright holder string (from the {@code appProperties} extension).
     * An empty value means no copyright notice is appended to the version descriptor.
     */
    @Input
    public abstract Property<String> getCopyrightHolder();

    /**
     * Optional first year of the copyright range (from the {@code appProperties} extension).
     * When absent, only the build year is written.
     */
    @Input
    @Optional
    public abstract Property<Integer> getCopyrightFromYear();

    /** The output {@code meta.properties} file. */
    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void createProperties() throws IOException {
        var outputFile = getOutputFile().get().getAsFile();
        //noinspection ResultOfMethodCallIgnored
        outputFile.getParentFile().mkdirs();

        var p = new Properties();
        p.setProperty("version", getVersion().get());
        p.setProperty("name", getDisplayName().get());
        p.setProperty("buildDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
        p.setProperty("copyrightHolder", getCopyrightHolder().get());
        p.setProperty("copyrightFromYear", getCopyrightFromYear().map(Object::toString).getOrElse(""));

        try (var writer = new FileWriter(outputFile)) {
            p.store(writer, null);
        }
    }
}
