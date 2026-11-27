package io.jenkins.plugins.thememanager;

import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.kohsuke.stapler.DataBoundConstructor;

/// Designates the light and dark themes selected for Jenkins.
public final class ThemeSelection {

    private final ThemeManagerFactory light;
    private final ThemeManagerFactory dark;

    @DataBoundConstructor
    public ThemeSelection(ThemeManagerFactory light, ThemeManagerFactory dark) {
        this.light = Objects.requireNonNullElseGet(light, NoOpThemeManagerFactory::new);
        this.dark = Objects.requireNonNullElseGet(dark, NoOpThemeManagerFactory::new);
    }

    public static ThemeSelection of(ThemeManagerFactory theme) {
        return new ThemeSelection(theme, theme);
    }

    public ThemeManagerFactory getLight() {
        return light;
    }

    public ThemeManagerFactory getDark() {
        return dark;
    }

    Set<String> generateHeaderElements(boolean injectCss) {
        Set<String> headerElements = new LinkedHashSet<>();
        if (!light.getDescriptor().isNamespaced()) {
            headerElements.addAll(light.getTheme().generateHeaderElements(injectCss, "(prefers-color-scheme: light)"));
        }
        if (!dark.getDescriptor().isNamespaced()) {
            headerElements.addAll(dark.getTheme().generateHeaderElements(injectCss, "(prefers-color-scheme: dark)"));
        }
        return headerElements;
    }

    String generateProperties() {
        return Theme.generateProperties(light.getTheme(), dark.getTheme());
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || (o instanceof ThemeSelection that
                        && Objects.equals(light, that.light)
                        && Objects.equals(dark, that.dark));
    }

    @Override
    public int hashCode() {
        return Objects.hash(light, dark);
    }
}
