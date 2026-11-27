package io.jenkins.plugins.thememanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ThemeTest {

    private static class StubThemeManagerFactory extends ThemeManagerFactory {
        private final Theme theme;
        private final DescriptorImpl descriptor;

        StubThemeManagerFactory(String themeId, boolean namespaced, Theme theme) {
            this.theme = theme;
            this.descriptor = new DescriptorImpl(themeId, namespaced);
        }

        @Override
        public Theme getTheme() {
            return theme;
        }

        @Override
        public ThemeManagerFactoryDescriptor getDescriptor() {
            return descriptor;
        }

        private static class DescriptorImpl extends ThemeManagerFactoryDescriptor {
            private final String themeId;
            private final boolean namespaced;

            DescriptorImpl(String themeId, boolean namespaced) {
                this.themeId = themeId;
                this.namespaced = namespaced;
            }

            @Override
            public String getThemeId() {
                return themeId;
            }

            @Override
            public boolean isNamespaced() {
                return namespaced;
            }
        }
    }

    private static ThemeManagerFactory stubFactory(String themeId, boolean namespaced, Theme theme) {
        return new StubThemeManagerFactory(themeId, namespaced, theme);
    }

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
    void generateHeaderElementsScopesNonNamespacedCssByColorScheme() {
        ThemeManagerFactory light = stubFactory(
                "light-css",
                false,
                Theme.builder().withCssUrl("/theme-light/theme.css").build());
        ThemeManagerFactory dark = stubFactory(
                "dark-css",
                false,
                Theme.builder().withCssUrl("/theme-dark/theme.css").build());
        ThemeManagerFactory namespaced = stubFactory(
                "namespaced",
                true,
                Theme.builder().withCssUrl("/theme-ns/theme.css").build());

        Set<String> splitElements = new ThemeSelection(light, dark).generateHeaderElements(true);
        assertThat(
                splitElements,
                contains(
                        "<link type=\"text/css\" rel=\"stylesheet\" href=\"/theme-light/theme.css\" media=\"(prefers-color-scheme: light)\"/>",
                        "<link type=\"text/css\" rel=\"stylesheet\" href=\"/theme-dark/theme.css\" media=\"(prefers-color-scheme: dark)\"/>"));

        // Namespaced themes are loaded unconditionally by ThemeManagerPageDecorator, so ThemeSelection skips them
        Set<String> mixedElements = new ThemeSelection(namespaced, dark).generateHeaderElements(true);
        assertThat(
                mixedElements,
                contains(
                        "<link type=\"text/css\" rel=\"stylesheet\" href=\"/theme-dark/theme.css\" media=\"(prefers-color-scheme: dark)\"/>"));
    }

    @Test
    void generatePropertiesIncludesBothLightAndDark() {
        ThemeManagerFactory light = stubFactory(
                "light-test",
                true,
                Theme.builder().withProperty("prism-api", "theme", "prism").build());
        ThemeManagerFactory dark = stubFactory(
                "dark-test",
                true,
                Theme.builder().withProperty("prism-api", "theme", "tomorrow").build());

        assertThat(
                new ThemeSelection(light, dark).generateProperties(),
                is(
                        "<script id=\"theme-manager-properties\" type=\"application/json\">{\"light\":{\"prism-api:theme\":\"prism\"},\"dark\":{\"prism-api:theme\":\"tomorrow\"}}</script>"));
    }
}
