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

    default ThemeManagerFactory getTheme() {
        return switch (this) {
            case StaticTheme(var theme) -> theme;
            case SystemTheme(var light, var dark) -> light;
        };
    }

    record StaticTheme(ThemeManagerFactory theme) implements JenkinsTheme {
        @DataBoundConstructor
        public StaticTheme {
            theme = Objects.requireNonNullElseGet(theme, NoOpThemeManagerFactory::new);
        }
    }

    record SystemTheme(ThemeManagerFactory light, ThemeManagerFactory dark) implements JenkinsTheme {
        @DataBoundConstructor
        public SystemTheme {
            light = Objects.requireNonNullElseGet(light, NoOpThemeManagerFactory::new);
            dark = Objects.requireNonNullElseGet(dark, NoOpThemeManagerFactory::new);
        }

        public ThemeManagerFactory getLight() {
            return light;
        }

        public ThemeManagerFactory getDark() {
            return dark;
        }
    }
}
