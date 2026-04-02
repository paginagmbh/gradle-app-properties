package gmbh.pagina.tools.gradle;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.time.LocalDate;

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
 *
 * <p>The computed accessors {@link #getBuildDate()} and {@link #getCopyrightString()} return
 * lazy {@link Provider}s and can be passed directly to other plugins' properties:
 *
 * <pre>{@code
 * otherPlugin {
 *     copyright = appProperties.getCopyrightString()
 *     date      = appProperties.getBuildDate()
 * }
 * }</pre>
 */
public abstract class AppPropertiesExtension {

    /** Gradle-injected factory used to construct the computed providers. */
    @Inject
    protected abstract ProviderFactory getProviders();

    // ===============================================================================================
    // Input properties
    // ===============================================================================================

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

    // ===============================================================================================
    // Computed accessors (lazy Providers)
    // ===============================================================================================

    /**
     * Returns a lazy provider for today's build date ({@link LocalDate#now()}).
     *
     * <p>The value is resolved at task execution time, not at configuration time. Pass it directly
     * to other plugins' {@code Property<LocalDate>} fields:
     *
     * <pre>{@code
     * otherPlugin {
     *     date = appProperties.getBuildDate()
     * }
     * }</pre>
     */
    public Provider<LocalDate> getBuildDate() {
        return getProviders().provider(LocalDate::now);
    }

    /**
     * Returns a lazy provider for the full copyright notice string including the {@code ©} symbol,
     * computed from {@link #getCopyrightHolder()} and {@link #getCopyrightFromYear()}.
     *
     * <p>Example: {@code "© 2020–2026 pagina GmbH, Tübingen"}
     *
     * @see #getCopyrightStringWithoutSymbol()
     */
    public Provider<String> getCopyrightString() {
        return getCopyrightHolder().flatMap(holder ->
                getCopyrightFromYear().orElse(-1).map(fromYear ->
                        CopyrightString.of(holder, fromYear, LocalDate.now().getYear())
                )
        );
    }

    /**
     * Returns a lazy provider for the copyright notice string <em>without</em> the {@code ©}
     * symbol, computed from {@link #getCopyrightHolder()} and {@link #getCopyrightFromYear()}.
     *
     * <p>Example: {@code "2020–2026 pagina GmbH, Tübingen"}
     *
     * @see #getCopyrightString()
     */
    public Provider<String> getCopyrightStringWithoutSymbol() {
        return getCopyrightHolder().flatMap(holder ->
                getCopyrightFromYear().orElse(-1).map(fromYear ->
                        CopyrightString.of(holder, fromYear, LocalDate.now().getYear(), false)
                )
        );
    }
}
