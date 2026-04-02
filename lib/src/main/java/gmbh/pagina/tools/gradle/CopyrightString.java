package gmbh.pagina.tools.gradle;

/**
 * Single source of truth for building copyright notice strings from a holder name and year range.
 *
 * <p>Used by both {@link AppProperties} at application runtime and {@code AppPropertiesExtension}
 * at Gradle build time, so that the format is always identical in both contexts.
 */
public final class CopyrightString {

    private CopyrightString() {}

    /**
     * Builds a copyright notice string.
     *
     * <pre>{@code
     * CopyrightString.of("pagina GmbH, Tübingen", 2020, 2026)
     * // → "© 2020–2026 pagina GmbH, Tübingen"
     *
     * CopyrightString.of("pagina GmbH, Tübingen", 2020, 2026, false)
     * // → "2020–2026 pagina GmbH, Tübingen"
     *
     * CopyrightString.of("pagina GmbH, Tübingen", -1, 2026)
     * // → "© 2026 pagina GmbH, Tübingen"
     *
     * CopyrightString.of("", -1, 2026)
     * // → ""
     * }</pre>
     *
     * @param holder      Copyright holder name, e.g. {@code "pagina GmbH, Tübingen"}.
     *                    Returns {@code ""} when null or empty.
     * @param fromYear    First year of the copyright range, or any non-positive value when absent.
     *                    When strictly less than {@code buildYear}, a range is used.
     * @param buildYear   The build year (typically {@code LocalDate.now().getYear()}).
     * @param showSymbol  When {@code true} (the default), the {@code ©} symbol is prepended.
     * @return The full copyright notice, or {@code ""} when {@code holder} is empty.
     */
    public static String of(String holder, int fromYear, int buildYear, boolean showSymbol) {
        if (holder == null || holder.isEmpty()) return "";
        String year = (fromYear > 0 && fromYear < buildYear)
                ? fromYear + "–" + buildYear
                : String.valueOf(buildYear);
        return (showSymbol ? "© " : "") + year + " " + holder;
    }

    /**
     * Builds a copyright notice string with the {@code ©} symbol shown (default behaviour).
     * Delegates to {@link #of(String, int, int, boolean)} with {@code showSymbol = true}.
     *
     * @param holder    Copyright holder name.
     * @param fromYear  First year of the range, or any non-positive value when absent.
     * @param buildYear The build year.
     * @return The full copyright notice, or {@code ""} when {@code holder} is empty.
     */
    public static String of(String holder, int fromYear, int buildYear) {
        return of(holder, fromYear, buildYear, true);
    }
}

