package io.jenkins.plugins.thememanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ThemeTest {

    @Test
    void getPropertyReturnsKeyIfPresent() {
        Theme theme =
                Theme.builder().withProperty("prism-api", "theme", "some-theme").build();

        Optional<String> property = theme.getProperty("prism-api", "theme");
        assertThat(property, is(Optional.of("some-theme")));
    }

    @Test
    void getPropertyReturnsOptionalIfNotPresent() {
        Theme theme = Theme.builder().build();

        Optional<String> property = theme.getProperty("prism-api", "theme");
        assertThat(property, is(Optional.empty()));
    }

    @Test
    void generateHeaderElementsSupportsMediaQuery() {
        Theme theme = Theme.builder().withCssUrl("/theme-dark/theme.css").build();

        Set<String> unconditional = theme.generateHeaderElements(true);
        assertThat(
                unconditional, contains("<link type=\"text/css\" rel=\"stylesheet\" href=\"/theme-dark/theme.css\"/>"));

        Set<String> mediaScoped = theme.generateHeaderElements(true, "(prefers-color-scheme: dark)");
        assertThat(
                mediaScoped,
                contains(
                        "<link type=\"text/css\" rel=\"stylesheet\" href=\"/theme-dark/theme.css\" media=\"(prefers-color-scheme: dark)\"/>"));
    }

    @Test
    void generatePropertiesIncludesBothLightAndDark() {
        Theme light =
                Theme.builder().withProperty("prism-api", "theme", "prism").build();
        Theme dark =
                Theme.builder().withProperty("prism-api", "theme", "tomorrow").build();
        ThemeSelection selection = new ThemeSelection(
                new ThemeManagerFactory() {
                    @Override
                    public Theme getTheme() {
                        return light;
                    }
                },
                new ThemeManagerFactory() {
                    @Override
                    public Theme getTheme() {
                        return dark;
                    }
                });

        assertThat(
                selection.generateProperties(),
                is(
                        "<script id=\"theme-manager-properties\" type=\"application/json\">{\"light\":{\"prism-api:theme\":\"prism\"},\"dark\":{\"prism-api:theme\":\"tomorrow\"}}</script>"));
    }
}
