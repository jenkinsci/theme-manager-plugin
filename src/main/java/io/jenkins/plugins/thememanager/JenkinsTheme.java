package io.jenkins.plugins.thememanager;

import hudson.model.Describable;
import hudson.model.Descriptor;
import io.jenkins.plugins.thememanager.none.NoOpThemeManagerFactory;
import java.util.Objects;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

/// Designates the choice of themes the user has available.
/// It is either going to be a single theme, or if they are following the system then a light/dark mode theme
public sealed interface JenkinsTheme extends Describable<JenkinsTheme> {

    @Override
    @SuppressWarnings("unchecked")
    default Descriptor<JenkinsTheme> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(getClass());
    }

    default boolean isSystem() {
        return this instanceof SystemTheme;
    }

    default ThemeManagerFactory getTheme() {
        return switch (this) {
            case StaticTheme st -> st.getTheme();
            case SystemTheme sy -> sy.getLight();
        };
    }

    default ThemeManagerFactory getLight() {
        return switch (this) {
            case StaticTheme st -> st.getTheme();
            case SystemTheme sy -> sy.getLight();
        };
    }

    default ThemeManagerFactory getDark() {
        return switch (this) {
            case StaticTheme st -> st.getTheme();
            case SystemTheme sy -> sy.getDark();
        };
    }

    final class StaticTheme implements JenkinsTheme {
        private final ThemeManagerFactory theme;

        @DataBoundConstructor
        public StaticTheme(ThemeManagerFactory theme) {
            this.theme = Objects.requireNonNullElseGet(theme, NoOpThemeManagerFactory::new);
        }

        @Override
        public ThemeManagerFactory getTheme() {
            return theme;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || (o instanceof StaticTheme that && Objects.equals(theme, that.theme));
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(theme);
        }
    }

    final class SystemTheme implements JenkinsTheme {
        private final ThemeManagerFactory light;
        private final ThemeManagerFactory dark;

        @DataBoundConstructor
        public SystemTheme(ThemeManagerFactory light, ThemeManagerFactory dark) {
            this.light = Objects.requireNonNullElseGet(light, NoOpThemeManagerFactory::new);
            this.dark = Objects.requireNonNullElseGet(dark, NoOpThemeManagerFactory::new);
        }

        @Override
        public ThemeManagerFactory getLight() {
            return light;
        }

        @Override
        public ThemeManagerFactory getDark() {
            return dark;
        }

        @Override
        public boolean equals(Object o) {
            return this == o
                    || (o instanceof SystemTheme that
                            && Objects.equals(light, that.light)
                            && Objects.equals(dark, that.dark));
        }

        @Override
        public int hashCode() {
            return Objects.hash(light, dark);
        }
    }
}
