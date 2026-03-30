package gmbh.pagina.tools.gradle;

import org.gradle.api.provider.Property;

/**
 * Extension for the App Properties plugin.
 *
 * <p>All properties are <em>optional</em>. Sensible defaults are provided for every field.
 *
 * <pre>{@code
 * appProperties {
 *     displayName       = "My Application"        // optional, default: project.name
 *     copyrightHolder   = "pagina GmbH, Tübingen" // optional, default: "" (omitted)
 *     copyrightFromYear = 2020                    // optional, default: absent (build year only)
 * }
 * }</pre>
 */
public abstract class AppPropertiesExtension {

    /**
     * Human-readable display name of the application.
     *
     * <p>Written into {@code meta.properties} as the {@code name} key and exposed via
     * {@code AppProperties.name} at runtime.
     *
     * <p><b>Optional.</b> Defaults to the Gradle project name.
     */
    public abstract Property<String> getDisplayName();

    /**
     * Copyright holder string, e.g. {@code "pagina GmbH, Tübingen"}.
     *
     * <p>Written into {@code meta.properties} as the {@code copyrightHolder} key. Together with
     * the build year (and optionally {@link #getCopyrightFromYear()}), it is used to build
     * {@code AppProperties.copyrightString} at runtime.
     *
     * <p><b>Optional.</b> Defaults to {@code ""}, which omits the copyright notice from
     * {@code AppProperties.copyrightString} and {@code AppProperties.versionDescriptor}.
     */
    public abstract Property<String> getCopyrightHolder();

    /**
     * First year of the copyright range, e.g. {@code 2020}.
     *
     * <p>When set <em>and</em> earlier than the build year, {@code AppProperties.copyrightString}
     * reads {@code "© 2020-2026 …"} instead of {@code "© 2026 …"}. When set to the same value as
     * the build year, or when absent, only the build year is shown.
     *
     * <p><b>Optional.</b> No default — leave unset to display only the build year.
     */
    public abstract Property<Integer> getCopyrightFromYear();
}
