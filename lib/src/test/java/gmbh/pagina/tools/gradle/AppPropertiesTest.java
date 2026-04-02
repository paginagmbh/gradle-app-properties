package gmbh.pagina.tools.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

/**
 * Unit tests for {@link AppProperties}.
 *
 * <p>All fields are {@code static final} and are initialised once at class-load time from the
 * {@code meta.properties} fixture in {@code src/test/resources}. The fixture uses a fixed build
 * date ({@code 2020-06-15}) and {@code copyrightFromYear=2018} so the year-range branch of
 * {@link AppProperties#copyrightString} is deterministically exercised without depending on the
 * current system date.
 */
class AppPropertiesTest {

    // -----------------------------------------------------------------------
    // Raw bundle fields
    // -----------------------------------------------------------------------

    @Test
    void version_isReadFromBundle() {
        assertEquals("1.2.3-TEST", AppProperties.version);
    }

    @Test
    void name_isReadFromBundle() {
        assertEquals("Test Application", AppProperties.name);
    }

    @Test
    void buildDate_isParsedAsLocalDate() {
        assertEquals(LocalDate.of(2020, 6, 15), AppProperties.buildDate);
    }

    @Test
    void isoBuildDate_isFormattedAsIso8601() {
        assertEquals("2020-06-15", AppProperties.isoBuildDate);
    }

    @Test
    void copyrightHolder_isReadFromBundle() {
        assertEquals("Test Holder GmbH", AppProperties.copyrightHolder);
    }

    @Test
    void copyrightFromYear_isReadFromBundle() {
        assertEquals("2018", AppProperties.copyrightFromYear);
    }

    // -----------------------------------------------------------------------
    // Derived fields
    // -----------------------------------------------------------------------

    @Test
    void copyrightString_whenFromYearIsEarlierThanBuildYear_usesRange() {
        // fixture: buildDate=2020-06-15, copyrightFromYear=2018 → 2018 < 2020
        assertEquals("© 2018–2020 Test Holder GmbH", AppProperties.copyrightString);
    }

    @Test
    void copyrightStringWithoutSymbol_omitsSymbol() {
        assertEquals("2018–2020 Test Holder GmbH", AppProperties.copyrightStringWithoutSymbol);
    }

    @Test
    void versionDescriptor_containsAllComponents() {
        assertEquals(
                "Test Application 1.2.3-TEST, built 2020-06-15 © 2018–2020 Test Holder GmbH",
                AppProperties.versionDescriptor
        );
    }
}

