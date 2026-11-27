package io.jenkins.plugins.thememanager;

import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.util.Objects;
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

    public boolean isSameTheme() {
        return Objects.equals(light.getDescriptor(), dark.getDescriptor());
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
