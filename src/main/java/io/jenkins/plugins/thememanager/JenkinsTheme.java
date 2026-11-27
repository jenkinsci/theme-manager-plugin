package io.jenkins.plugins.thememanager;

import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.util.Objects;
import org.kohsuke.stapler.DataBoundConstructor;

public final class JenkinsTheme {

    private final ThemeManagerFactory light;
    private final ThemeManagerFactory dark;

    @DataBoundConstructor
    public JenkinsTheme(ThemeManagerFactory light, ThemeManagerFactory dark) {
        this.light = Objects.requireNonNullElseGet(light, NoOpThemeManagerFactory::new);
        this.dark = Objects.requireNonNullElseGet(dark, NoOpThemeManagerFactory::new);
    }

    public static JenkinsTheme of(ThemeManagerFactory theme) {
        return new JenkinsTheme(theme, theme);
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
                || (o instanceof JenkinsTheme that
                        && Objects.equals(light, that.light)
                        && Objects.equals(dark, that.dark));
    }

    @Override
    public int hashCode() {
        return Objects.hash(light, dark);
    }
}
